package io.pleo.antaeus.scheduling

class SchedulerConfiguration {
    //Cron expression format: second, minute, hour, day of month, month, day(s) of week


    val ordinaryBillingJobCronExpression : String = "0 0 0 1 * ?"   // -> First of every month at midnight
    val urgentBillingJobCronExpression : String = "0 0 * * * ?"     // -> Every hour o'clock
    val defaultersBillingJobCronExpression : String = "0 0 0 * * ?" // -> Every day at midnight


    // Debug
    //val ordinaryBillingJobCronExpression : String = "0/10 * * * * ?"
    //val urgentBillingJobCronExpression : String = "0/10 * * * * ?"
    //val defaultersBillingJobCronExpression : String = "0/10 * * * * ?"


    val ordinaryBillingJobName : String = "OrdinaryBillingJob"
    val urgentBillingJobName : String = "UrgentBillingJob"
    val defaultersBillingJobName : String = "DefaultersBillingJob"
    val billingJobGroup : String  = "BillingJobs"
}