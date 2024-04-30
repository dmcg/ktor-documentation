package com.example.plugins

import com.example.models.Customer
import com.example.models.Order
import com.example.routes.routesFor
import org.http4k.core.Method
import org.http4k.core.Method.*
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

fun routesFor(customers: MutableList<Customer>, orders: List<Order>) =
    ServerFilters.CatchLensFailure.then(
        routes(
            "/customer" bind routesFor(customers),
            "/order" bind routesFor(orders)
        )
    )

private fun routesFor(customers: List<Order>) =
    routes(
        "/" bind GET to { TODO() }
    )
