package com.example

import com.example.models.Order
import com.example.models.OrderItem
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.*

class OrderRouteTests {

    val myOrders = listOf(
        Order(
            "2020-04-06-01", listOf(
                OrderItem("Ham Sandwich", 2, 5.50),
                OrderItem("Water", 1, 1.50),
                OrderItem("Beer", 3, 2.30),
                OrderItem("Cheesecake", 1, 3.75)
            )
        )
    )

    @Test
    fun testGetOrder() = testApplicationWith(mutableListOf(), myOrders) {
        val response = client.get("/order/2020-04-06-01")
        assertEquals(
            """{"number":"2020-04-06-01","contents":[{"item":"Ham Sandwich","amount":2,"price":5.5},{"item":"Water","amount":1,"price":1.5},{"item":"Beer","amount":3,"price":2.3},{"item":"Cheesecake","amount":1,"price":3.75}]}""",
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
