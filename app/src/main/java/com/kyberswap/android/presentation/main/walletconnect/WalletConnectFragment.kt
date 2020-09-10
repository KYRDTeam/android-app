package com.kyberswap.android.presentation.main.walletconnect

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentWalletConnectBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletConnect
import com.kyberswap.android.domain.model.WcEthSign
import com.kyberswap.android.domain.model.WcSessionRequest
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.walletconnect.service.WcSessionManagerService
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.APPROVE_WALLET_CONNECT_ACTION
import com.kyberswap.android.util.APPROVE_WALLET_CONNECT_SESSION_ACTION
import com.kyberswap.android.util.APPROVE_WALLET_CONNECT_SIGN_ACTION
import com.kyberswap.android.util.DISCONNECT_WALLET_CONNECT_EVENT
import com.kyberswap.android.util.FAIL_WALLET_CONNECT_EVENT
import com.kyberswap.android.util.OPEN_SCAN_IN_WALLET_CONNECT_ACTION
import com.kyberswap.android.util.OPEN_WALLET_CONNECT_SCREEN_EVENT
import com.kyberswap.android.util.REJECT_WALLET_CONNECT_ACTION
import com.kyberswap.android.util.REJECT_WALLET_CONNECT_SESSION_ACTION
import com.kyberswap.android.util.REJECT_WALLET_CONNECT_SIGN_ACTION
import com.kyberswap.android.util.SWITCH_WALLET_WHEN_WALLET_CONNECT_EVENT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.isApproveTx
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import javax.inject.Inject

class WalletConnectFragment : BaseFragment() {

