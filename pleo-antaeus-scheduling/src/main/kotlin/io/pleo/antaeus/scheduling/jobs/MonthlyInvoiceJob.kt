package io.pleo.antaeus.scheduling.jobs

import org.quartz.Job
import org.quartz.JobExecutionContext
import io.pleo.antaeus.factories.ServiceFactory
import io.pleo.antaeus.logging.LoggerFactory
import io.pleo.antaeus.messaging.QueueInfo
import io.pleo.antaeus.messaging.MessagingConfiguration
import io.pleo.antaeus.messaging.MessagingFactory
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

        var logger = LoggerFactory.getLogger()
        try {
            val billingService = ServiceFactory.getBillingProvider()
            var messagingConfiguration = MessagingConfiguration()

            var connInfo = QueueInfo(
                messagingConfiguration.connInfo,
                messagingConfiguration.customersExchange,
                messagingConfiguration.exchangeType,
                messagingConfiguration.customersToInvoiceQueue,
                messagingConfiguration.exchangeType,
                messagingConfiguration.routingKey)

            val publisher = MessagingFactory.getMessagePublisher()
            publisher.connect(connInfo)
            runBlocking {
                val customers = billingService.getCustomersToBillMonthly()
                customers.map { customer ->
                    val msg = Json.encodeToString(CustomerToInvoiceMsg(customer.id, InvoiceStatus.PENDING))
                    publisher.publish(msg, connInfo)
                }
            }
        }
        catch (e : Exception)
        {
            logger.logError("Unable to execute ${this.javaClass.name}. ${e.message}")
        }
    }
}