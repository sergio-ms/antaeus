package io.pleo.antaeus.scheduling

import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.JobBuilder.*
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.Job


class CronScheduler(private val jobName : String,
                    private val jobGroup : String,
                    private val cronExpression : String,
                    private val jobClass : Job) {
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()

    fun start()
    {
        scheduler.start()
        val job = newJob(jobClass.javaClass)
            .withIdentity(jobName, jobGroup)
            .build()

        val trigger: Trigger = newTrigger()
            .startNow()
            .withSchedule(
              //cronSchedule("0/30 * * * * ?")
              cronSchedule(cronExpression)
            )
            .build()
        scheduler.scheduleJob(job, trigger)
    }

    fun shutdown()
    {
        scheduler.shutdown(false)
    }
}