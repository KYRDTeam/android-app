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
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.TransferTokenTransactionState
import com.kyberswap.android.util.di.ViewModelFactory
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
        wallet?.apply {
            viewModel.getSendData(this.address)



        viewModel.getSendCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSendState.Success -> {
                        binding.send = state.send
            
                    is GetSendState.ShowError -> {

            
        
    
)

        viewModel.transferTokenTransactionCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == TransferTokenTransactionState.Loading)
                when (state) {
                    is TransferTokenTransactionState.Success -> {

                        showAlert(
                            String.format(
                                getString(R.string.payment_send_success),
                                binding.send?.contact?.name
                            )
                        )
                        onBackPressed()
            
                    is TransferTokenTransactionState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)


        binding.imgBack.setOnClickListener {
            onBackPressed()


        binding.tvCancel.setOnClickListener {
            onBackPressed()


        binding.tvConfirm.setOnClickListener {
            viewModel.send(wallet, binding.send)

    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newIntent(context: Context, wallet: Wallet?) =
            Intent(context, SendConfirmActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
    
    }
}
