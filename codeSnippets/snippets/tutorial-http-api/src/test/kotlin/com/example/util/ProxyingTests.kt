package com.example.util

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.Serializable
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ProxyingTests {

    @Test
    fun `fake lets us implement an interface`() {
        val mock = object : List<String> by fake() {
            override fun get(index: Int): String {
                return "banana"
            }
        }
        assertEquals("banana", mock.get(1))
        assertThrows<NotImplementedError> {
            mock.isEmpty()
        }
    }

    @Test
    fun `stronglyStructurallyTypedAs calls methods with matching name and signature`() {
        val wrapped = listOf("banana", "kumquat")
        val wrapper = wrapped.stronglyStructurallyTypedAs<MyCollection<String>>()
        assertEquals(2, wrapper.size())
        assertEquals("kumquat", wrapper.get(1))
    }

    @Test
    fun `stronglyStructurallyTypedAs throws NoSuchMethodError on creation if can't find a match`() {
        val wrapped = listOf("banana", "kumquat")
        try {
            wrapped.stronglyStructurallyTypedAs<MyCollectionWithNoSuch<String>>()
            fail("No exception was thrown")
        } catch (e: NoSuchMethodError) {
            println(e.message)
            assertTrue(e.message!!.contains("Nothing found to match methods"))
            assertTrue(e.message!!.contains("public abstract java.lang.Object com.example.util.MyCollectionWithNoSuch.nosuch()"))
        }
    }

    @Test
    fun `weaklyStructurallyTypedAs does not throw NoSuchMethodError`() {
        val wrapped = listOf("banana", "kumquat")
        val implementation =
            object :
                MyCollectionWithNoSuch<String> by wrapped.weaklyStructurallyTypedAs<MyCollectionWithNoSuch<String>>() {
                override fun nosuch(): Any = "aha"
            }
        assertEquals("aha", implementation.nosuch())
        assertEquals(2, implementation.size())
    }

    @Test
    fun `coStronglyStructurallyTypedAs calls methods with matching name and signature ignoring suspend`() {
        val wrapped = listOf("banana", "kumquat")
        val wrapper = wrapped.coStronglyStructurallyTypedAs<MyCoCollection<String>>()
        runTest {
            assertEquals(2, wrapper.size())
            assertEquals("kumquat", wrapper.get(1))
            assertEquals(false, wrapper.isEmpty())
        }
    }

    @Test
    fun `can distinguish between nothing to call and returning null`() {
        val wrapped = object {
            @Suppress("unused")
            fun nosuch() = null
        }
        val wrapper = wrapped.weaklyStructurallyTypedAs<MyCollectionWithNoSuch<String>>()
        assertEquals(null, wrapper.nosuch())
    }

    @Test
    fun `allTypes can find all types of an object`() {
        val thing = "hello"
        assertEquals(
            setOf(
                String::class.java,
                Serializable::class.java,
                Comparable::class.java,
                CharSequence::class.java,
                java.lang.constant.Constable::class.java,
                java.lang.constant.ConstantDesc::class.java,
                Object::class.java
            ),
            thing::class.java.allTypes()
        )
    }
}

interface MyCollection<E> {
    fun size(): Int
    fun get(index: Int): E
}

interface MyCoCollection<E> {
    suspend fun size(): Int
    suspend fun get(index: Int): E
    fun isEmpty(): Boolean
}

interface MyCollectionWithNoSuch<E> {
    fun size(): Int
    fun get(index: Int): E
    fun nosuch(): Any?
}