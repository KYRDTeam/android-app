package com.kyberswap.android.presentation.main.swap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivitySwapConfirmBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.base.BaseFragment.Companion.HASH_PARAM
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.GET_GAS_LIMIT_ERROR
import com.kyberswap.android.util.GET_GAS_PRICE_ERROR
import com.kyberswap.android.util.SW_BROADCAST_ERROR
import com.kyberswap.android.util.SW_CONFIRMED_ERROR
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import java.io.File
import javax.inject.Inject


class SwapConfirmActivity : BaseActivity(), KeystoreStorage {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private var wallet: Wallet? = null

    private val viewModel: SwapConfirmViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SwapConfirmViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivitySwapConfirmBinding>(
            this,
            R.layout.activity_swap_confirm
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WalletManager.storage = this
        WalletManager.scanWallets()
        wallet = intent.getParcelableExtra(WALLET_PARAM)
        wallet?.let {
            viewModel.getSwapData(it)
        }

        viewModel.getSwapDataCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {
                        binding.swap = state.swap
                        viewModel.getGasLimit(wallet, binding.swap)
                        viewModel.getGasPrice()
                    }
                    is GetSwapState.ShowError -> {

                    }
                }
            }
        })

        viewModel.swapTokenTransactionCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SwapTokenTransactionState.Loading)
                when (state) {
                    is SwapTokenTransactionState.Success -> {
                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        returnIntent.putExtra(HASH_PARAM, state.responseStatus?.hash)
                        finish()
                    }
                    is SwapTokenTransactionState.ShowError -> {
                        firebaseAnalytics.logEvent(
                            SW_CONFIRMED_ERROR, Bundle().createEvent(
                                SW_BROADCAST_ERROR, state.message
                            )
                        )
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.getGetGasLimitCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasLimitState.Success -> {
                        if (state.gasLimit.toString() != binding.swap?.gasLimit) {
                            val swap = binding.swap?.copy(
                                gasLimit = state.gasLimit.toString()
                            )


                            if (swap != binding.swap) {
                                binding.swap = swap
                                binding.executePendingBindings()
                            }
                        }
                    }
                    is GetGasLimitState.ShowError -> {
                        firebaseAnalytics.logEvent(
                            SW_CONFIRMED_ERROR, Bundle().createEvent(
                                GET_GAS_LIMIT_ERROR, state.message
                            )
                        )
                    }
                }
            }
        })

        viewModel.getGetGasPriceCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        if (state.gas != binding.swap?.gas) {
                            val swap = binding.swap?.copy(
                                gas = if (wallet?.isPromo == true) state.gas.toPromoGas() else state.gas
                            )
                            if (swap != binding.swap) {
                                binding.swap = swap
                                binding.executePendingBindings()
                            }
                        }
                    }
                    is GetGasPriceState.ShowError -> {
                        firebaseAnalytics.logEvent(
                            SW_CONFIRMED_ERROR, Bundle().createEvent(
                                GET_GAS_PRICE_ERROR, state.message
                            )
                        )
                    }
                }
            }
        })


        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvCancel.setOnClickListener {
            onBackPressed()
        }

        binding.tvConfirm.setOnClickListener {
            viewModel.swap(wallet, binding.swap)
        }
    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newIntent(context: Context, wallet: Wallet?) =
            Intent(context, SwapConfirmActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
            }
    }
}
