package com.example.plugins

import com.example.models.Customer
import com.example.models.Order
import com.example.routes.routesFor
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes

fun routesFor(
    customers: MutableList<Customer>,
    orders: List<Order>
): HttpHandler =
    ServerFilters.CatchLensFailure.then(
        routes(
            "/order" bind routesFor(orders),
            "/customer" bind routesFor(customers)
        )
    )

private fun routesFor(customers: List<Order>) =
    routes(
        "/" bind GET to { TODO() }
    )