    private lateinit var binding: FragmentWalletConnectBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(WalletConnectViewModel::class.java)
    }

    private lateinit var handler: Handler

    private var connectionInfo: String? = null

    private var requestCode: Int = 0

    private var approvedTransactionDialog: AlertDialog? = null
    private var approveSessionDialog: AlertDialog? = null
    private var disConnectSessionDialog: AlertDialog? = null

    private val isTransactionRequest: Boolean
        get() = connectionInfo != null && connectionInfo?.contains(
            "bridge",
            true
        ) != true

    private val hasSession: Boolean
        get() = walletConnect?.hasSession == true

    private val isSessionRequest: Boolean
        get() = connectionInfo?.contains("bridge", true) == true
    private var isOnline = false

    private val hasDataPayload: Boolean
        get() = walletConnect?.wcEthSendTransaction != null || walletConnect?.wcEthSign != null

    private val hasSessionInfo: Boolean
        get() = walletConnect?.wcSessionRequest != null && walletConnect?.wcSessionRequest!!.status && walletConnect?.wcSessionRequest!!.meta.name.isNotBlank()

    private var walletConnect: WalletConnect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        connectionInfo = arguments?.getString(CONTENT_PARAM)
        walletConnect =
            WalletConnect(address = wallet?.address ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalletConnectBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun startConnectionService() {
        if (activity != null) {
            val stickyService = Intent(activity, WcSessionManagerService::class.java)
            activity?.startService(stickyService)
        }
    }

    private fun stopConnectionService() {
        if (activity != null) {
            val stickyService = Intent(activity, WcSessionManagerService::class.java)
            activity?.stopService(stickyService)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        handler = Handler()
        analytics.logEvent(OPEN_WALLET_CONNECT_SCREEN_EVENT, Bundle().createEvent("1"))
        viewModel.getSelectedWallet()
        binding.imgBack.setOnClickListener {
            if (isOnline) {
                showDisconnectSessionDialog(REQUEST_BACK)
            } else {
                onBack()
            }
        }

        wallet?.address?.let {
            viewModel.getWalletConnect(it)
        }
        binding.imgWalletConnect.setOnClickListener {
            if (isOnline) {
                showDisconnectSessionDialog(REQUEST_SCAN)
            } else {
                openQRScan()
            }
            analytics.logEvent(OPEN_SCAN_IN_WALLET_CONNECT_ACTION, Bundle().createEvent("1"))
        }



        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (state.wallet.address != wallet?.address) {
                            analytics.logEvent(
                                SWITCH_WALLET_WHEN_WALLET_CONNECT_EVENT,
                                Bundle().createEvent("1")
                            )
                            viewModel.killSession()
                            wallet = state.wallet
                            viewModel.getWalletConnect(state.wallet.address)
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        if (isSessionRequest) {
            handleWalletConnect(connectionInfo)
        }

        viewModel.requestConnectCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is RequestState.Loading -> {
                        showProgress(true)
                    }
                    is RequestState.Success -> {
                        isOnline = state.status
                        showStatus(state.status)
                        showProgress(false)
                        if (!state.status) {
                            showError(
                                getString(R.string.can_not_connect_wallet),
                                time = 10
                            )
                        }
                    }
                    is RequestState.ShowError -> {
                        showStatus(false)
                        showProgress(false)
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.getWalletConnectCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is WalletConnectState.Success -> {
                        if (walletConnect != state.walletConnect) {
                            walletConnect = state.walletConnect
                            if (hasSession) {
                                if (isTransactionRequest) {
                                    if (hasDataPayload) {
                                        if (hasSessionInfo) {
                                            showSessionInfo(
                                                walletConnect?.wcSessionRequest?.status,
                                                walletConnect?.wcSessionRequest?.meta
                                            )
                                        }
                                    }

                                    if (!connectionInfo.isNullOrBlank()) {
                                        walletConnect?.wcEthSendTransaction?.let {
                                            if (it.id > 0 && it.wcTransaction.from.isNotBlank()) {
                                                showEthSendTransactionApprovalDialog(
                                                    it.id,
                                                    it.wcTransaction,
                                                    it.transaction
                                                )
                                            }
                                        }

                                        walletConnect?.wcEthSign?.let {
                                            if (it.id > 0 && it.message.data.isNotBlank()) {
                                                showEthSignApprovalDialog(it.id, it.message)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is WalletConnectState.ShowError -> {

                    }
                }
            }
        })

        viewModel.rejectSessionCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is RequestState.Success -> {
                        showStatus(false)
                    }
                    is RequestState.ShowError -> {

                    }
                }
            }
        })

        viewModel.approveSessionCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SessionRequestState.Loading)
                when (state) {
                    is SessionRequestState.Success -> {
                        startConnectionService()
                        showSessionInfo(state.status, state.meta)
                        saveWcSessionRequest(WcSessionRequest(state.status, state.meta))
                    }
                    is SessionRequestState.ShowError -> {
                        isOnline = false
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.decodeTransactionCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is DecodeTransactionState.Success -> {
                        handler.postDelayed(
                            {
                                showEthSendTransactionApprovalDialog(
                                    state.id,
                                    state.wcTransaction,
                                    state.transaction
                                )
                            }, 250
                        )
                    }
                    is DecodeTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.sendTransactionCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                showProgress(state == RequestState.Loading)
                when (state) {
                    is RequestState.Success -> {

                    }
                    is RequestState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.killSessionCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is RequestState.Success -> {
                        if (requestCode == REQUEST_SCAN) {
                            IntentIntegrator.forSupportFragment(this)
                                .setBeepEnabled(false)
                                .initiateScan()
                        } else if (requestCode == REQUEST_WALLET_CHANGE || requestCode == REQUEST_BACK) {
                            popBackStack()
                        }
                        viewModel.resetWalletConnect(wallet?.address ?: "")
                    }
                    is RequestState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                        if (requestCode == REQUEST_BACK) {
                            popBackStack()
                        }
                    }
                }
            }
        })
    }

    private fun showEthSendTransactionApprovalDialog(
        id: Long,
        wcTransaction: WCEthereumTransaction,
        transaction: Transaction
    ) {
        approvedTransactionDialog = AlertDialog.Builder(activity)
            .setTitle(
                if (wcTransaction.isApproveTx()) {
                    getString(R.string.approve_token)
                } else {
                    getString(R.string.approve_transaction)
                }
            )
            .setMessage(
                if (wcTransaction.isApproveTx()) {
                    getString(R.string.request_approve_message) + " " + transaction.tokenSource
                } else {

                    (if (transaction.isSwap) getString(R.string.dialog_swap) else getString(
                        R.string.dialog_transfer
                    )) + " " + transaction.displayWalletConnectTransaction
                }
            )
            .setPositiveButton(
                getString(R.string.dialog_approve)
            ) { _, _ ->
                analytics.logEvent(
                    APPROVE_WALLET_CONNECT_ACTION,
                    Bundle().createEvent(id.toString())
                )
                viewModel.sendTransaction(id, wcTransaction, wallet!!)

            }
            .setNegativeButton(getString(R.string.dialog_reject)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                analytics.logEvent(
                    REJECT_WALLET_CONNECT_ACTION,
                    Bundle().createEvent(id.toString())
                )
                viewModel.rejectTransaction(id)

            }
            .create()
        approvedTransactionDialog?.setCanceledOnTouchOutside(false)
        showDialog(approvedTransactionDialog)
    }

    private fun showEthSignApprovalDialog(id: Long, signedMessage: WCEthereumSignMessage) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.signature_request))
            .setMessage(getString(R.string.you_are_signing) + signedMessage.raw.first())
            .setPositiveButton(
                getString(R.string.dialog_sign)
            ) { _, _ ->
                viewModel.sign(id, signedMessage, wallet!!)
                saveWcEthSign()
                analytics.logEvent(
                    APPROVE_WALLET_CONNECT_SIGN_ACTION,
                    Bundle().createEvent(id.toString())
                )

            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                viewModel.rejectTransaction(id)
                saveWcEthSign()
                analytics.logEvent(
                    REJECT_WALLET_CONNECT_SIGN_ACTION,
                    Bundle().createEvent(id.toString())
                )
                dialog.dismiss()

            }
            .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun showSessionInfo(
        status: Boolean?, meta: WCPeerMeta?
    ) {
        if (status == null) return
        if (meta == null) return
        isOnline = status
        showStatus(isOnline)
        binding.lnContent.visibility = View.VISIBLE
        Glide.with(this).load(meta.icons.first())
            .into(binding.imgConnectedTo)
        binding.tvTitle.text = meta.name
        binding.tvConnectedTo.text = meta.url
        binding.tvAddress.text = wallet?.address
    }

    private fun saveWcSessionRequest(wcSessionRequest: WcSessionRequest) {
        val wc = walletConnect?.copy(
            address = wallet?.address ?: "",
            wcSessionRequest = wcSessionRequest
        )
        viewModel.saveWalletConnect(wc)
    }

    private fun saveWcEthSign(wcEthSign: WcEthSign? = null) {
        val wc = walletConnect?.copy(
            address = wallet?.address ?: "",
            wcEthSign = wcEthSign
        )
        viewModel.saveWalletConnect(wc)
    }

    private fun showStatus(isOnLine: Boolean) {
        if (isOnLine) {
            binding.tvStatus.text = getText(R.string.online)
            activity?.let {
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.color_green
                    )
                )
            }
        } else {
            binding.tvStatus.text = getString(R.string.offline)
            activity?.let {
                binding.tvStatus.setTextColor(Color.RED)
            }
        }
    }

    fun onBackPress() {
        if (isOnline) {
            showDisconnectSessionDialog(REQUEST_BACK)
        } else {
            onBack()
        }
    }

    private fun showDialog(alertDialog: AlertDialog?, isShowDialog: Boolean = true) {
        if (isShowDialog) {
            alertDialog?.show()
        } else {
            alertDialog?.dismiss()
        }
    }

    private fun showDisconnectSessionDialog(code: Int) {
        if (isAdded) {
            disConnectSessionDialog = AlertDialog.Builder(activity)
                .setTitle(getString(R.string.disconnect_title))
                .setMessage(getString(R.string.disconnect_message))
                .setPositiveButton(getString(R.string.dialog_disconnect)) { _, _ ->
                    onBack(code)
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            disConnectSessionDialog?.show()
        }
    }

    private fun handleWalletConnect(connectionInfo: String?) {
        wallet?.address?.let { walletAddress ->
            connectionInfo?.let { info ->
                viewModel.saveWalletConnect(walletConnect?.copy(sessionInfo = info))
                viewModel.connect(walletAddress, info, { id, meta ->
                    if (isAdded) {
                        handler.post {
                            showProgress(false)
                            approveSessionDialog = AlertDialog.Builder(activity)
                                .setTitle(meta.name)
                                .setMessage(meta.url)
                                .setPositiveButton(getString(R.string.dialog_approve)) { _, _ ->
                                    viewModel.approveSession(walletAddress, meta)
                                    analytics.logEvent(
                                        APPROVE_WALLET_CONNECT_SESSION_ACTION,
                                        Bundle().createEvent(id.toString())
                                    )

                                }
                                .setNegativeButton(getString(R.string.dialog_reject)) { dialog, _ ->
                                    viewModel.rejectSession()
                                    analytics.logEvent(
                                        REJECT_WALLET_CONNECT_SESSION_ACTION,
                                        Bundle().createEvent(id.toString())
                                    )
                                    dialog.dismiss()

                                }
                                .create()
                            showDialog(approveSessionDialog)
                        }
                    }

                }, { id, transaction ->
                    viewModel.decodeTransaction(id, transaction, wallet!!, walletConnect)
                }, { id, signedMessage ->
                    saveWcEthSign(WcEthSign(id, signedMessage))
                    if (isAdded) {
                        handler.post {
                            showEthSignApprovalDialog(id, signedMessage)
                        }
                    }

                }, { code, _ ->
                    viewModel.resetWalletConnect(walletAddress)
                    if (isAdded) {
                        handler.post {
                            isOnline = false
                            showProgress(false)
                            if (requestCode == REQUEST_BACK) {
                                handler.post { activity?.onBackPressed() }
                            } else {
                                showStatus(false)
                                if (requestCode != REQUEST_SCAN && requestCode > 0) {
                                    dialogHelper.showDisconnectDialog({
                                        openQRScan()
                                    }, {
                                        onBack()
                                    })
                                }
                            }

                            if (requestCode != REQUEST_SCAN && requestCode != REQUEST_BACK && requestCode != REQUEST_WALLET_CHANGE) {
                                analytics.logEvent(
                                    DISCONNECT_WALLET_CONNECT_EVENT,
                                    Bundle().createEvent(code.toString())
                                )
                            }
                        }
                    }

                }, {
                    isOnline = false
                    viewModel.resetWalletConnect(walletAddress)
                    if (isAdded) {
                        handler.post {
                            showProgress(false)
                            if (requestCode == REQUEST_BACK) {
                                activity?.onBackPressed()
                            }
                            showStatus(false)
                            if (!isNetworkAvailable()) {
                                showNetworkUnAvailable()
                            } else if (!it.message.isNullOrEmpty()) {
                                showError(it.message!!)
                            }

                            it.message?.let {
                                analytics.logEvent(
                                    FAIL_WALLET_CONNECT_EVENT,
                                    Bundle().createEvent(it)
                                )
                            }
                        }
                    }

                })
            }
        }
    }

    private fun onBack(code: Int = REQUEST_BACK) {
        requestCode = code
        stopConnectionService()
        if (isOnline) {
            viewModel.killSession()
        } else {
            popBackStack()
        }
    }

    private fun openQRScan() {
        requestCode = REQUEST_SCAN
        if (isOnline) {
            viewModel.killSession()
        } else {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()
        }
    }

    private fun popBackStack() {
        (activity as MainActivity).getCurrentFragment()?.childFragmentManager?.popBackStack()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        approveSessionDialog?.dismiss()
        approvedTransactionDialog?.dismiss()
        disConnectSessionDialog?.dismiss()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null && scanResult.contents != null) {
            this.connectionInfo = scanResult.contents
            handleWalletConnect(connectionInfo)
        }
    }

    companion object {
        private const val REQUEST_BACK = 1
        private const val REQUEST_SCAN = 2
        private const val REQUEST_WALLET_CHANGE = 3
        private const val WALLET_PARAM = "wallet_param"
        private const val CONTENT_PARAM = "content_param"
        fun newInstance(wallet: Wallet?, content: String) =
            WalletConnectFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putString(CONTENT_PARAM, content)
                }
            }
    }
}

