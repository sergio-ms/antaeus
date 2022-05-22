package io.pleo.antaeus.core.providers

import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.InvoiceStatus

interface BillingProvider {
    /**
     * Receives a list of invoices ids and tries to invoice them
     * @param list of invoices ids
     * @return Instance object
     */
    suspend fun chargeInvoices(invoiceIds: List<Int>)

    /**
     * Tries to charge an invoice
     * @param id of the invoice ot charge
     */
    suspend fun chargeInvoice(invoiceId: Int)

    /**
     * Get a list of customers that have pending invoices and need to be charged
     * by the ordinary billing procedure
     * @returns list of customers
     */
    suspend fun getCustomersToBillOrdinarily(): List<Customer>

    /**
     * Get a list of customers that have pending invoices that could not be paid
     * previously because there was not enough cash
     * @returns list of customers
     */
    fun getDefaulterCustomers(): List<Customer>

    /**
     * Get a list of customers that have pending invoices that could not be paid
     * because some technical (network) problems
     * @returns list of customers
     */
    fun getCustomersToReBillUrgently(): List<Customer>

    /**
     * Get the status that apply to ordinary payments
     * @returns invoice status
     */
    fun getStatusForOrdinaryPayment(): InvoiceStatus

    /**
     * Get the status that apply to urgent payments
     * @returns invoice status
     */
    fun getStatusForUrgentRebilling(): InvoiceStatus

    /**
     * Get the status that apply to defaulter customers payments
     * @returns invoice status
     */
    fun getStatusForDefaulters(): InvoiceStatus
}

