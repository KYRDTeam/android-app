package com.kyberswap.android

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ProcessLifecycleOwner
import com.kyberswap.android.util.di.DaggerAppComponent
import com.kyberswap.android.util.di.module.DatabaseModule
import com.kyberswap.android.util.di.module.NetworkModule
import com.orhanobut.hawk.Hawk
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber

class KyberSwapApplication : DaggerApplication(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        RxJavaPlugins.setErrorHandler { e -> Timber.e(e.toString()) }

        Hawk.init(this).build()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
            .application(this)
            .networkModule(NetworkModule())
            .databaseModule(DatabaseModule())
            .build()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
    }
}
