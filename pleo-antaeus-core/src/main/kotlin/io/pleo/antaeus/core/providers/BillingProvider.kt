package io.pleo.antaeus.core.providers

import io.pleo.antaeus.models.Customer

interface BillingProvider {
    suspend fun chargeInvoices(invoiceIds: List<Int>)
    suspend fun chargeInvoice(invoiceId : Int)
    suspend fun getCustomersToBillMonthly(): List<Customer>
}