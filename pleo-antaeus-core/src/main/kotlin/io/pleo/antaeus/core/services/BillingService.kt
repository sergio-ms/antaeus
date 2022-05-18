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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.pow

class BillingService(private val paymentProvider: PaymentProvider, private val invoiceProvider: InvoiceProvider,
                     private val customerProvider : CustomerProvider) :
    BillingProvider {

    override suspend fun chargeInvoices(invoiceIds: List<Int>) = runBlocking {
        invoiceIds.forEach()
        {
            launch {
                chargeInvoice(it)
            }
        }
    }

    override suspend fun chargeInvoice(invoiceId: Int) {
        val invoice = invoiceProvider.fetch(invoiceId)
        if (invoice.canBeInvoiced) {
            println("Charging invoice ${invoice.id} for customer ${invoice.customerId}")
            val newStatus = tryToChargeInvoice(invoice)
            invoiceProvider.updateInvoiceStatus(invoice, newStatus)
        }
    }

    override suspend fun getCustomersToBillMonthly(): List<Customer> {
        return customerProvider.fetchCustomersWithPendingInvoices()
    }

    private suspend fun tryToChargeInvoice(invoice: Invoice): InvoiceStatus {
        var retries = 0
        while(true) {
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
                // Retry three times with an exponential time strategy. If this fail it will be set to
                // NetworkUnavailable status for a late reprocessing
                if(retries >= 3)
                    return InvoiceStatus.PENDING_NETWORKUNAVAILABLE

                delay(1000L * (++retries).toDouble().pow(2.0).toLong())
            }
        }
    }


}
