package com.example

import com.example.models.*
import io.ktor.server.application.*
import com.example.plugins.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val customers = InMemoryCustomers()
    val orders = mutableListOf<Order>()
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        module = { module(customers, orders) }
    ).start(wait = true)
}

fun Application.module(
    customers: Customers,
    orders: List<Order>
) {
    configureRouting(customers, orders)
    configureSerialization()
}
