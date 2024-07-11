package com.example.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.Serializable
import kotlin.test.assertEquals

class StructuralTypingTests {

    @Test
    fun `calls methods with matching name and signature`() {
        val wrapped = listOf("banana", "kumquat")
        val wrapper = wrapped.structurallyTypedAs<MyCollection<String>>()
        assertEquals(2, wrapper.size())
        assertEquals("kumquat", wrapper.get(1))
    }

    @Test
    fun `throws NoSuchMethodError on creation if can't find a match`() {
        val wrapped = listOf("banana", "kumquat")
        assertThrows<NoSuchMethodError> {
            wrapped.structurallyTypedAs<MyCollection2<String>>()
        }
    }

    @Test
    fun `can find all types of an object`() {
        val thing = "hello"
        assertEquals(
            setOf(String::class.java, Serializable::class.java, Comparable::class.java, CharSequence::class.java, java.lang.constant.Constable::class.java, java.lang.constant.ConstantDesc::class.java, Object::class.java),
            thing::class.java.allTypes()
        )
    }
}

interface MyCollection<E> {
    fun size(): Int
    fun get(index: Int): E
}

interface MyCollection2<E> {
    fun size(): Int
    fun get(index: Int): E
    fun nosuch(): Any
}