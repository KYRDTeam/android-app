package com.kyberswap.android.presentation.setting

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andrognito.pinlockview.PinLockListener
import com.github.pwittchen.rxbiometric.library.RxBiometric
import com.github.pwittchen.rxbiometric.library.throwable.AuthenticationError
import com.github.pwittchen.rxbiometric.library.throwable.AuthenticationFail
import com.github.pwittchen.rxbiometric.library.throwable.AuthenticationHelp
import com.github.pwittchen.rxbiometric.library.throwable.BiometricNotSupported
import com.github.pwittchen.rxbiometric.library.validation.RxPreconditions
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityPassCodeLockBinding
import com.kyberswap.android.domain.model.PassCode
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.AlertWithoutIconActivity
import com.kyberswap.android.presentation.common.BIOMETRIC_ERROR_CANCEL
import com.kyberswap.android.presentation.common.BIOMETRIC_ERROR_NONE_ENROLLED
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PassCodeLockActivity : BaseActivity() {

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

    private var disposable: Disposable? = null

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
                        disposeObserver()
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
                                disposeObserver()
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

        binding.imgFingerPrint.setOnClickListener {
            showFingerPrint()
        }
    }

    private fun showFingerPrint() {

        disposable =
            RxPreconditions
                .hasBiometricSupport(this)
                .flatMapCompletable {
                    if (!it) {
                        binding.imgFingerPrint.visibility = View.GONE
                        Completable.error(BiometricNotSupported())
                    } else {
                        binding.imgFingerPrint.visibility = View.VISIBLE
                        RxBiometric
                            .title(getString(R.string.verify_your_identity))
                            .description(getString(R.string.confirm_your_fingerpint_to_continue))
                            .negativeButtonText(getString(R.string.finger_print_cancel))
                            .negativeButtonListener(DialogInterface.OnClickListener { _, _ ->

                            })
                            .executor(ActivityCompat.getMainExecutor(this))
                            .build()
                            .authenticate(this)
                    }

                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        (applicationContext as KyberSwapApplication).startCounter()
                        finish()
                    },
                    onError = {
                        when (it) {
                            is AuthenticationError -> {
                                when (it.errorCode) {
                                    BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                        binding.imgFingerPrint.visibility = View.GONE
                                    }
                                    BIOMETRIC_ERROR_CANCEL -> {

                                    }
                                    else -> {
                                        it.errorMessage?.let { err ->
                                            showMessage(err.toString())
                                        }
                                    }
                                }
                            }

                            is AuthenticationFail -> {
                                showMessage(it.localizedMessage)
                            }

                            is AuthenticationHelp -> {

                            }

                            is BiometricNotSupported -> {

                            }

                            else -> {
                                showMessage(it.localizedMessage)
                            }
                        }
                    }
                )
    }

    override fun onPause() {
        super.onPause()
        disposeObserver()
    }

    private fun disposeObserver() {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
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


    override fun onBackPressed() {
        if (!isChangePinCode) {
            finishAffinity()
        }
        super.onBackPressed()
    }

    private fun showMessage(message: String) {
        val intent = AlertWithoutIconActivity.newIntent(this, null, message)
        startActivityForResult(intent, BaseFragment.SHOW_ALERT)
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
        fun newIntent(context: Context, type: Int = PASS_CODE_LOCK_TYPE_VERIFY) =
            Intent(context, PassCodeLockActivity::class.java)
                .apply {
                    putExtra(TYPE_PARAM, type)
                }
    }
}
