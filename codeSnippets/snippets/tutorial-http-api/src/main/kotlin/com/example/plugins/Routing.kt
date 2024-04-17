package com.example.plugins

import com.example.models.Customer
import com.example.models.Order
import com.example.models.orderStorage
import com.example.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(customers: MutableList<Customer>, orders: List<Order>) {
    routing {
        customerRouting(customers)
        listOrdersRoute(orders)
        getOrderRoute(orders)
        totalizeOrderRoute(orders)
    }
}
