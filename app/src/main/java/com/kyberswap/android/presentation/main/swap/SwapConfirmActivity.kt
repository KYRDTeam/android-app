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
import com.kyberswap.android.presentation.common.PLATFORM_FEE_BPS
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.AMOUNT
import com.kyberswap.android.util.CURRENT_RATE
import com.kyberswap.android.util.ERROR_TEXT
import com.kyberswap.android.util.MIN_RATE
import com.kyberswap.android.util.SWAPCONFIRM_BROADCAST_FAILED
import com.kyberswap.android.util.SWAPCONFIRM_BROADCAST_SUCCESS
import com.kyberswap.android.util.SWAPCONFIRM_CANCEL
import com.kyberswap.android.util.SWAPCONFIRM_ERROR
import com.kyberswap.android.util.TOKEN_PAIR
import com.kyberswap.android.util.TX_FEE
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

    private var platformFee: Int = PLATFORM_FEE_BPS

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
        platformFee = intent.getIntExtra(PLATFORM_FEE, PLATFORM_FEE_BPS)
        wallet?.let {
            viewModel.getSwapData(it)
        }

        viewModel.getPlatformFee()

        viewModel.getSwapDataCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {
                        binding.swap = state.swap
                        viewModel.getGasLimit(wallet, binding.swap, platformFee)
                        viewModel.getGasPrice()
                        binding.executePendingBindings()
                    }
                    is GetSwapState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getPlatformFeeCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPlatformFeeState.Success -> {
                        if (state.platformFee.fee != platformFee) {
                            platformFee = state.platformFee.fee
                            viewModel.getGasLimit(wallet, binding.swap, platformFee)
                            viewModel.getGasPrice()
                        }
                    }
                    is GetPlatformFeeState.ShowError -> {

                    }
                }
            }
        })

        viewModel.swapTokenTransactionCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SwapTokenTransactionState.Loading)
                when (state) {
                    is SwapTokenTransactionState.Success -> {
                        firebaseAnalytics.logEvent(
                            SWAPCONFIRM_BROADCAST_SUCCESS, Bundle().createEvent(
                                listOf(
                                    TOKEN_PAIR,
                                    AMOUNT,
                                    CURRENT_RATE,
                                    MIN_RATE,
                                    TX_FEE
                                ), listOf(
                                    state.swap?.displayPair,
                                    state.swap?.sourceAmount,
                                    state.swap?.expectedRate,
                                    state.swap?.displayMinAcceptedRate,
                                    state.swap?.displayGasFee
                                )
                            )
                        )
                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        returnIntent.putExtra(HASH_PARAM, state.responseStatus?.hash)
                        finish()
                    }
                    is SwapTokenTransactionState.ShowError -> {
                        showErrorWithTime(
                            state.message ?: getString(R.string.something_wrong), 10
                        )
                        firebaseAnalytics.logEvent(
                            SWAPCONFIRM_BROADCAST_FAILED, Bundle().createEvent(
                                listOf(
                                    TOKEN_PAIR,
                                    AMOUNT,
                                    CURRENT_RATE,
                                    MIN_RATE,
                                    TX_FEE,
                                    ERROR_TEXT
                                ), listOf(
                                    state.swap?.displayPair,
                                    state.swap?.sourceAmount,
                                    state.swap?.expectedRate,
                                    state.swap?.displayMinAcceptedRate,
                                    state.swap?.displayGasFee,
                                    state.message
                                )
                            )
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
                            SWAPCONFIRM_ERROR, Bundle().createEvent(
                                ERROR_TEXT, state.message
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
                            SWAPCONFIRM_ERROR, Bundle().createEvent(
                                ERROR_TEXT, state.message
                            )
                        )
                    }
                }
            }
        })


        binding.imgBack.setOnClickListener {
            firebaseAnalytics.logEvent(
                SWAPCONFIRM_CANCEL, Bundle().createEvent(
                    listOf(
                        TOKEN_PAIR,
                        AMOUNT,
                        CURRENT_RATE,
                        MIN_RATE,
                        TX_FEE
                    ), listOf(
                        binding.swap?.displayPair,
                        binding.swap?.sourceAmount,
                        binding.swap?.expectedRate,
                        binding.swap?.displayMinAcceptedRate,
                        binding.swap?.displayGasFee
                    )
                )
            )
            onBackPressed()
        }

        binding.tvCancel.setOnClickListener {
            firebaseAnalytics.logEvent(
                SWAPCONFIRM_CANCEL, Bundle().createEvent(
                    listOf(
                        TOKEN_PAIR,
                        AMOUNT,
                        CURRENT_RATE,
                        MIN_RATE,
                        TX_FEE
                    ), listOf(
                        binding.swap?.displayPair,
                        binding.swap?.sourceAmount,
                        binding.swap?.expectedRate,
                        binding.swap?.displayMinAcceptedRate,
                        binding.swap?.displayGasFee
                    )
                )
            )
            onBackPressed()
        }

        binding.tvConfirm.setOnClickListener {
            viewModel.swap(wallet, binding.swap, platformFee)
        }
    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val PLATFORM_FEE = "platform_fee_param"
        fun newIntent(context: Context, wallet: Wallet?, platformFee: Int) =
            Intent(context, SwapConfirmActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
                putExtra(PLATFORM_FEE, platformFee)
            }
    }
}
