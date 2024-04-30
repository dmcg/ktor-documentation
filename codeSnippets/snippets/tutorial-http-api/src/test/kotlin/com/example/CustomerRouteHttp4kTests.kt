package com.example

import com.example.models.Customer
import com.example.models.Order
import com.example.plugins.routesFor
import io.ktor.http.ContentType
import org.http4k.core.*
import org.http4k.core.Method.*
import org.http4k.strikt.bodyString
import org.http4k.strikt.status
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomerRouteHttp4kTests {

    private val customers = mutableListOf<Customer>()
    private val orders = emptyList<Order>()
    val handler = routesFor(customers, orders)

    private val aCustomer = Customer(
        id = "id",
        firstName = "firstName",
        lastName = "lastName",
        email = "email",
    )
    private val aCustomerJson = """{"id":"id","firstName":"firstName","lastName":"lastName","email":"email"}"""

    @Test
    fun `returns No customers found when there are no customers`() {
        expectThat(handler(Request(GET, "/customer"))) {
            status.isEqualTo(Status.OK)
            bodyString.isEqualTo("No customers found")
        }
    }

    @Test
    fun `returns all the customers`() {
        customers.add(aCustomer)
        expectThat(handler(Request(GET, "/customer"))) {
            status.isEqualTo(Status.OK)
            bodyString.isEqualTo("[$aCustomerJson]")
        }
    }

    @Test
    fun `returns a single customer by id`() {
        customers.add(aCustomer)
        expectThat(handler(Request(GET, "/customer/id"))) {
            status.isEqualTo(Status.OK)
            bodyString.isEqualTo(aCustomerJson)
        }
    }

    @Test
    fun `returns 404 when no customer found by id`() {
        customers.add(aCustomer)
        expectThat(handler(Request(GET, "/customer/no-such-id"))) {
            status.isEqualTo(Status.NOT_FOUND)
            bodyString.isEqualTo("No customer with id no-such-id")
        }
    }

    @Test
    fun `returns all customers when no id`() {
        customers.add(aCustomer)
        expectThat(handler(Request(GET, "/customer/"))) {
            status.isEqualTo(Status.OK)
            bodyString.isEqualTo("[$aCustomerJson]")
        }
    }

    @Test
    fun `deletes a customer by id`() {
        customers.add(aCustomer)
        expectThat(handler(Request(DELETE, "/customer/id"))) {
            status.isEqualTo(Status.ACCEPTED)
            bodyString.isEqualTo("Customer removed correctly")
        }
        assertTrue(customers.isEmpty())
    }

    @Test
    fun `returns 404 when no customer found to delete`() {
        customers.add(aCustomer)
        expectThat(handler(Request(DELETE, "/customer/no-such-id"))) {
            status.isEqualTo(Status.NOT_FOUND)
            bodyString.isEqualTo("Not Found")
        }
        assertFalse(customers.isEmpty())
    }

    @Test
    fun `returns 405 when no id given to delete`() {
        customers.add(aCustomer)
        expectThat(handler(Request(DELETE, "/customer/"))) {
            status.isEqualTo(Status.METHOD_NOT_ALLOWED)
        }
        assertFalse(customers.isEmpty())
    }

    @Test
    fun `adds a customer`() {
        assertTrue(customers.isEmpty())
        expectThat(
            handler(Request(POST, "/customer").body(aCustomerJson))
        ) {
            status.isEqualTo(Status.CREATED)
        }
        assertEquals(listOf(aCustomer), customers)
    }

    @Test
    fun `returns 400 when no customer to add`() {
        expectThat(handler(Request(POST, "/customer"))) {
            status.isEqualTo(Status.BAD_REQUEST)
        }
        assertTrue(customers.isEmpty())
    }
}
