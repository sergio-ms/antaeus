package io.pleo.antaeus.messaging.consumers
import io.pleo.antaeus.messaging.MessageConsumer
import io.pleo.antaeus.messaging.QueueNames


class CustomerToInvoiceConsumer(private val messageConsumer : MessageConsumer, ) {
    val consumerTag = "CustomerToInvoiceConsumer"

    fun start()
    {
        messageConsumer.subscribe(QueueNames.customersToInvoiceQueue, consumerTag, ::onDeliverAction, ::onCancelAction)
    }

    private fun onDeliverAction(consumerTag : String?, message : String)
    {
        println("['$consumerTag'] Received message: '$message'")
    }


    private fun onCancelAction(consumerTag : String?)
    {
        println("Received message: '$consumerTag'")
    }
}