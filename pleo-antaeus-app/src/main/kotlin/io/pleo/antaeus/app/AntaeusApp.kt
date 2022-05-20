/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import io.pleo.antaeus.core.providers.BillingProvider
import io.pleo.antaeus.core.providers.InvoiceProvider
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.factories.ServiceFactory
import io.pleo.antaeus.logging.LoggerFactory
import io.pleo.antaeus.messaging.MessagingConfiguration
import io.pleo.antaeus.messaging.MessagingFactory
import io.pleo.antaeus.messaging.processors.CustomerToInvoiceProcessor
import io.pleo.antaeus.messaging.processors.PendingInvoiceProcessor
import io.pleo.antaeus.rest.AntaeusRest
import io.pleo.antaeus.scheduling.CronScheduler
import io.pleo.antaeus.scheduling.jobs.OrdinaryBillingJob
import io.pleo.antaeus.scheduling.SchedulerConfiguration
import io.pleo.antaeus.scheduling.jobs.DefaultersReBillingJob
import io.pleo.antaeus.scheduling.jobs.UrgentReBillingJob
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import setupInitialData
import java.io.File
import java.sql.Connection


fun main() {

    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    val dbFile: File = File.createTempFile("antaeus-db", ".sqlite")
    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
        .connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC",
            user = "root",
            password = ""
        )
        .also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            transaction(it) {
                addLogger(StdOutSqlLogger)
                // Drop all existing tables to ensure a clean slate on each run
                SchemaUtils.drop(*tables)
                // Create all tables
                SchemaUtils.create(*tables)
            }
        }

    // Set up data access layer.
    val dal = AntaeusDal(db = db)

    // Insert example data in the database.
    setupInitialData(dal = dal)

    // Factory required by the different jobs
    ServiceFactory.setDal(dal)

    // Create core services
    val invoiceService = ServiceFactory.getInvoiceProvider()
    val customerService = ServiceFactory.getCustomerProvider()
    val billingService = ServiceFactory.getBillingProvider()

    // Create REST web service
    AntaeusRest(
        invoiceService = invoiceService as InvoiceService,
        customerService = customerService
    ).run()

    startSchedulers()
    registerProcessors(invoiceService, billingService)
}

private fun registerProcessors(
    invoiceService: InvoiceProvider,
    billingService: BillingProvider
) {
    // Create CustomerToInvoiceProcessor processor
    CustomerToInvoiceProcessor(
        MessagingConfiguration(), MessagingFactory.getMessageConsumer(),
        MessagingFactory.getMessagePublisher(), invoiceService, LoggerFactory.getLogger()
    ).start()

    // Create CustomerToInvoiceProcessor processor
    PendingInvoiceProcessor(
        MessagingConfiguration(), MessagingFactory.getMessageConsumer(), billingService, LoggerFactory.getLogger()
    ).start()
}

private fun startSchedulers() {
    val conf = SchedulerConfiguration()
    // Start job for ordinary billing
    var ordinaryBillingJobScheduler = CronScheduler(
        conf.ordinaryBillingJobName,
        conf.billingJobGroup,
        conf.ordinaryInvoicingJobCronExpression,
        OrdinaryBillingJob()
    )
    ordinaryBillingJobScheduler.start()

    // Start job for urgent re-billing
    var urgentReBillingJobScheduler = CronScheduler(
        conf.urgentBillingJobName,
        conf.billingJobGroup,
        conf.urgentInvoicingJobCronExpression,
        UrgentReBillingJob()
    )
    urgentReBillingJobScheduler.start()

    // Start job for defaulters
    var defaultersReBillingJobScheduler = CronScheduler(
        conf.defaultersBillingJobName,
        conf.billingJobGroup,
        conf.defaultersInvoicingJobCronExpression,
        DefaultersReBillingJob()
    )
    defaultersReBillingJobScheduler.start()
}


