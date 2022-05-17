package io.pleo.antaeus.core.providers

import io.pleo.antaeus.models.Customer

interface CustomerProvider {
    fun fetchAll(): List<Customer>
    fun fetch(id: Int): Customer
}