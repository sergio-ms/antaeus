package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.providers.BillingProvider
import io.pleo.antaeus.core.providers.CustomerProvider
import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(private val paymentProvider: PaymentProvider, private val invoiceProvider: InvoiceProvider,
                     private val customerProvider : CustomerProvider) :
    BillingProvider {

    override fun chargeInvoice(invoiceId: Int) {
        val invoice = invoiceProvider.fetch(invoiceId)
        if (invoice.canBeInvoiced) {
            val newStatus = tryToChargeInvoice(invoice)
            invoiceProvider.updateInvoiceStatus(invoice, newStatus)
        }
    }

    override fun GetCustomersToBillMontly(): List<Customer> {
        return customerProvider.fetchCustomersWithPendingInvoices()
    }

    private fun tryToChargeInvoice(invoice: Invoice): InvoiceStatus {
        try {
            if (paymentProvider.charge(invoice)) {
                return InvoiceStatus.PAID
            } else {
                return InvoiceStatus.PENDING_INSUFICIENTCASH
            }
        } catch (ex: CustomerNotFoundException) {
            // Do not retry as this probably requires manual intervention
            return InvoiceStatus.PENDING_INVALIDCUSTOMER
        } catch (ex: CurrencyMismatchException) {
            // Do not retry as this probably requires manual intervention
            return InvoiceStatus.PENDING_INVALIDCURRENCY
        } catch (ex: NetworkException) {
            // TODO: retry
            return InvoiceStatus.PENDING_NETWORKUNAVAILABLE
        }
    }


}
