/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

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
import io.pleo.antaeus.scheduling.jobs.MonthlyInvoiceJob
import io.pleo.antaeus.scheduling.SchedulerConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import setupInitialData
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.util.*


fun main() {
//    val file = File("/var/www/html/config.properties")
//
//    val prop = Properties()
//    FileInputStream(file).use { prop.load(it) }

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

    // Cron Job for MonthlyInvoiceJob
    val schedConf = SchedulerConfiguration()
    var scheduler = CronScheduler(
        schedConf.monthlyInvoiceJobName,
        schedConf.monthlyInvoiceJobGroup,
        schedConf.monthlyInvoiceJobCronExpression,
        MonthlyInvoiceJob()
    )
    scheduler.start()

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


