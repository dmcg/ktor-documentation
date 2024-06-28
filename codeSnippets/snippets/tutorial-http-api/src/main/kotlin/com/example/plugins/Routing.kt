package com.example.plugins

import com.example.models.Customers
import com.example.models.Order
import com.example.routes.customerRouting
import com.example.routes.getOrderRoute
import com.example.routes.listOrdersRoute
import com.example.routes.totalizeOrderRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    customers: Customers,
    orders: List<Order>
) {
    routing {
        customerRouting(customers)
        listOrdersRoute(orders)
        getOrderRoute(orders)
        totalizeOrderRoute(orders)
    }
}
