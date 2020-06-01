package com.kyberswap.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.freshchat.consumer.sdk.Freshchat
import com.freshchat.consumer.sdk.FreshchatConfig
import com.freshchat.consumer.sdk.FreshchatNotificationConfig
import com.google.android.gms.tasks.OnCompleteListener
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadFactory
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.presentation.notification.NotificationOpenedHandler
import com.kyberswap.android.presentation.setting.PassCodeLockActivity
import com.kyberswap.android.util.di.AppComponent
import com.kyberswap.android.util.di.DaggerAppComponent
import com.kyberswap.android.util.di.module.DatabaseModule
import com.kyberswap.android.util.di.module.NetworkModule
import com.onesignal.OneSignal
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
import javax.inject.Inject


class KyberSwapApplication : DaggerApplication(), LifecycleObserver {
    private lateinit var applicationComponent: AppComponent

    private val PREF_FILE_NAME = "kyber_swap_pref"
    private val TINK_KEYSET_NAME = "kyber_swap_keyset"
    private val MASTER_KEY_URI = "android-keystore://kyber_swap_master_key"
    lateinit var aead: Aead
    private val disposable = CompositeDisposable()
    private var counter = THRESHOLD_VALUE

    private var freshchat: Freshchat? = null

    @Inject
    lateinit var mediator: StorageMediator

    private var _currentActivity: Activity? = null

    val currentActivity: Activity?
        get() = _currentActivity


    fun setCurrentActivity(activity: Activity) {
        this._currentActivity = activity
    }


    override fun onCreate() {
        super.onCreate()
        instance = this

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
                            .setDefaultFontPath("fonts/Roboto-Medium.ttf")
                            .build()
                    )
                )
                .build()
        )

        try {
            AeadConfig.register()
            aead = AeadFactory.getPrimitive(getOrGenerateNewKeySetHandle())
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

        initialiseFreshChat()

        OneSignal.startInit(this)
            .setNotificationOpenedHandler(NotificationOpenedHandler())
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .init()
        setupRemoteConfig()
    }

    fun stopCounter() {
        counter = 0
        disposable.clear()
    }

    private fun initialiseFreshChat() {
        val freshchatConfig = FreshchatConfig(
            getString(R.string.freshchat_app_id),
            getString(R.string.freshchat_app_key)
        )
        freshchatConfig.isCameraCaptureEnabled = true
        freshchatConfig.isGallerySelectionEnabled = true
        freshchatConfig.isResponseExpectationEnabled = false
        freshchatConfig.isUserEventsTrackingEnabled = false
        getFreshchatInstance(applicationContext)?.init(freshchatConfig)
        val notificationConfig = FreshchatNotificationConfig()
            .setNotificationSoundEnabled(true)
            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
            .setLargeIcon(R.drawable.ic_app_icon_rounded)
            .setImportance(NotificationManagerCompat.IMPORTANCE_MAX)
        getFreshchatInstance(applicationContext)?.setNotificationConfig(notificationConfig)

        if (!mediator.isInitialFreshChat()) {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    token?.let {
                        Freshchat.getInstance(this).setPushRegistrationToken(token)
                        Freshchat.getInstance(this).userIdTokenStatus.asInt()
                        mediator.setInitialFreshChat(true)
                    }

                })
        }
    }

    private fun getFreshchatInstance(context: Context): Freshchat? {
        if (freshchat == null) {
            freshchat = Freshchat.getInstance(context)
        }
        return freshchat
    }

    fun startCounter() {
        disposable.clear()
        if (counter >= THRESHOLD_VALUE) {
            counter = 0
        }
        disposable.add(
            Observable.interval(counter, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    counter = it
                }, {
                    it.printStackTrace()
                })
        )
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getOrGenerateNewKeySetHandle(): KeysetHandle {
        return AndroidKeysetManager.Builder()
            .withSharedPref(applicationContext, TINK_KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
    }

    private fun setupRemoteConfig() {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        firebaseRemoteConfig.fetch(3 * 60) // fetch every minutes
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseRemoteConfig.activateFetched()
                }
            }
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
        _currentActivity = null
        disposable.clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        if (counter >= THRESHOLD_VALUE) {
            startActivity(PassCodeLockActivity.newIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            })
        }
    }

    companion object {
        const val THRESHOLD_VALUE = 60L
        lateinit var instance: Context private set
    }
}
