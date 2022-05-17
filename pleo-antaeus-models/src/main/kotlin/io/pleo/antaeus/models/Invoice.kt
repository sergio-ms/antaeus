package io.pleo.antaeus.models

data class Invoice(
    val id: Int,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus
)
{
    val canBeInvoiced : Boolean
        get() {
            return status == InvoiceStatus.PENDING
                    || status == InvoiceStatus.PENDING_INSUFICIENTCASH
                    || status == InvoiceStatus.PENDING_NETWORKUNAVAILABLE
        }
}


