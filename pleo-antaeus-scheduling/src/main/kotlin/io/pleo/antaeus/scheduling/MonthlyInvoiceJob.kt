package io.pleo.antaeus.scheduling

import org.quartz.Job
import org.quartz.JobExecutionContext
import io.pleo.antaeus.factories.ServiceFactory
import io.pleo.antaeus.messaging.ExchangeNames
import io.pleo.antaeus.messaging.MessagePublisher
import io.pleo.antaeus.messaging.QueueNames
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MonthlyInvoiceJob() : Job {

    override fun execute(context: JobExecutionContext?) {

        var customerService = ServiceFactory.GetCustomerService()
        val publisher = MessagePublisher()
        var customers = customerService.fetchAll()
        customers.map { customer ->
            val msg = Json.encodeToString(customer.id)
            publisher.publish(ExchangeNames.customersExchange, QueueNames.customersToInvoiceQueue, msg)
        }
    }
}