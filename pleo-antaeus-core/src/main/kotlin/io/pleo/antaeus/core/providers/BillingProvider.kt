package io.pleo.antaeus.core.providers

import io.pleo.antaeus.models.Customer

interface BillingProvider {
    fun chargeInvoice(invoiceId : Int)
    fun GetCustomersToBillMontly(): List<Customer>
}