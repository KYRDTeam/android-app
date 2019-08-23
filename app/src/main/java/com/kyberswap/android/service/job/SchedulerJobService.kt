package com.kyberswap.android.service.job

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SchedulerJobService @Inject constructor() :
    FrameworkJobSchedulerService() {

    @Inject
    lateinit var job: JobManager


    override fun getJobManager(): JobManager {
        return job
    }
}
