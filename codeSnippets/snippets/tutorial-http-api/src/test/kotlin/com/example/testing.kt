package com.example

import com.example.models.Customers
import com.example.models.Order
import io.ktor.server.testing.*

fun testApplicationWith(
    customerStorage: Customers,
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