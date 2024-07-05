package com.example.util

import java.lang.reflect.Method
import java.lang.reflect.Proxy.newProxyInstance

inline fun <reified T> fake() = newProxyInstance(
    T::class.java.classLoader,
    arrayOf(T::class.java)
) { _, method, _ ->
    TODO("No implementation for method $method")
} as T

inline fun <reified T> Any.structurallyTypedAs(): T =
    structurallyTypedAs(T::class.java)

@Suppress("UNCHECKED_CAST")
fun <T> Any.structurallyTypedAs(proxyType: Class<T>): T {
    val allTypes = this.javaClass.allTypes()
    val lookup: Map<Method, Method> =
        proxyType.methods.associateWith { proxyMethod ->
            allTypes.methodMatching(proxyMethod, thatCanAccess = this) ?:
                throw NoSuchMethodError("Matching " + proxyMethod.toString())
        }
    return newProxyInstance(
        proxyType.classLoader,
        arrayOf(proxyType)
    ) { _, method, args ->
        lookup[method]?.invoke(this, *(args ?: emptyArray()))
            ?: error("No implementation for method $method")
    } as T
}

private fun Iterable<Class<*>>.methodMatching(
    method: Method,
    thatCanAccess: Any
): Method? = firstNotNullOfOrNull { clazz ->
    clazz.methodOrNull(
        method.name,
        *method.parameterTypes
    )?.takeIf { it.canAccess(thatCanAccess) }
}

private fun Class<*>.methodOrNull(
    name: String,
    vararg parameterTypes: Class<*>
): Method? = try {
    getMethod(name, *parameterTypes)
} catch (x: NoSuchMethodException) {
    null
}

internal fun Class<*>.allTypes(
    soFar: MutableList<Class<*>> = mutableListOf()
): Set<Class<*>> {
    soFar.add(this)
    val interfaces = this.interfaces
    soFar.addAll(interfaces)
    interfaces.forEach {
        it.allTypes(soFar)
    }
    return if (this.superclass == null)
        soFar.toSet()
    else
        this.superclass.allTypes(soFar)
}