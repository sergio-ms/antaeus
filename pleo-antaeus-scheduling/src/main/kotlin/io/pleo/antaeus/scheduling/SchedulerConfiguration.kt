package io.pleo.antaeus.scheduling

class SchedulerConfiguration {
    val monthlyInvoiceJobCronExpression : String = "0 * * * * ?"
    val monthlyInvoiceJobName : String = "MonthlyInvoiceJob"
    val monthlyInvoiceJobGroup : String  = "invoiceJobs"
}