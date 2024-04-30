package com.example.routes

import com.example.models.Customer
import org.http4k.core.*
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNSUPPORTED_MEDIA_TYPE
import org.http4k.filter.ServerFilters
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.format.KotlinxSerialization.json
import org.http4k.lens.LensFailure
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

fun routesFor(customerStorage: MutableList<Customer>) =
    ServerFilters.CatchLensFailure.then(
        routes(
            "/customer/{id}" bind GET to { req ->
                val id = req.path("id")
                val customer = customerStorage.find { it.id == id }
                when {
                    customer == null ->
                        Response(NOT_FOUND).body("No customer with id $id")
                    else ->
                        Response(OK).json(customer)
                }
            },
            "/customer" bind GET to { req ->
                when {
                    req.uri.path.endsWith("/") ->
                        Response(BAD_REQUEST).body("Missing id")
                    customerStorage.isEmpty() ->
                        Response(OK).body("No customers found")
                    else ->
                        Response(OK).json(customerStorage)
                }
            },
            "/customer/{id}" bind DELETE to { req ->
                val id = req.path("id")
                val deleted = customerStorage.removeIf { it.id == id }
                when (deleted) {
                    true ->
                        Response(ACCEPTED).body("Customer removed correctly")

                    else ->
                        Response(NOT_FOUND).body("Not Found")
                }
            },
            "/customer" bind DELETE to {
                Response(BAD_REQUEST)
            },
            "/customer" bind Method.POST to { request ->
                try {
                    val customer = request.json<Customer>()
                    customerStorage.add(customer)
                    Response(CREATED)
                } catch (x: LensFailure) {
                    Response(UNSUPPORTED_MEDIA_TYPE)
                }
            }
        )
    )

inline fun <reified T : Any> Request.json(): T = Body.auto<T>().toLens()(this)