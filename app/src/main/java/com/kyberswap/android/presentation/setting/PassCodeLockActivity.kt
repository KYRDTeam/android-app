package com.kyberswap.android.presentation.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andrognito.pinlockview.PinLockListener
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityPassCodeLockBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class PassCodeLockActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PassCodeLockViewModel::class.java)
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

    var type: Int = PASS_CODE_LOCK_TYPE_VERIFY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = intent.getIntExtra(TYPE_PARAM, PASS_CODE_LOCK_TYPE_VERIFY)


        binding.pinLockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinLockView.setPinLockListener(object : PinLockListener {
            override fun onComplete(pin: String) {
                if (binding.title == repeatTitle || binding.title == verifyAccess) {
                    viewModel.verifyPin(pin)
                } else {
                    viewModel.save(pin)
                }
            }

            override fun onEmpty() {
            }

            override fun onPinChange(pinLength: Int, intermediatePin: String) {
            }

        })

        viewModel.getPin()

        binding.pinLockView.pinLength = 6
        binding.pinLockView.enableLayoutShuffling()

        viewModel.savePinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SavePinState.Loading)
                when (state) {
                    is SavePinState.Success -> {
                        binding.pinLockView.resetPinLockView()
                        binding.title = repeatTitle
                        binding.content = repeatContent
                    }
                    is SavePinState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.verifyPinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == VerifyPinState.Loading)
                when (state) {
                    is VerifyPinState.Success -> {
                        (applicationContext as KyberSwapApplication).startCounter()
                        finish()
                    }
                    is VerifyPinState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
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
                        if (state.digest.isNullOrEmpty()) {
                            binding.title = newPinTitle
                            binding.content = newPinContent
                            binding.executePendingBindings()
                        } else {
                            binding.title = verifyAccess
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

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }

    companion object {
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
