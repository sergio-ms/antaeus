package io.pleo.antaeus.factories

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.providers.BillingProvider
import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import kotlin.random.Random

object ServiceFactory {

    private var dal : AntaeusDal? = null

    fun setDal(dal : AntaeusDal) {
        this.dal = dal
    }
    fun getCustomerProvider(): CustomerService {
        return CustomerService(dal!!)
    }

    fun getBillingProvider(): BillingProvider {
        var paymentProvider = getPaymentProvider()
        var invoiceProvider = getInvoiceProvider()

        return BillingService(paymentProvider, invoiceProvider, getCustomerProvider())
    }

    fun getInvoiceProvider(): InvoiceProvider {
        return InvoiceService(dal!!)
    }

    fun getPaymentProvider(): PaymentProvider {
        return object : PaymentProvider {
            override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
            }
        }
    }
}