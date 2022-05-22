package io.pleo.antaeus.messaging.messages

import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.serialization.Serializable

@Serializable
data class CustomerToInvoiceMsg(val customerId: Int,
                                val statusToProcess : InvoiceStatus)
