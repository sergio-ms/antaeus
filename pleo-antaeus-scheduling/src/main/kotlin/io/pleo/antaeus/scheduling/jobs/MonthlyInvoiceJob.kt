package io.pleo.antaeus.scheduling.jobs

import org.quartz.Job
import org.quartz.JobExecutionContext
import io.pleo.antaeus.factories.ServiceFactory
import io.pleo.antaeus.messaging.ConnectionInfo
import io.pleo.antaeus.messaging.MessagingConfiguration
import io.pleo.antaeus.messaging.RabbitMqMessagePublisher
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MonthlyInvoiceJob() : Job {

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

        val customers = billingService.GetCustomersToBillMontly()
        customers.map { customer ->
            val msg = Json.encodeToString(customer.id)
            publisher.publish(msg)
        }
    }
}