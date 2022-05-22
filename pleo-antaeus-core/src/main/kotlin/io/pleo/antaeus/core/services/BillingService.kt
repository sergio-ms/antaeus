package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.providers.BillingProvider
import io.pleo.antaeus.core.providers.CustomerProvider
import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.logging.PleoLogger
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.pow

class BillingService(private val paymentProvider: PaymentProvider,
                     private val invoiceProvider: InvoiceProvider,
                     private val customerProvider : CustomerProvider,
                     private val logger : PleoLogger
) :
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
        try {
            val invoice = invoiceProvider.fetch(invoiceId)
            if (invoice.canBeInvoiced) {
                println("Charging invoice ${invoice.id} for customer ${invoice.customerId}")
                val newStatus = tryToChargeInvoice(invoice)
                invoiceProvider.updateInvoiceStatus(invoice, newStatus)
            }
        }
        catch(e : Exception)
        {
            logger.logError("${this.javaClass.name}: Unable to chargeInvoice. ${e.message}")
        }
    }

    override suspend fun getCustomersToBillOrdinarily(): List<Customer> {
        try {
            return customerProvider.fetchCustomersWithInvoicesAtStatus(getStatusForOrdinaryPayment())
        }
        catch (e : Exception)
        {
            logger.logError("${this.javaClass.name}: Unable to get customers to bill monthly. ${e.message}")
            throw e;
        }
    }

    override fun getDefaulterCustomers(): List<Customer> {
        try {
            return customerProvider.fetchCustomersWithInvoicesAtStatus(getStatusForDefaulters())
        }
        catch (e : Exception)
        {
            logger.logError("${this.javaClass.name}: Unable to get customers to re-bill daily. ${e.message}")
            throw e;
        }
    }

    override fun getCustomersToReBillUrgently(): List<Customer> {
        try {
            return customerProvider.fetchCustomersWithInvoicesAtStatus(getStatusForUrgentRebilling())
        }
        catch (e : Exception)
        {
            logger.logError("${this.javaClass.name}: Unable to get customers to re-bill immediately. ${e.message}")
            throw e;
        }
    }

    override fun getStatusForOrdinaryPayment() : InvoiceStatus = InvoiceStatus.PENDING
    override fun getStatusForUrgentRebilling() : InvoiceStatus = InvoiceStatus.PENDING_NETWORKUNAVAILABLE
    override fun getStatusForDefaulters() : InvoiceStatus = InvoiceStatus.PENDING_INSUFICIENTCASH

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
                 if(retries >= 3) {
                        logger.logError("BillingService: Unable to process invoice ${invoice.id}. Error: ${ex.message}.")
                        return InvoiceStatus.PENDING_NETWORKUNAVAILABLE
                 }
                delay(1000L * (++retries).toDouble().pow(2.0).toLong())
            } catch (ex : Exception) {
                logger.logError("${this.javaClass.name}: Unable to process invoice ${invoice.id}. Error: ${ex.message}.")
                return InvoiceStatus.PENDING_UNKNOWNERROR
            }

        }
    }


}
