package com.example.util

import java.lang.reflect.Method
import java.lang.reflect.Proxy.newProxyInstance
import kotlin.coroutines.Continuation

inline fun <reified T> fake() = newProxyInstance(
    T::class.java.classLoader,
    arrayOf(T::class.java)
) { _, method, _ ->
    TODO("No implementation for method $method")
} as T

typealias MyInvocationHandler = (Array<*>) -> Any?

@Suppress("UNCHECKED_CAST")
fun <T> Map<Method, (MyInvocationHandler)?>.asProxy(
    proxyType: Class<T>
) = newProxyInstance(
    proxyType.classLoader,
    arrayOf(proxyType)
) { _, method, args ->
    val invocationHandler =
        this.getOrElse(method) {
            error("No implementation for method $method")
        } ?: error("Bad default logic for method $method")
    invocationHandler.invoke(args ?: emptyArray<Any>())
} as T

inline fun <reified T> Any.stronglyStructurallyTypedAs(): T =
    structuralLookupFor(T::class.java).checkAllFound().asProxy(T::class.java)

inline fun <reified T> Any.weaklyStructurallyTypedAs(): T =
    structuralLookupFor(T::class.java).asProxy(T::class.java)

fun <T> Any.structuralLookupFor(proxyType: Class<T>): Map<Method, (MyInvocationHandler)?> {
    val allTypes = this.javaClass.allTypes()
    return proxyType.methods.associateWith { proxyMethod ->
        allTypes.methodMatching(
            proxyMethod.name,
            proxyMethod.parameterTypes,
            thatCanAccess = this
        )?.let { matchedMethod ->
            { args ->
                matchedMethod.invoke(this, *(args))
            }
        }
    }
}

inline fun <reified T> Any.coStronglyStructurallyTypedAs(): T =
    coStructuralLookupFor(T::class.java).checkAllFound().asProxy(T::class.java)

inline fun <reified T> Any.coWeaklyStructurallyTypedAs(): T =
    coStructuralLookupFor(T::class.java).asProxy(T::class.java)

fun <T> Any.coStructuralLookupFor(proxyType: Class<T>): Map<Method, (MyInvocationHandler)?> {
    val allTypes = this.javaClass.allTypes()
    return proxyType.methods.associateWith { proxyMethod ->
        // If we have a last parameter of Continuation we can ignore it for matching
        // and drop it when we are called.
        val parameterTypes = proxyMethod.parameterTypes
        val isSuspend = parameterTypes.lastOrNull() == Continuation::class.java
        allTypes.methodMatching(
            proxyMethod.name,
            parameterTypes.dropLastIf(isSuspend),
            thatCanAccess = this
        )?.let { matchedMethod ->
            { args ->
                matchedMethod.invoke(this, *(args.dropLastIf(isSuspend)))
            }
        }
    }
}

fun Map<Method, (MyInvocationHandler)?>.checkAllFound(): Map<Method, MyInvocationHandler> {
    val notFound = this.entries.filter { it.value == null }
    if (notFound.isNotEmpty()) {
        throw NoSuchMethodError("Nothing found to match methods ${notFound.map { it.key }}")
    }
    @Suppress("UNCHECKED_CAST")
    return this as Map<Method, MyInvocationHandler>
}

private fun <T> Array<T>.dropLastIf(
    condition: Boolean
): Array<T> = when {
    condition -> copyOfRange(0, size - 1)
    else -> this
}

private fun Iterable<Class<*>>.methodMatching(
    methodName: String,
    parameterTypes: Array<out Class<*>>,
    thatCanAccess: Any
): Method? = firstNotNullOfOrNull { clazz ->
    clazz.methodOrNull(
        methodName,
        *parameterTypes
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
    this.interfaces.forEach {
        it.allTypes(soFar)
    }
    return when (val superClass = this.superclass) {
        null -> soFar.toSet()
        else -> superClass.allTypes(soFar)
    }
}