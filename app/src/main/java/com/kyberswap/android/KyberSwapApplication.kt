package com.kyberswap.android

import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadFactory
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.kyberswap.android.presentation.setting.PassCodeLockActivity
import com.kyberswap.android.util.di.AppComponent
import com.kyberswap.android.util.di.DaggerAppComponent
import com.kyberswap.android.util.di.module.DatabaseModule
import com.kyberswap.android.util.di.module.NetworkModule
import com.orhanobut.hawk.Hawk
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.concurrent.TimeUnit


class KyberSwapApplication : DaggerApplication(), LifecycleObserver {
    private lateinit var applicationComponent: AppComponent

    private val PREF_FILE_NAME = "kyber_swap_pref"
    private val TINK_KEYSET_NAME = "kyber_swap_keyset"
    private val MASTER_KEY_URI = "android-keystore://kyber_swap_master_key"
    lateinit var aead: Aead
    private val disposable = CompositeDisposable()
    private var counter = THRESHOLD_VALUE


    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())


        RxJavaPlugins.setErrorHandler { e ->
            e.printStackTrace()
            Timber.e(e.toString())


        Hawk.init(this).build()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/Montserrat-Medium.ttf")
                            .build()
                    )
                )
                .build()
        )

        try {
            AeadConfig.register()
            aead = AeadFactory.getPrimitive(getOrGenerateNewKeysetHandle())
 catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
 catch (e: IOException) {
            throw RuntimeException(e)


        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(
                TwitterAuthConfig(
                    resources.getString(R.string.twitter_consumer_key),
                    resources.getString(R.string.twitter_consumer_secret)
                )
            )
            .debug(true)
            .build()
        Twitter.initialize(config)

    }

    fun stopCounter() {
        disposable.clear()
    }

    fun startCounter() {
        disposable.clear()
        if (counter >= THRESHOLD_VALUE) {
            counter = 0

        disposable.add(
            Observable.interval(counter, 10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    counter = it
        , {
                    it.printStackTrace()
        )
        )

    }

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun getOrGenerateNewKeysetHandle(): KeysetHandle {
        return AndroidKeysetManager.Builder()
            .withSharedPref(applicationContext, TINK_KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        applicationComponent = DaggerAppComponent.builder()
            .application(this)
            .networkModule(NetworkModule())
            .databaseModule(DatabaseModule())
            .build()
        return applicationComponent
    }

    @Synchronized
    fun getComponent(): AppComponent {
        return applicationComponent
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroy() {
        disposable.clear()
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        if (counter >= THRESHOLD_VALUE) {
            startActivity(PassCodeLockActivity.newIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
    )

    }

    companion object {
        const val THRESHOLD_VALUE = 60L
    }
}
