package com.kyberswap.android.presentation.setting

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andrognito.pinlockview.PinLockListener
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityPassCodeLockBinding
import com.kyberswap.android.domain.model.PassCode
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.common.DEFAULT_KEY_NAME
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.setting.fingerprint.FingerprintAuthenticationDialogFragment
import com.kyberswap.android.util.di.ViewModelFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.inject.Inject


class PassCodeLockActivity : BaseActivity(), FingerprintAuthenticationDialogFragment.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(PassCodeLockViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityPassCodeLockBinding>(
            this,
            R.layout.activity_pass_code_lock
        )
    }

    private val newPinTitle by lazy {
        getString(R.string.pl_set_new_pin)
    }

    private val newPinContent by lazy {
        getString(R.string.pl_access_wallet)
    }

    private val repeatTitle by lazy {
        getString(R.string.pl_repeat_title)
    }

    private val repeatContent by lazy {
        getString(R.string.pl_repeat_content)
    }

    private val verifyAccess by lazy {
        getString(R.string.pl_verify_access)
    }

    private val isChangePinCode: Boolean
        get() = PASS_CODE_LOCK_TYPE_CHANGE == type

    private var remainNum = MAX_NUMBER_INPUT

    private var passCode: PassCode? = null

    var type: Int = PASS_CODE_LOCK_TYPE_VERIFY

    private var currentTimePassed: Long = 0

    private lateinit var keyStore: KeyStore

    private lateinit var keyGenerator: KeyGenerator

    private var currentPin: String? = null

    private val isVerifyAccess: Boolean
        get() = binding.title == verifyAccess

    private val isRepeatTitle: Boolean
        get() = binding.title == repeatTitle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(TYPE_PARAM, PASS_CODE_LOCK_TYPE_VERIFY)
        viewModel.getPin()
        binding.pinLockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinLockView.setPinLockListener(object : PinLockListener {
            override fun onComplete(pin: String) {
                if (isVerifyAccess) {
                    viewModel.verifyPin(pin, remainNum, System.currentTimeMillis())
                } else if (isRepeatTitle) {
                    if (pin == currentPin) {
                        currentPin = pin
                        viewModel.save(pin)
                    } else {
                        showAlertWithoutIcon(
                            title = getString(R.string.title_error), message = getString(
                                R.string.pin_confirm_unmatch
                            )
                        )
                        binding.pinLockView.resetPinLockView()
                        binding.title = newPinTitle
                        binding.content = newPinContent
                    }
                } else {
                    currentPin = pin
                    binding.pinLockView.resetPinLockView()
                    binding.title = repeatTitle
                    binding.content = repeatContent
                }
            }

            override fun onEmpty() {
            }

            override fun onPinChange(pinLength: Int, intermediatePin: String) {
            }
        })

        binding.pinLockView.pinLength = 6

        viewModel.savePinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SavePinState.Loading)
                when (state) {
                    is SavePinState.Success -> {
                        (applicationContext as KyberSwapApplication).startCounter()
                        finish()
                    }
                    is SavePinState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.verifyPinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == VerifyPinState.Loading)
                when (state) {
                    is VerifyPinState.Success -> {
                        if (state.verifyStatus.success) {
                            if (type == PASS_CODE_LOCK_TYPE_CHANGE) {
                                type = PASS_CODE_LOCK_TYPE_VERIFY
                                binding.pinLockView.resetPinLockView()
                                binding.title = newPinTitle
                                binding.content = newPinContent
                                binding.executePendingBindings()
                            } else {
                                (applicationContext as KyberSwapApplication).startCounter()
                                finish()
                            }
                        } else {
                            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                            binding.indicatorDots.startAnimation(shakeAnimation)

                            remainNum -= 1
                            if (remainNum > 0) {
                                binding.content =
                                    String.format(getString(R.string.number_of_attempt), remainNum)
                            } else {
                                binding.content = ""
                                startCounter()
                            }
                            binding.pinLockView.resetPinLockView()
                        }
                    }
                    is VerifyPinState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                        binding.title = newPinTitle
                        binding.content = newPinContent
                    }
                }
            }
        })

        viewModel.getPinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetPinState.Loading)
                when (state) {
                    is GetPinState.Success -> {
                        if (passCode != state.passCode) {
                            passCode = state.passCode
                            setupInitialView()
                            if (state.passCode.digest.isEmpty()) {
                                binding.title = newPinTitle
                                binding.content = newPinContent
                                binding.executePendingBindings()
                            } else {
                                binding.title = verifyAccess
                                showFingerPrint()
                            }
                        }
                    }
                    is GetPinState.ShowError -> {
                        binding.title = newPinTitle
                        binding.content = newPinContent
                    }
                }
            }
        })
    }

    private fun showFingerPrint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isChangePinCode) {
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            val fingerprintManager = getSystemService(FingerprintManager::class.java)
            if (keyguardManager?.isKeyguardSecure == true &&
                fingerprintManager?.isHardwareDetected == true &&
                fingerprintManager.hasEnrolledFingerprints()
            ) {
                setupKeyStoreAndKeyGenerator()
                createKey(DEFAULT_KEY_NAME)
                val cipher = setupCiphers()
                val fragment = FingerprintAuthenticationDialogFragment()
                fragment.setCryptoObject(FingerprintManager.CryptoObject(cipher))
                fragment.setCallback(this)
                if (initCipher(cipher, DEFAULT_KEY_NAME)) {
                    fragment.show(supportFragmentManager, DIALOG_FRAGMENT_TAG)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupCiphers(): Cipher {
        val defaultCipher: Cipher
        try {
            val cipherString =
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            defaultCipher = Cipher.getInstance(cipherString)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchPaddingException ->
                    throw RuntimeException("Failed to get an instance of Cipher", e)
                else -> throw e
            }
        }
        return defaultCipher
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun initCipher(cipher: Cipher, keyName: String): Boolean {
        try {
            keyStore.load(null)
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(keyName, null) as SecretKey)
            return true
        } catch (e: Exception) {
            when (e) {
                is KeyPermanentlyInvalidatedException -> return false
                is KeyStoreException,
                is CertificateException,
                is UnrecoverableKeyException,
                is IOException,
                is NoSuchAlgorithmException,
                is InvalidKeyException -> throw RuntimeException("Failed to init Cipher", e)
                else -> throw e
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupKeyStoreAndKeyGenerator() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to get an instance of KeyStore", e)
        }

        try {
            keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is NoSuchProviderException ->
                    throw RuntimeException("Failed to get an instance of KeyGenerator", e)
                else -> throw e
            }
        }
    }

    private fun startCounter() {
        binding.pinLockView.enableInput(false)
        viewModel.compositeDisposable.clear()
        viewModel.compositeDisposable.add(
            Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { interval ->
                    val currentWaitingTime = 60 - interval - currentTimePassed
                    if (currentWaitingTime > 0) {
                        binding.content =
                            String.format(
                                getString(R.string.too_many_attempt),
                                currentWaitingTime.toString()
                            )
                    } else {
                        remainNum = MAX_NUMBER_INPUT
                        currentTimePassed = 0
                        binding.pinLockView.enableInput(true)
                        binding.content = ""
                        viewModel.compositeDisposable.clear()
                    }
                }
        )
    }

    private fun setupInitialView() {
        passCode?.let {
            if (it.remainNum > 1) {
                remainNum = it.remainNum - 1
                binding.content = String.format(getString(R.string.number_of_attempt), remainNum)
            } else if (it.remainNum > 0) {
                if (it.time > 0) {
                    currentTimePassed = (System.currentTimeMillis() - it.time) / 1000
                    startCounter()
                }
            }
        }
    }

    override fun onAuthSuccess() {
        (applicationContext as KyberSwapApplication).startCounter()
        finish()
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean) {
        try {
            keyStore.load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val builder = KeyGenParameterSpec.Builder(keyName, keyProperties)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)

            keyGenerator.run {
                init(builder.build())
                generateKey()
            }
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is InvalidAlgorithmParameterException,
                is CertificateException,
                is IOException -> throw RuntimeException(e)
                else -> throw e
            }
        }
    }

    override fun onBackPressed() {
        if (!isChangePinCode) {
            finishAffinity()
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        viewModel.onCleared()
        super.onDestroy()
    }

    companion object {
        private const val MAX_NUMBER_INPUT = 5
        const val PASS_CODE_LOCK_TYPE_VERIFY = 0
        const val PASS_CODE_LOCK_TYPE_CHANGE = 1
        private const val TYPE_PARAM = "type_param"
        private const val DIALOG_FRAGMENT_TAG = "fingerprint_fragment"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        fun newIntent(context: Context, type: Int = PASS_CODE_LOCK_TYPE_VERIFY) =
            Intent(context, PassCodeLockActivity::class.java)
                .apply {
                    putExtra(TYPE_PARAM, type)
                }
    }
}
