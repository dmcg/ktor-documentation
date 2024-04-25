package com.example

import com.example.models.*
import io.ktor.server.application.*
import com.example.plugins.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val customers = mutableListOf<Customer>()
    val orders = listOf(
        Order(
            "2020-04-06-01", listOf(
                OrderItem("Ham Sandwich", 2, 5.50),
                OrderItem("Water", 1, 1.50),
                OrderItem("Beer", 3, 2.30),
                OrderItem("Cheesecake", 1, 3.75)
            )
        ),
        Order(
            "2020-04-03-01", listOf(
                OrderItem("Cheeseburger", 1, 8.50),
                OrderItem("Water", 2, 1.50),
                OrderItem("Coke", 2, 1.76),
                OrderItem("Ice Cream", 1, 2.35)
            )
        )
    )
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        module = { module(customers, orders) }
    ).start(wait = true)
}

fun Application.module(customers: MutableList<Customer>, orders: List<Order>) {
    configureRouting(customers, orders)
    configureSerialization()
}
