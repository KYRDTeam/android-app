package com.kyberswap.android.presentation.main.swap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityPromoSwapConfirmBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.PLATFORM_FEE_BPS
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import java.io.File
import javax.inject.Inject

class PromoSwapConfirmActivity : BaseActivity(), KeystoreStorage {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var wallet: Wallet? = null

    private var platformFee: Int = PLATFORM_FEE_BPS

    private var isReserveRouting: Boolean = false

    private val viewModel: SwapConfirmViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SwapConfirmViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityPromoSwapConfirmBinding>(
            this,
            R.layout.activity_promo_swap_confirm
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WalletManager.storage = this
        WalletManager.scanWallets()
        wallet = intent.getParcelableExtra(WALLET_PARAM)
        platformFee = intent.getIntExtra(PLATFORM_FEE, PLATFORM_FEE_BPS)
        isReserveRouting = intent.getBooleanExtra(RESERVE_ROUTING_PARAM, false)
        binding.isReserveRouting = isReserveRouting
        wallet?.let {
            viewModel.getSwapData(it)
            binding.expiredDate = it.expiredDatePromoCode
        }

        viewModel.getSwapDataCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {
                        binding.swap = state.swap
                        viewModel.getPlatformFee(state.swap)
                        binding.executePendingBindings()
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
                        returnIntent.putExtra(BaseFragment.HASH_PARAM, state.responseStatus?.hash)
                        finish()
                    }
                    is SwapTokenTransactionState.ShowError -> {
                        showErrorWithTime(
                            state.message ?: getString(R.string.something_wrong), 10
                        )
                    }
                }
            }
        })

        viewModel.getPlatformFeeCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPlatformFeeState.Success -> {
                        platformFee = state.platformFee.fee
                    }
                    is GetPlatformFeeState.ShowError -> {

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

        binding.tvGasFee.setOnClickListener {
            dialogHelper.showBottomSheetGasFeeDialog()
        }

        binding.tvConfirm.setOnClickListener {
            viewModel.swap(wallet, binding.swap, platformFee, isReserveRouting)
        }
    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val PLATFORM_FEE = "platform_fee_param"
        private const val RESERVE_ROUTING_PARAM = "reserve_routing_param"
        fun newIntent(
            context: Context,
            wallet: Wallet?,
            platformFee: Int,
            isReserveRouting: Boolean
        ) =
            Intent(context, PromoSwapConfirmActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
                putExtra(PLATFORM_FEE, platformFee)
                putExtra(RESERVE_ROUTING_PARAM, isReserveRouting)
            }
    }
}
