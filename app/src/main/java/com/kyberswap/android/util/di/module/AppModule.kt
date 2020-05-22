package com.kyberswap.android.util.di.module

import android.app.Application
import android.content.Context
import com.kyberswap.android.domain.AppSchedulerProvider
import com.kyberswap.android.domain.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Singleton
    @Provides
    @JvmStatic
    fun provideContext(application: Application): Context = application

    @Singleton
    @Provides
    @JvmStatic
    fun provideSchedulerProvider(): SchedulerProvider = AppSchedulerProvider()

//    @Provides
//    @Singleton
//    @JvmStatic
//    fun jobManager(application: Application): JobManager {
//        val config = Configuration.Builder(application)
//            .consumerKeepAlive(45)
//            .maxConsumerCount(3)
//            .minConsumerCount(1)
//            .injector { job ->
//                if (job is GetTokenWorker) {
//                    job.inject((application as KyberSwapApplication).getComponent())
//                }
//            }
//            .scheduler(
//                createSchedulerFor(
//                    application,
//                    SchedulerJobService::class.javaObjectType
//                )
//            )
//            .build()
//        return JobManager(config)
//    }

}
