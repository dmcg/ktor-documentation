package cc.home.mapping

import kotlinx.coroutines.delay
import org.http4k.client.OkHttp
import org.http4k.core.*
import org.http4k.lens.Query
import org.http4k.server.JettyLoom
import org.http4k.server.ServerConfig
import org.http4k.server.asServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.Duration
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ThroughputTests {
    private val requestCount = 1000
    private val invocationCount = AtomicInteger(0)
    private lateinit var report: Report

    companion object {
        private val query = Query.required("s")
        private val request = Request(Method.GET, "http://localhost/").with(query of "banana")
        private fun body(value: Any) = Response(Status.OK).body(value.toString())
        private fun checkResponse(response: Response) {
            assertEquals(Status.OK, response.status)
            assertEquals("6", response.bodyString())
        }
    }

    fun fastFun(s: String): Int {
        invocationCount.getAndIncrement()
        return s.length
    }

    fun slowFun(s: String): Int {
        Thread.sleep(1000)
        return fastFun(s)
    }

    suspend fun slowCoroutineFun(s: String): Int {
        delay(1000)
        return fastFun(s)
    }

    @Test
    @Order(2)
    fun `fast fun jettyLoom`() {
        // java version
        println(Runtime.version())
        println(KotlinVersion.CURRENT)
        val handler: HttpHandler = { Response(Status.OK) }
        report = requestLotsHttp4k(requestCount, ::JettyLoom, handler, request, ::checkResponse)
    }

    @AfterEach
    fun report(testInfo: TestInfo) {
        with(report) {
            println("${testInfo.displayName} : $requestCount in $duration = $requestsPerSecond r/s [$errorsString]")
        }
        assertEquals(report.requestCount, invocationCount.get())
    }

}

fun requestLotsHttp4k(
    count: Int,
    serverConfig: (Int) -> ServerConfig,
    handler: HttpHandler,
    request: Request,
    assertion: (Response) -> Unit
) : Report {
    return handler.asServer(serverConfig(0)).start().use { server ->
        val requestForMyPort = request.uri(request.uri.port(server.port()))
        OkHttp().use { client ->
            val doRequest = { client(requestForMyPort) }
            runLots(count, doRequest, assertion, ::clearThePipes)
        }
    }
}

fun <R> runLots(
    count: Int,
    operation: () -> R,
    assertion: (R) -> Unit,
    beforeRun: () -> Unit = {}
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
    Thread.sleep(30000)
}


data class Report(
    val requestCount: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val errors: List<Throwable>
) {
    val duration: Duration get() = Duration.ofMillis(endTimeMs - startTimeMs)
    val requestsPerSecond get() = 1000 * requestCount / duration.toMillis()
    val errorsString = errors.groupBy { it.message }.map { (key, value) -> "${value.size} * $key" }.joinToString(", ")
}
