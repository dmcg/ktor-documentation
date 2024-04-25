package com.example

import com.example.models.Customer
import com.example.models.Order
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomerRouteTests {

    private val customers = mutableListOf<Customer>()
    private val orders = emptyList<Order>()

    private val aCustomer = Customer(
        id = "id",
        firstName = "firstName",
        lastName = "lastName",
        email = "email",
    )
    private val aCustomerJson = """{"id":"id","firstName":"firstName","lastName":"lastName","email":"email"}"""

    @Test
    fun `returns No customers found when there are no customers`() =
        testApplicationWith(customers, orders) {
            val response: HttpResponse = client.get("/customer")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("No customers found", response.bodyAsText())
        }

    @Test
    fun `returns all the customers`() =
        testApplicationWith(customers, orders) {
            customers.add(aCustomer)
            val response = client.get("/customer")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[$aCustomerJson]", response.bodyAsText())
        }

    @Test
    fun `returns a single customer by id`() =
        testApplicationWith(customers, orders) {
            customers.add(aCustomer)
            val response = client.get("/customer/id")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(aCustomerJson, response.bodyAsText())
        }

    @Test
    fun `returns 404 when no customer found by id`() =
        testApplicationWith(customers, orders) {
            customers.add(aCustomer)
            val response = client.get("/customer/no-such-id")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("No customer with id no-such-id", response.bodyAsText())
        }

    @Test
    fun `returns 400 when no id`() =
        testApplicationWith(customers, orders) {
            customers.add(aCustomer)
            val response = client.get("/customer/")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Missing id", response.bodyAsText())
        }

    @Test
    fun `deletes a customer by id`() =
        testApplicationWith(customers, orders) {
            customers.add(aCustomer)
            val response = client.delete("/customer/id")
            assertEquals(HttpStatusCode.Accepted, response.status)
            assertEquals("Customer removed correctly", response.bodyAsText())
            assertTrue(customers.isEmpty())
        }

    @Test
    fun `returns 404 when no customer found to delete`() =
        testApplicationWith(customers, orders) {
            customers.add(aCustomer)
            val response = client.delete("/customer/no-such-id")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("Not Found", response.bodyAsText())
            assertFalse(customers.isEmpty())
        }

    @Test
    fun `returns 400 when no id given to delete`() =
        testApplicationWith(customers, orders) {
            customers.add(aCustomer)
            val response = client.delete("/customer/")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertFalse(customers.isEmpty())
        }

    @Test
    fun `adds a customer`() =
        testApplicationWith(customers, orders) {
            assertTrue(customers.isEmpty())
            val response = client.post("/customer") {
                contentType(ContentType.Application.Json)
                setBody(aCustomerJson)
            }
            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals(listOf(aCustomer), customers)
        }

    @Test
    fun `returns 415 when no customer to add`() =
        testApplicationWith(customers, orders) {
            val response = client.post("/customer") {
                contentType(ContentType.Application.Json)
                setBody("")
            }
            assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
            assertTrue(customers.isEmpty())
        }
}
