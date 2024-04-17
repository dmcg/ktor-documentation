package com.example

import com.example.models.Customer
import com.example.models.Order
import com.example.models.customerStorage
import com.example.models.orderStorage
import io.ktor.server.application.*
import com.example.plugins.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

//fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        module = { module(customerStorage, orderStorage) }
    ).start(wait = true)
}

fun Application.module(customers: MutableList<Customer>, orders: List<Order>) {
    configureRouting(customers, orders)
    configureSerialization()
}
