package cc.home.mapping

import com.example.models.Customer
import com.example.models.Order
import com.example.plugins.routesFor
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.server.Jetty
import org.http4k.server.Netty
import org.http4k.server.ServerConfig
import org.http4k.server.asServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.Duration
import java.util.concurrent.*

class ThroughputTests {

    private val customers = mutableListOf<Customer>()
    private val orders = mutableListOf<Order>()
    private val requestCount = 1000

    @Test
    fun test(testInfo: TestInfo) {
        val handler = routesFor(customers, orders)
        val request = Request(Method.GET, "http://localhost:8080/customer")
        val checkResponse = { response: Response -> assertEquals(Status.OK, response.status) }
        val report = requestLotsHttp4k(requestCount, handler, ::Jetty, request, checkResponse)
        with (report) {
            println("${testInfo.displayName} : $requestCount in $duration = $requestsPerSecond r/s [$errorsString]")
        }
    }
}

private fun requestLotsHttp4k(
    requestCount: Int,
    handler: HttpHandler,
    serverConfig: (Int) -> ServerConfig,
    request: Request,
    assertion: (Response) -> Unit
) : Report {
    return handler.asServer(serverConfig(8080)).start().use { server ->
        OkHttp().use { client ->
            val doRequest = { client(request) }
            runLots(::clearThePipes, requestCount, doRequest, assertion)
        }
    }
}

private fun <R> runLots(
    beforeRun: () -> Unit = {},
    count: Int,
    operation: () -> R,
    assertion: (R) -> Unit
): Report {
    val errors = ConcurrentLinkedQueue<Throwable>()
    val callables = List(count) {
        object : Callable<R> {
            override fun call(): R {
                while (true) {
                    try {
                        return operation()
                    } catch (x: Exception) {
                        errors.add(x)
                    }
                }
            }
        }
    }

    val executor = Executors.newVirtualThreadPerTaskExecutor()
    beforeRun()

    val startTimeMs = System.currentTimeMillis()
    val futures: List<Future<R>> = callables.map { executor.submit(it) }
    executor.shutdown()
    val didTerminate = executor.awaitTermination(30, TimeUnit.SECONDS)
    val endTimeMs = System.currentTimeMillis()

    assertTrue(didTerminate, "Failed to complete all jobs")
    futures.forEach {
        assertTrue(it.isDone)
        assertion(it.get())
    }
    return Report(count, startTimeMs, endTimeMs, errors.toList())
}

private fun clearThePipes() {
    System.gc()
    System.gc()
}

private data class Report(
    val requestCount: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val errors: List<Throwable>
) {
    val duration: Duration get() = Duration.ofMillis(endTimeMs - startTimeMs)
    val requestsPerSecond get() = 1000 * requestCount / duration.toMillis()
    val errorsString = errors.groupBy { it.message }.map { (key, value) -> "${value.size} * $key" }.joinToString(", ")
}
