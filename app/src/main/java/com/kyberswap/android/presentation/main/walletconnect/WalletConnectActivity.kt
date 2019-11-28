package com.kyberswap.android.presentation.main.walletconnect

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityWalletConnectBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class WalletConnectActivity : BaseActivity() {
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var connectionInfo: String? = null

    private val viewModel: WalletConnectViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(WalletConnectViewModel::class.java)
    }

    private var requestCode: Int = 0

    private lateinit var handler: Handler
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityWalletConnectBinding>(
            this,
            R.layout.activity_wallet_connect
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = intent.getParcelableExtra(WALLET_PARAM)
        connectionInfo = intent.getStringExtra(CONTENT_PARAM)

        handler = Handler()

        binding.imgBack.setOnClickListener {
            requestCode = REQUEST_BACK
            viewModel.killSession()
        }

        binding.imgWalletConnect.setOnClickListener {
            requestCode = REQUEST_SCAN
            viewModel.killSession()
        }

        handleWalletConnect()

        viewModel.requestConnectCallback.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->

                when (state) {
                    is RequestState.Loading -> {
                        showProgress(true)
                    }
                    is RequestState.Success -> {

                    }
                    is RequestState.ShowError -> {
                        showProgress(false)
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.approveSessionCallback.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SessionRequestState.Loading)
                when (state) {
                    is SessionRequestState.Success -> {
                        binding.lnContent.visibility = View.VISIBLE
                        Glide.with(this).load(state.meta.icons.first())
                            .into(binding.imgConnectedTo)
                        binding.tvTitle.text = state.meta.name
                        binding.tvConnectedTo.text = state.meta.url
                        binding.tvAddress.text = wallet?.address
                    }
                    is SessionRequestState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })


        viewModel.decodeTransactionCallback.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is DecodeTransactionState.Success -> {
                        val transaction = state.transaction
                        val approvedTransactionDialog = AlertDialog.Builder(this)
                            .setTitle("Approve Transaction")
                            .setMessage((if (transaction.isSwap) "Swap " else "Transfer ") + transaction.displayWalletConnectTransaction)
                            .setPositiveButton(
                                "Approve"
                            ) { _, _ ->
                                viewModel.sendTransaction(state.id, state.wcTransaction, wallet!!)

                            }
                            .setNegativeButton("Reject") { dialogInterface, _ ->
                                dialogInterface.dismiss()
                                viewModel.rejectTransaction(state.id)

                            }
                            .create()
                        approvedTransactionDialog.show()
                    }
                    is DecodeTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })


        viewModel.killSessionCallback.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is RequestState.Success -> {
                        if (requestCode == REQUEST_SCAN) {
                            IntentIntegrator(this)
                                .setBeepEnabled(false)
                                .initiateScan()
                        }
                    }
                    is RequestState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private fun handleWalletConnect() {
        wallet?.address?.let { walletAddress ->
            connectionInfo?.let { info ->
                viewModel.connect(walletAddress, info, { id, meta ->
                    handler.post {

                        showProgress(false)
                        val approveSessionDialog = AlertDialog.Builder(this)
                            .setTitle(meta.name)
                            .setMessage(meta.url)
                            .setPositiveButton("Approve") { _, _ ->
                                viewModel.approveSession(walletAddress, meta)

                            }
                            .setNegativeButton("Reject") { dialog, _ ->
                                viewModel.rejectSession()
                                dialog.dismiss()

                            }
                            .create()
                        approveSessionDialog.show()
                    }

                }, { id, transaction ->
                    viewModel.decodeTransaction(id, transaction, wallet!!)
                }, { id, signedMessage ->

                    handler.post {
                        val test = AlertDialog.Builder(this)
                            .setTitle("Sign")
                            .setMessage("sign transaction")
                            .setPositiveButton(
                                R.string.ok
                            ) { _, _ ->
                                viewModel.sign(id, signedMessage, wallet!!)

                            }.create()
                        test.show()
                    }
                }, { _, _ ->
                    showProgress(false)
                    if (requestCode != REQUEST_SCAN) {
                        handler.post { onBackPressed() }
                    }

                }, {
                    showProgress(false)
                    showError(it.message ?: "")
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null && scanResult.contents != null) {
            this.connectionInfo = scanResult.contents
            handleWalletConnect()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.killSession()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        viewModel.killSession()
        super.onDestroy()
    }


    companion object {
        private const val REQUEST_BACK = 1
        private const val REQUEST_SCAN = 2
        private const val WALLET_PARAM = "wallet_param"
        private const val CONTENT_PARAM = "content_param"
        fun newIntent(context: Context, wallet: Wallet?, content: String) =
            Intent(context, WalletConnectActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
                putExtra(CONTENT_PARAM, content)
            }
    }
}
