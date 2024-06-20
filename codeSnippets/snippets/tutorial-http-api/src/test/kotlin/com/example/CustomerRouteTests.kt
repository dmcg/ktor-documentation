package com.example

import com.example.models.Customer
import com.example.models.Order
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomerRouteTests {

    private val aCustomer = Customer(
        id = "id",
        firstName = "firstName",
        lastName = "lastName",
        email = "email",
    )
    private val aCustomerJson = """{"id":"id","firstName":"firstName","lastName":"lastName","email":"email"}"""

    private val customers = mutableListOf(aCustomer)
    private val orders = emptyList<Order>()

    @Test
    fun `returns No customers found when there are no customers`() =
        testApplicationWith(customers, orders) {
            customers.clear()
            with(client.get("/customer")) {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("No customers found", bodyAsText())
            }
        }

    @Test
    fun `returns all the customers`() =
        testApplicationWith(customers, orders) {
            with(client.get("/customer")) {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("[$aCustomerJson]", bodyAsText())
            }
        }

    @Test
    fun `returns a single customer by id`() =
        testApplicationWith(customers, orders) {
            with(client.get("/customer/id")) {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(aCustomerJson, bodyAsText())
            }
        }

    @Test
    fun `returns 404 when no customer found by id`() =
        testApplicationWith(customers, orders) {
            with(client.get("/customer/no-such-id")) {
                assertEquals(HttpStatusCode.NotFound, status)
                assertEquals("No customer with id no-such-id", bodyAsText())
            }
        }

    @Test
    fun `returns 400 when no id`() =
        testApplicationWith(customers, orders) {
            with(client.get("/customer/")) {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertEquals("Missing id", bodyAsText())
            }
        }

    @Test
    fun `deletes a customer by id`() =
        testApplicationWith(customers, orders) {
            with(client.delete("/customer/id")) {
                assertEquals(HttpStatusCode.Accepted, status)
                assertEquals("Customer removed correctly", bodyAsText())
            }
            assertTrue(customers.isEmpty())
        }

    @Test
    fun `returns 404 when no customer found to delete`() =
        testApplicationWith(customers, orders) {
            with(client.delete("/customer/no-such-id")) {
                assertEquals(HttpStatusCode.NotFound, status)
                assertEquals("Not Found", bodyAsText())
            }
            assertFalse(customers.isEmpty())
        }

    @Test
    fun `returns 400 when no id given to delete`() =
        testApplicationWith(customers, orders) {
            with(client.delete("/customer/")) {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
            assertFalse(customers.isEmpty())
        }

    @Test
    fun `adds a customer`() =
        testApplicationWith(customers, orders) {
            customers.clear()
            val response = client.post("/customer") {
                contentType(ContentType.Application.Json)
                setBody(aCustomerJson)
            }
            with(response) {
                assertEquals(HttpStatusCode.Created, status)
            }
            assertEquals(listOf(aCustomer), customers)
        }

    @Test
    fun `returns 415 when no customer to add`() =
        testApplicationWith(customers, orders) {
            val response = client.post("/customer") {
                contentType(ContentType.Application.Json)
                setBody("")
            }
            with(response) {
                assertEquals(HttpStatusCode.UnsupportedMediaType, status)
            }
            assertEquals(listOf(aCustomer), customers)
        }
}
