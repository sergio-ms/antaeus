package io.pleo.antaeus.core.providers

import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.InvoiceStatus

interface CustomerProvider {
    fun fetchAll(): List<Customer>
    fun fetch(id: Int): Customer
    fun fetchCustomersWithInvoicesAtStatus(status : InvoiceStatus): List<Customer>


}