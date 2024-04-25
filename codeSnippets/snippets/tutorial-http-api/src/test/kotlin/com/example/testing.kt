package com.example

import com.example.models.Customer
import com.example.models.Order
import io.ktor.server.testing.*

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