/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(private val dal: AntaeusDal) : InvoiceProvider {
    override fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    override fun fetchByStatus(customerId : Int, status : InvoiceStatus): List<Invoice> {
        return dal.fetchInvoicesByStatus(customerId, status)
    }

    override fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    override fun updateInvoiceStatus(invoice : Invoice, newStatus : InvoiceStatus)
    {
        dal.updateInvoiceStatus(invoice.id, newStatus)
    }
}
