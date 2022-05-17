package io.pleo.antaeus.core.providers


import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

interface InvoiceProvider {
    fun fetchAll(): List<Invoice>
    fun fetchPending(customerId : Int): List<Invoice>
    fun fetch(id: Int): Invoice
    fun updateInvoiceStatus(invoice : Invoice, newStatus : InvoiceStatus)

}