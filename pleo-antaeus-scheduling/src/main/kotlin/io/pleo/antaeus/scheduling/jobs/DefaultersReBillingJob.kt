package io.pleo.antaeus.scheduling.jobs

import org.quartz.Job
import org.quartz.JobExecutionContext
import io.pleo.antaeus.factories.ServiceFactory
import io.pleo.antaeus.logging.LoggerFactory
import io.pleo.antaeus.messaging.QueueInfo
import io.pleo.antaeus.messaging.MessagingConfiguration
import io.pleo.antaeus.messaging.MessagingFactory
import io.pleo.antaeus.messaging.messages.CustomerToInvoiceMsg
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DefaultersReBillingJob() : Job {

    /***
     * This scheduler is in charge of triggering the payment operation defaulter
     * customers that didn't have enough cash for the previous billing operation
     */
    override fun execute(context: JobExecutionContext?) {

        var logger = LoggerFactory.getLogger()
        try {
            val billingService = ServiceFactory.getBillingProvider()
            var messagingConfiguration = MessagingFactory.getMessageConfiguration()

            var connInfo = QueueInfo(
                messagingConfiguration.customersExchange,
                messagingConfiguration.exchangeType,
                messagingConfiguration.customersToInvoiceQueue,
                messagingConfiguration.exchangeType,
                messagingConfiguration.routingKey)

            val publisher = MessagingFactory.getMessagePublisher()
            runBlocking {
                val customers = billingService.getDefaulterCustomers()
                customers.map { customer ->
                    val msg = Json.encodeToString(CustomerToInvoiceMsg(customer.id,
                        billingService.getStatusForDefaulters()))
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