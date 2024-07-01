package com.example.util

import java.lang.reflect.Proxy.*

inline fun <reified T> fake() = newProxyInstance(
    T::class.java.classLoader,
    arrayOf(T::class.java)
) { _, method, _ ->
    TODO("No implementation for method $method")
} as T

inline fun <reified T> Any.structurallyTypedAs() = newProxyInstance(
    T::class.java.classLoader,
    arrayOf(T::class.java)
) { _, method, args ->
    val matchingMethod = this.javaClass.getMethod(
        method.name,
        *method.parameterTypes
    )
    matchingMethod.invoke(this, *(args ?: emptyArray()))
} as T
