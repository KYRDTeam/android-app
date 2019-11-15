package com.kyberswap.android.presentation.main.swap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityPromoSwapConfirmBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.common.CustomAlertActivity
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

    private var wallet: Wallet? = null

    private val viewModel: SwapConfirmViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SwapConfirmViewModel::class.java)
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
        wallet?.let {
            viewModel.getSwapData(it)
            binding.expiredDate = it.expiredDatePromoCode
        }

        viewModel.getSwapDataCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {
                        binding.swap = state.swap
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
//                        showAlert(getString(R.string.transaction_broadcasted_message))
                        showBroadcastAlert(
                            CustomAlertActivity.DIALOG_TYPE_BROADCASTED,
                            Transaction(
                                type = Transaction.TransactionType.SWAP,
                                hash = state.responseStatus?.hash ?: ""
                            )
                        )
                        onBackPressed()
                    }
                    is SwapTokenTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
            Intent(context, PromoSwapConfirmActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
            }
    }
}
