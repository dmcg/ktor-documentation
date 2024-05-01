package com.example

import com.example.models.Customer
import com.example.models.Order
import com.example.plugins.routesFor
import org.http4k.format.KotlinxSerialization
import org.http4k.server.Netty
import org.http4k.server.asServer

fun main() {
    val customers = mutableListOf<Customer>()
    val orders = mutableListOf<Order>()
    routesFor(customers, orders)
        .asServer(
            Netty(
                port = System.getenv("PORT")?.toInt() ?: 8080
            )
        ).start()
}

val exampleAppJson = KotlinxSerialization