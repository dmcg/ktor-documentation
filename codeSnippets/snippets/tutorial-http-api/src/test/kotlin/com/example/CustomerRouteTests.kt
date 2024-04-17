package com.example

import com.example.models.Customer
import com.example.models.Order
import com.example.models.orderStorage
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class CustomerRouteTests {

    val myCustomerStorage = mutableListOf<Customer>()
    val myOrderStorage = emptyList<Order>()

    private val aCustomer = Customer(
        id = "id",
        firstName = "firstName",
        lastName = "lastName",
        email = "email",
    )

    @Test
    fun `returns No customers found when there are no customers`() =
        testApplicationWith(myCustomerStorage, myOrderStorage) {
            val response = client.get("/customer")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                "No customers found",
                response.bodyAsText()
            )
        }

    @Test
    fun `returns all the customers`() =
        testApplicationWith(myCustomerStorage, myOrderStorage) {
            myCustomerStorage.add(
                aCustomer
            )
            val response = client.get("/customer")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                """[{"id":"id","firstName":"firstName","lastName":"lastName","email":"email"}]""",
                response.bodyAsText()
            )
        }

    @Test
    fun `returns a single customer by id`() =
        testApplicationWith(myCustomerStorage, myOrderStorage) {
            myCustomerStorage.add(
                aCustomer
            )
            val response = client.get("/customer/id")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                """{"id":"id","firstName":"firstName","lastName":"lastName","email":"email"}""",
                response.bodyAsText()
            )
        }
}

fun testApplicationWith(
    customerStorage: MutableList<Customer>,
    orders: List<Order>,
    block: suspend ApplicationTestBuilder.() -> Unit
) {
    testApplication {
        application {
            module(customerStorage, orders)
        }
        this.block()
    }
}
