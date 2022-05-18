package io.pleo.antaeus.scheduling.jobs

import org.quartz.Job
import org.quartz.JobExecutionContext
import io.pleo.antaeus.factories.ServiceFactory
import io.pleo.antaeus.messaging.ConnectionInfo
import io.pleo.antaeus.messaging.MessagingConfiguration
import io.pleo.antaeus.messaging.RabbitMqMessagePublisher
import io.pleo.antaeus.messaging.messages.CustomerToInvoiceMsg
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MonthlyInvoiceJob() : Job {

    /***
     * This scheduler is in charge of triggering the payment operation for all customers
     * that have at least one pending invoice on these invoices that are in PAY status only
     */
    override fun execute(context: JobExecutionContext?) {
        val billingService = ServiceFactory.getBillingProvider()

        // TODO get queueConf and publisher from a factory
        var messagingConfiguration = MessagingConfiguration()

        var connInfo = ConnectionInfo()
        connInfo.connectionName = messagingConfiguration.connectionName
        connInfo.exchange = messagingConfiguration.customersExchange
        connInfo.queue = messagingConfiguration.customersToInvoiceQueue
        connInfo.exchangeType = messagingConfiguration.exchangeType
        connInfo.routingKey = messagingConfiguration.routingKey

        val publisher = RabbitMqMessagePublisher()
        publisher.connect(connInfo)
        runBlocking {
            val customers = billingService.getCustomersToBillMonthly()
            customers.map { customer ->
                val msg = Json.encodeToString(CustomerToInvoiceMsg(customer.id, InvoiceStatus.PENDING))
                publisher.publish(msg, connInfo)
            }
        }
    }
}