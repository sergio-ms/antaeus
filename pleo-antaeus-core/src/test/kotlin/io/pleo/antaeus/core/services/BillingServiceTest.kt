package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.providers.CustomerProvider
import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.logging.PleoLogger
import io.pleo.antaeus.models.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingServiceTest {
    private val pendingInvoice = Invoice(1, 100, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING)
    private val paidInvoice = Invoice(2, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PAID)
    private val invalidCcyInvoice = Invoice(3, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_INVALIDCURRENCY)
    private val invalidCustomerInvoice = Invoice(4, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_INVALIDCUSTOMER)
    private val insuficientCashInvoice = Invoice(5, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_INSUFICIENTCASH)
    private val networkUnavailableInvoice = Invoice(6, 200, Money(BigDecimal(1001), Currency.EUR), InvoiceStatus.PENDING_NETWORKUNAVAILABLE)


    private val mockPaymentProvider = mockk<PaymentProvider> {    }
    private val mockInvoiceProvider = mockk<InvoiceProvider> {
        every { fetch(pendingInvoice.id) } returns pendingInvoice
        every { fetch(paidInvoice.id) } returns paidInvoice
        every { fetch(invalidCcyInvoice.id) } returns invalidCcyInvoice
        every { fetch(invalidCustomerInvoice.id) } returns invalidCustomerInvoice
        every { fetch(insuficientCashInvoice.id) } returns insuficientCashInvoice
        every { fetch(networkUnavailableInvoice.id) } returns networkUnavailableInvoice
        every { updateInvoiceStatus(any(), any()) }.returns(Unit)
    }
    private val mockCustomerProvider = mockk<CustomerProvider>{
        every { fetchCustomersWithInvoicesAtStatus(any()) }.returns(listOf())
    }
    private val mockLogger = mockk<PleoLogger>()



    private val billingService = BillingService(mockPaymentProvider, mockInvoiceProvider, mockCustomerProvider, mockLogger)

    @Test
    fun `when charge provider 5 invoices paymentProver provider is called 15 times`()  = runBlocking {
        every { mockPaymentProvider.charge(pendingInvoice) } returns true
        val invoices = listOf(1, 1, 1, 1, 1)
        billingService.chargeInvoices(invoices)

        verify (exactly = 5) {mockPaymentProvider.charge(pendingInvoice)}
    }

    @Test
    fun `when charge provider returns true set invoice to PAID`()  = runBlocking {
        every { mockPaymentProvider.charge(pendingInvoice) } returns true
        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {mockInvoiceProvider.updateInvoiceStatus(pendingInvoice, InvoiceStatus.PAID)}
    }

    @Test
    fun `when charge provider returns false set invoice to INSUFICIENTBALANCE`() = runBlocking {
        every { mockPaymentProvider.charge(pendingInvoice) } returns false
        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {mockInvoiceProvider.updateInvoiceStatus(any(), InvoiceStatus.PENDING_INSUFICIENTCASH)}
    }

    @Test
    fun `when charge invoice is pending cash do charge`() = runBlocking {
        every { mockPaymentProvider.charge(pendingInvoice) }.returns(true)
        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1){ mockPaymentProvider.charge(pendingInvoice) }
    }

    @Test
    fun `when charge invoice is pending insuficient cash do charge`() = runBlocking  {
        every { mockPaymentProvider.charge(insuficientCashInvoice) }.returns(true)
        billingService.chargeInvoice(insuficientCashInvoice.id)
        verify (exactly = 1){ mockPaymentProvider.charge(insuficientCashInvoice) }
    }

    @Test
    fun `when charge invoice is pending network unavailable do charge`() = runBlocking  {
        every { mockPaymentProvider.charge(networkUnavailableInvoice) }.returns(true)
        billingService.chargeInvoice(networkUnavailableInvoice.id)
        verify (exactly = 1){ mockPaymentProvider.charge(networkUnavailableInvoice) }
    }

    @Test
    fun `when charge invoice is paid do not charge`() = runBlocking {
        billingService.chargeInvoice(paidInvoice.id)
        verify (exactly = 0){ mockPaymentProvider.charge(paidInvoice) }
    }

    @Test
    fun `when charge invoice is pending invalid currency do not charge`() = runBlocking  {
        billingService.chargeInvoice(invalidCcyInvoice.id)
        verify (exactly = 0){ mockPaymentProvider.charge(paidInvoice) }
    }

    @Test
    fun `when charge invoice is pending invalid customer do not charge`() = runBlocking  {
        billingService.chargeInvoice(invalidCustomerInvoice.id)
        verify (exactly = 0){ mockPaymentProvider.charge(paidInvoice) }
    }

    @Test
    fun `when charge invoice throws CustomerNotFoundException status is set to PENDING_INVALIDCUSTOMER`() = runBlocking  {
        every { mockPaymentProvider.charge(pendingInvoice) }
            .throws(CustomerNotFoundException(pendingInvoice.customerId))

        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {mockInvoiceProvider.updateInvoiceStatus(any(), InvoiceStatus.PENDING_INVALIDCUSTOMER)}
    }

    @Test
    fun `when charge invoice throws CurrencyMismatchException status is set to PENDING_INVALIDCURRENCY`() = runBlocking  {
        every { mockPaymentProvider.charge(pendingInvoice) }
            .throws(CurrencyMismatchException(pendingInvoice.id, pendingInvoice.customerId))

        billingService.chargeInvoice(pendingInvoice.id)
        verify (exactly = 1) {mockInvoiceProvider.updateInvoiceStatus(any(), InvoiceStatus.PENDING_INVALIDCURRENCY)}
    }

    @Test
    fun `GetCustomersToBillOrdinary should fetch customers with PENDING`()  = runBlocking {

        billingService.getCustomersToBillOrdinarily()
        verify (exactly = 1) {mockCustomerProvider.fetchCustomersWithInvoicesAtStatus(InvoiceStatus.PENDING)}
    }

    @Test
    fun `GetDefaulterCustomers should fetch customers with PENDING_INSUFICIENTCASH`()  = runBlocking {

        billingService.getDefaulterCustomers()
        verify (exactly = 1) {mockCustomerProvider.fetchCustomersWithInvoicesAtStatus(InvoiceStatus.PENDING_INSUFICIENTCASH)}
    }

    @Test
    fun `GetCustomersToReBillUrgently should fetch customers with PENDING_NETWORKUNAVAILABLE`()  = runBlocking {

        billingService.getCustomersToReBillUrgently()
        verify (exactly = 1) {mockCustomerProvider.fetchCustomersWithInvoicesAtStatus(InvoiceStatus.PENDING_NETWORKUNAVAILABLE)}
    }

}