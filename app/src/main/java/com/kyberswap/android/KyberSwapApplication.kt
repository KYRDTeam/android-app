package com.kyberswap.android

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
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.IOException
import java.security.GeneralSecurityException


class KyberSwapApplication : DaggerApplication(), LifecycleObserver {
    private lateinit var applicationComponent: AppComponent

    private val PREF_FILE_NAME = "kyber_swap_pref"
    private val TINK_KEYSET_NAME = "kyber_swap_keyset"
    private val MASTER_KEY_URI = "android-keystore://kyber_swap_master_key"
    lateinit var aead: Aead

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        RxJavaPlugins.setErrorHandler { e ->
            e.printStackTrace()
            Timber.e(e.toString())
        }

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
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

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

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
    }
}
