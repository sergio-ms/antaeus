package io.pleo.antaeus.messaging.processors

import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.messaging.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class CustomerToInvoiceProcessor(
    private val conf: MessagingConfiguration,
    private val messageConsumer: MessageConsumer,
    private val messagePublisher: MessagePublisher,
    private val invoiceProvider: InvoiceProvider
) {
    private val consumerTag = "CustomerToInvoiceConsumer"

    fun start() {
        var connInfo = ConnectionInfo()
        connInfo.connectionName = conf.connectionName
        connInfo.exchange = conf.invoicesExchange
        connInfo.queue = conf.pendingInvoicesQueue
        connInfo.exchangeType = conf.exchangeType
        connInfo.routingKey = conf.routingKey

        messagePublisher.connect(connInfo)
        messageConsumer.subscribe(
            conf.connectionName,
            conf.customersToInvoiceQueue,
            consumerTag, ::onDeliverAction, ::onCancelAction
        )
    }

    private fun onDeliverAction(consumerTag: String?, message: String) {

        val customerId = Json.decodeFromString<Int>(message)
        println("['$consumerTag'] Processing invoices for customer $customerId")
        invoiceProvider.fetchPending(customerId)
            .map { invoice ->
                val msg = Json.encodeToString(invoice.id)
                messagePublisher.publish(msg)
            }
    }

    private fun onCancelAction(consumerTag: String?) {
        println("Cancelling: '$consumerTag'")
    }
}