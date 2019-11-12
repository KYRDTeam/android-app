package com.kyberswap.android.presentation.main.balance.send

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivitySendConfirmBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.common.CustomAlertActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.TransferTokenTransactionState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.isNetworkAvailable
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import java.io.File
import javax.inject.Inject


class SendConfirmActivity : BaseActivity(), KeystoreStorage {
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var isContactExist: Boolean = false

    private val viewModel: SendConfirmViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SendConfirmViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivitySendConfirmBinding>(
            this,
            R.layout.activity_send_confirm
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WalletManager.storage = this
        WalletManager.scanWallets()
        wallet = intent.getParcelableExtra(WALLET_PARAM)
        isContactExist = intent.getBooleanExtra(CONTACT_EXIST_PARAM, false)
        binding.isContactExist = isContactExist
        wallet?.let {
            viewModel.getSendData(it)
        }


        viewModel.getSendCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSendState.Success -> {
                        if (binding.send != state.send) {
                            binding.send = state.send
                            viewModel.getGasLimit(binding.send, wallet)
                            viewModel.getGasPrice()
                        }
                    }
                    is GetSendState.ShowError -> {

                    }
                }
            }
        })

        viewModel.transferTokenTransactionCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == TransferTokenTransactionState.Loading)
                when (state) {
                    is TransferTokenTransactionState.Success -> {
                        showBroadcastAlert(
                            CustomAlertActivity.DIALOG_TYPE_BROADCASTED,
                            Transaction(
                                type = Transaction.TransactionType.SEND,
                                hash = state.responseStatus.hash
                            )
                        )
                        onBackPressed()
                    }
                    is TransferTokenTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                        onBackPressed()
                    }
                }
            }
        })

        viewModel.getGetGasLimitCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasLimitState.Success -> {

                        if (state.gasLimit.toString() != binding.send?.gasLimit) {
                            val send = binding.send?.copy(
                                gasLimit = state.gasLimit.toString()
                            )

                            binding.send = send
                        }
                    }
                    is GetGasLimitState.ShowError -> {
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
                    }
                }
            }
        })

        viewModel.getGetGasPriceCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        if (state.gas != binding.send?.gas) {
                            val send = binding.send?.copy(
                                gas = state.gas
                            )
                            binding.send = send
                        }
                    }
                    is GetGasPriceState.ShowError -> {
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
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
            viewModel.send(wallet, binding.send)
        }
    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val CONTACT_EXIST_PARAM = "contact_exist_param"
        fun newIntent(context: Context, wallet: Wallet?, isContactExist: Boolean) =
            Intent(context, SendConfirmActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
                putExtra(CONTACT_EXIST_PARAM, isContactExist)
            }
    }
}
