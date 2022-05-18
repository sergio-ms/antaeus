package io.pleo.antaeus.messaging.processors
import io.pleo.antaeus.core.providers.BillingProvider
import io.pleo.antaeus.messaging.MessagingConfiguration
import io.pleo.antaeus.messaging.MessageConsumer
import io.pleo.antaeus.messaging.messages.InvoiceInfoMsg
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking

class PendingInvoiceProcessor(private val conf : MessagingConfiguration,
                              private val messageConsumer : MessageConsumer,
                              private val billingProvider : BillingProvider) {
    val consumerTag = "InvoiceConsumer"

    fun start()
    {
        messageConsumer.subscribe(conf.connectionName, conf.pendingInvoicesQueue, consumerTag,
            ::onDeliverAction, ::onCancelAction)
    }

    private fun onDeliverAction(consumerTag : String?, message : String) {
        runBlocking {
            val invoicesMsg = Json.decodeFromString<InvoiceInfoMsg>(message)
            billingProvider.chargeInvoices(invoicesMsg.invoiceIds)
        }
    }


    private fun onCancelAction(consumerTag : String?)
    {
        println("Cancelling: '$consumerTag'")
    }
}