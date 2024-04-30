package com.example

import com.example.models.*
import com.example.plugins.*
import com.example.routes.routesFor
import org.http4k.core.then
import org.http4k.filter.ServerFilters
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