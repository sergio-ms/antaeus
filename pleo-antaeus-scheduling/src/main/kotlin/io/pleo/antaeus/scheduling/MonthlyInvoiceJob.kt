package io.pleo.antaeus.scheduling

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

        var customerService = ServiceFactory.getCustomerProvider()

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

        var customers = customerService.fetchAll()
        customers.map { customer ->
            val msg = Json.encodeToString(customer.id)
            publisher.publish(msg)
        }
    }
}