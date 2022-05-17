package io.pleo.antaeus.messaging.processors
import io.pleo.antaeus.core.providers.BillingProvider
import io.pleo.antaeus.messaging.MessagingConfiguration
import io.pleo.antaeus.messaging.MessageConsumer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PendingInvoiceProcessor(private val conf : MessagingConfiguration,
                              private val messageConsumer : MessageConsumer,
                              private val billingProvider : BillingProvider) {
    val consumerTag = "InvoiceConsumer"

    fun start()
    {
        messageConsumer.subscribe(conf.connectionName, conf.pendingInvoicesQueue, consumerTag,
            ::onDeliverAction, ::onCancelAction)
    }

    private fun onDeliverAction(consumerTag : String?, message : String)
    {
        val invoiceId = Json.decodeFromString<Int>(message)
        billingProvider.chargeInvoice(invoiceId)
    }


    private fun onCancelAction(consumerTag : String?)
    {
        println("Cancelling: '$consumerTag'")
    }
}