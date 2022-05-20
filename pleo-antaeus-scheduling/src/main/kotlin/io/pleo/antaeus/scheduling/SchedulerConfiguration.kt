package io.pleo.antaeus.scheduling

class SchedulerConfiguration {
    val ordinaryInvoicingJobCronExpression : String = "0/5 * * * * ?"
    val urgentInvoicingJobCronExpression : String = "0/5 * * * * ?"
    val defaultersInvoicingJobCronExpression : String = "0/5 * * * * ?"
    val ordinaryBillingJobName : String = "OrdinaryBillingJob"
    val urgentBillingJobName : String = "UrgentBillingJob"
    val defaultersBillingJobName : String = "DefaultersBillingJob"
    val billingJobGroup : String  = "BillingJobs"
}