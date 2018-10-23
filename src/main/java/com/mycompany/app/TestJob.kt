package com.mycompany.app

import de.spinscale.dropwizard.jobs.Job
import de.spinscale.dropwizard.jobs.annotations.Every
import org.quartz.JobExecutionContext

@Every("1s")
class TestJob : Job() {
    override fun doJob(context: JobExecutionContext?) {
        println("doing job!")
    }
}