package io.pleo.antaeus.messaging.processors

import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.messaging.*
import io.pleo.antaeus.messaging.messages.CustomerToInvoiceMsg
import io.pleo.antaeus.messaging.messages.InvoiceInfoMsg
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
        var connInfo = ConnectionInfo()
        connInfo.connectionName = conf.connectionName
        connInfo.exchange = conf.invoicesExchange
        connInfo.queue = conf.pendingInvoicesQueue
        connInfo.exchangeType = conf.exchangeType
        connInfo.routingKey = conf.routingKey

        val customerToInvoice = Json.decodeFromString<CustomerToInvoiceMsg>(message)
        println("['$consumerTag'] Processing invoices for customer ${customerToInvoice.customerId}")
        // TODO use status in msg
        invoiceProvider.fetchByStatus(customerToInvoice.customerId, customerToInvoice.statusToProcess)
            .map { invoice -> invoice.id  }
            .chunked(10)
            .map { invoiceList ->
                val msg = Json.encodeToString(InvoiceInfoMsg(invoiceList))
                messagePublisher.publish(msg, connInfo)
            }
    }

    private fun onCancelAction(consumerTag: String?) {
        println("Cancelling: '$consumerTag'")
    }
}