package io.pleo.antaeus.messaging.processors

import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.logging.PleoLogger
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
    private val invoiceProvider: InvoiceProvider,
    private val logger : PleoLogger
) {
    private val consumerTag = "CustomerToInvoiceConsumer"


    fun start() {
        var queueInfo = QueueInfo(
            conf.connInfo,
            conf.invoicesExchange,
            conf.exchangeType,
            conf.pendingInvoicesQueue,
            conf.exchangeType,
            conf.routingKey)

        messagePublisher.connect(queueInfo)
        messageConsumer.subscribe(
            conf.connInfo,
            conf.customersToInvoiceQueue,
            consumerTag, ::onDeliverAction, ::onCancelAction
        )
    }

    private fun onDeliverAction(consumerTag: String?, message: String) {
        try {
            var queueInfo = QueueInfo(
                conf.connInfo,
                conf.invoicesExchange,
                conf.exchangeType,
                conf.pendingInvoicesQueue,
                conf.exchangeType,
                conf.routingKey)


            val customerToInvoice = Json.decodeFromString<CustomerToInvoiceMsg>(message)
            println("['$consumerTag'] Processing invoices for customer ${customerToInvoice.customerId}")
            // TODO use status in msg
            invoiceProvider.fetchByStatus(customerToInvoice.customerId, customerToInvoice.statusToProcess)
                .map { invoice -> invoice.id }
                .chunked(InvoiceInfoMsg.maxMsgSize)
                .map { invoiceList ->
                    val msg = Json.encodeToString(InvoiceInfoMsg(invoiceList))
                    messagePublisher.publish(msg, queueInfo)
                }
        }
        catch (e : Exception)
        {
            logger.logError("${this.javaClass.name} Unable to process message. ${e.message}")
        }
    }

    private fun onCancelAction(consumerTag: String?) {
        println("Cancelling: '$consumerTag'")
    }
}