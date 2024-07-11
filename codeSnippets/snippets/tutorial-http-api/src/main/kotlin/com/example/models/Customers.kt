package com.example.models

interface Customers {
    fun list(): List<Customer>
    fun findById(id: String): Customer?
    fun addCustomer(customer: Customer)
    fun deleteById(id: String): Boolean
}

interface CoCustomers {
    suspend fun list(): List<Customer>
    suspend fun findById(id: String): Customer?
    suspend fun addCustomer(customer: Customer)
    suspend fun deleteById(id: String): Boolean
}

class InMemoryCustomers(
    val list: MutableList<Customer> = mutableListOf()
) : Customers {
    constructor(vararg customers: Customer) :
        this(customers.toMutableList())
    override fun list(): List<Customer> = list
    override fun findById(id: String) =
        list.find { it.id == id }
    override fun addCustomer(customer: Customer) {
        list.add(customer)
    }
    override fun deleteById(id: String): Boolean {
        return list.removeIf { it.id == id }
    }
    fun clear() {
        list.clear()
    }
    fun isEmpty() = list.isEmpty()
}