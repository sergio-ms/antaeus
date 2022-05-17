package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.providers.CustomerProvider
import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BillingServiceTest {
    private val pendingInvoice = Invoice(1, 100, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING)
    private val paidInvoice = Invoice(2, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PAID)
    private val invalidCcyInvoice = Invoice(3, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_INVALIDCURRENCY)
    private val invalidCustomerInvoice = Invoice(4, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_INVALIDCUSTOMER)
    private val insuficientCashInvoice = Invoice(5, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_INSUFICIENTCASH)
    private val networkUnavailableInvoice = Invoice(6, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_NETWORKUNAVAILABLE)

    private val paymentProvider = mockk<PaymentProvider> {    }
    private val invoiceProvider = mockk<InvoiceProvider> {
        every { fetch(pendingInvoice.id) } returns pendingInvoice
        every { fetch(paidInvoice.id) } returns paidInvoice
        every { fetch(invalidCcyInvoice.id) } returns invalidCcyInvoice
        every { fetch(invalidCustomerInvoice.id) } returns invalidCustomerInvoice
        every { fetch(insuficientCashInvoice.id) } returns insuficientCashInvoice
        every { fetch(networkUnavailableInvoice.id) } returns networkUnavailableInvoice
        every { updateInvoiceStatus(any(), any()) }.returns(Unit)
    }
    private val customerProvider = mockk<CustomerProvider>{
        every { fetchCustomersWithPendingInvoices() }.returns(listOf())
    }



    private val billingService = BillingService(paymentProvider, invoiceProvider, customerProvider)

    @Test
    fun `when charge provider returns true set invoice to PAID`() {
        every { paymentProvider.charge(pendingInvoice) } returns true
        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {invoiceProvider.updateInvoiceStatus(pendingInvoice, InvoiceStatus.PAID)}
    }

    @Test
    fun `when charge provider returns false set invoice to INSUFICIENTBALANCE`() {
        every { paymentProvider.charge(pendingInvoice) } returns false
        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {invoiceProvider.updateInvoiceStatus(any(), InvoiceStatus.PENDING_INSUFICIENTCASH)}
    }

    @Test
    fun `when charge invoice is pending cash do charge`() {
        every { paymentProvider.charge(pendingInvoice) }.returns(true)
        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1){ paymentProvider.charge(pendingInvoice) }
    }

    @Test
    fun `when charge invoice is pending insuficient cash do charge`() {
        every { paymentProvider.charge(insuficientCashInvoice) }.returns(true)
        billingService.chargeInvoice(insuficientCashInvoice.id)
        verify (exactly = 1){ paymentProvider.charge(insuficientCashInvoice) }
    }

    @Test
    fun `when charge invoice is pending network unavailable do charge`() {
        every { paymentProvider.charge(networkUnavailableInvoice) }.returns(true)
        billingService.chargeInvoice(networkUnavailableInvoice.id)
        verify (exactly = 1){ paymentProvider.charge(networkUnavailableInvoice) }
    }

    @Test
    fun `when charge invoice is paid do not charge`() {
        billingService.chargeInvoice(paidInvoice.id)
        verify (exactly = 0){ paymentProvider.charge(paidInvoice) }
    }

    @Test
    fun `when charge invoice is pending invalid currency do not charge`() {
        billingService.chargeInvoice(invalidCcyInvoice.id)
        verify (exactly = 0){ paymentProvider.charge(paidInvoice) }
    }

    @Test
    fun `when charge invoice is pending invalid customer do not charge`() {
        billingService.chargeInvoice(invalidCustomerInvoice.id)
        verify (exactly = 0){ paymentProvider.charge(paidInvoice) }
    }

    @Test
    fun `when charge invoice throws CustomerNotFoundException status is set to PENDING_INVALIDCUSTOMER`() {
        every { paymentProvider.charge(pendingInvoice) }
            .throws(CustomerNotFoundException(pendingInvoice.customerId))

        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {invoiceProvider.updateInvoiceStatus(any(), InvoiceStatus.PENDING_INVALIDCUSTOMER)}
    }

    @Test
    fun `when charge invoice throws CurrencyMismatchException status is set to PENDING_INVALIDCURRENCY`() {
        every { paymentProvider.charge(pendingInvoice) }
            .throws(CurrencyMismatchException(pendingInvoice.id, pendingInvoice.customerId))

        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {invoiceProvider.updateInvoiceStatus(any(), InvoiceStatus.PENDING_INVALIDCURRENCY)}
    }

    @Test
    fun `GetCustomersToBillMonthly should get fetchCustomersWithPendingInvoices from customerProvider`() {

        billingService.GetCustomersToBillMontly()
        verify (exactly = 1) {customerProvider.fetchCustomersWithPendingInvoices()}
    }

}