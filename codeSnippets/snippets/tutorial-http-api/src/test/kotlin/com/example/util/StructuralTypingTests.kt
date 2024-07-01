package com.example.util

import org.junit.jupiter.api.Test
import java.util.ArrayList
import kotlin.test.assertEquals

class StructuralTypingTests {

    @Test
    fun test() {
        val wrapped = ArrayList(listOf("banana", "kumquat"))
        val wrapper = wrapped.structurallyTypedAs<MyCollection<String>>()
        assertEquals(2, wrapper.size())
        assertEquals("kumquat", wrapper.get(1))
    }
}


interface MyCollection<E> {
    fun size(): Int
    fun get(index: Int): E
}