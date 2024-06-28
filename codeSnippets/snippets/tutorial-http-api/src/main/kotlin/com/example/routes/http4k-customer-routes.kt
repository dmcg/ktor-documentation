package com.example.routes

import com.example.exampleAppJson
import com.example.models.Customer
import com.example.models.Customers
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

fun routesFor(customers: Customers) =
    with(exampleAppJson) {
        routes(
            "/" bind GET to {
                Response(OK).json(customers.list())
            },
            "/{id}" bind GET to { req ->
                val id = req.path("id") ?: error("Should not be null because that is a different route")
                val customer = customers.findById(id)
                when {
                    customer == null -> Response(NOT_FOUND).body("No customer with id $id")
                    else -> Response(OK).json(customer)
                }
            },
            "/" bind Method.POST to { request ->
                val customer = request.json<Customer>()
                customers.addCustomer(customer)
                Response(CREATED).body("Customer stored correctly")
            },
            "/{id}" bind DELETE to { req ->
                val id = req.path("id") ?: error("Should not be null because that is a different method")
                if (customers.deleteById(id)) {
                    Response(ACCEPTED).body("Customer removed correctly")
                } else {
                    Response(NOT_FOUND).body("Not Found")
                }
            }
        )
    }


