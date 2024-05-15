package com.example

import com.example.models.Customer
import com.example.models.Order
import com.example.plugins.routesFor
import org.http4k.format.KotlinxSerialization
import org.http4k.server.Netty
import org.http4k.server.asServer
import kotlin.time.measureTime

fun main() {
    val customers = mutableListOf<Customer>()
    val orders = mutableListOf<Order>()
    routesFor(customers, orders)
        .asServer(
            Netty(System.getenv("PORT")?.toInt() ?: 8080)
        ).start()
}

val exampleAppJson = KotlinxSerialization