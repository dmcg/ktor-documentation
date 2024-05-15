package com.example.routes

import com.example.exampleAppJson
import com.example.models.Customer
import org.http4k.core.*
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

fun routesFor(customers: MutableList<Customer>) =
    with(exampleAppJson) {
        routes(
            "/" bind GET to {
                if (customers.isEmpty()) {
                    Response(OK).body("No customers found")
                } else {
                    Response(OK).json(customers)
                }
            },
            "/{id}" bind GET to { req ->
                val id = req.path("id")
                val customer = customers.find { it.id == id }
                when {
                    customer == null -> Response(NOT_FOUND).body("No customer with id $id")
                    else -> Response(OK).json(customer)
                }
            },
            "/" bind Method.POST to { request ->
                val customer = request.json<Customer>()
                customers.add(customer)
                Response(CREATED).body("Customer stored correctly")
            },
            "/{id}" bind DELETE to { req ->
                val id = req.path("id")
                if (customers.removeIf { it.id == id }) {
                    Response(ACCEPTED).body("Customer removed correctly")
                } else {
                    Response(NOT_FOUND).body("Not Found")
                }
            }
        )
    }