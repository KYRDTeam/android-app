package com.kyberswap.android.presentation.main.walletconnect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletConnect
import com.kyberswap.android.domain.model.WcEthSendTransaction
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletConnectUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletConnectUseCase
import com.kyberswap.android.domain.usecase.walletconnect.DecodeTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectApproveSessionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectKillSessionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectRejectSessionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectRejectTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectSendTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectSignedTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.util.ErrorHandler
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import timber.log.Timber
import javax.inject.Inject

class WalletConnectViewModel @Inject constructor(
    private val walletConnectUseCase: WalletConnectUseCase,
    private val walletConnectSendTransactionUseCase: WalletConnectSendTransactionUseCase,
    private val walletConnectRejectSessionUseCase: WalletConnectRejectSessionUseCase,
    private val walletConnectApproveSessionUseCase: WalletConnectApproveSessionUseCase,
    private val walletConnectSignedTransactionUseCase: WalletConnectSignedTransactionUseCase,
    private val killSessionUseCase: WalletConnectKillSessionUseCase,
    private val decodeTransactionUseCase: DecodeTransactionUseCase,
    private val rejectTransactionUseCase: WalletConnectRejectTransactionUseCase,
    private val updateWalletConnectUseCase: UpdateWalletConnectUseCase,
    private val getWalletConnectUseCase: GetWalletConnectUseCase,
    getWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getWalletUseCase, errorHandler) {

    private val _approveSessionCallback = MutableLiveData<Event<SessionRequestState>>()
    val approveSessionCallback: LiveData<Event<SessionRequestState>>
        get() = _approveSessionCallback

    private val _sendTransactionCallback = MutableLiveData<Event<RequestState>>()
    val sendTransactionCallback: LiveData<Event<RequestState>>
        get() = _sendTransactionCallback

    private val _requestConnectCallback = MutableLiveData<Event<RequestState>>()
    val requestConnectCallback: LiveData<Event<RequestState>>
        get() = _requestConnectCallback

    private val _rejectSessionCallback = MutableLiveData<Event<RequestState>>()
    val rejectSessionCallback: LiveData<Event<RequestState>>
        get() = _rejectSessionCallback

    private val _killSessionCallback = MutableLiveData<Event<RequestState>>()
    val killSessionCallback: LiveData<Event<RequestState>>
        get() = _killSessionCallback

    private val _decodeTransactionCallback = MutableLiveData<Event<DecodeTransactionState>>()
    val decodeTransactionCallback: LiveData<Event<DecodeTransactionState>>
        get() = _decodeTransactionCallback

    private val _getWalletConnectCallback = MutableLiveData<Event<WalletConnectState>>()
    val getWalletConnectCallback: LiveData<Event<WalletConnectState>>
        get() = _getWalletConnectCallback

    fun saveWalletConnect(walletConnect: WalletConnect?) {
        if (walletConnect == null) return
        updateWalletConnectUseCase.execute(
            {

            },
            {
                it.printStackTrace()
            },
            walletConnect
        )
    }

    fun resetWalletConnect(address: String) {
        val wc = WalletConnect(address = address)
        updateWalletConnectUseCase.dispose()
        updateWalletConnectUseCase.execute(
            {

            },
            {
                it.printStackTrace()
            },
            wc
        )
    }

    fun getWalletConnect(address: String) {
        getWalletConnectUseCase.dispose()
        getWalletConnectUseCase.execute(
            {
                _getWalletConnectCallback.value = Event(WalletConnectState.Success(it))
            },
            {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
            },
            address
        )
    }

    fun connect(
        walletAddress: String,
        contents: String,
        onSessionRequest: (id: Long, peer: WCPeerMeta) -> Unit,
        onEthSendTransaction: (id: Long, transaction: WCEthereumTransaction) -> Unit,
        onEthSign: (id: Long, message: WCEthereumSignMessage) -> Unit,
        onDisconnect: (code: Int, reason: String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        _requestConnectCallback.postValue(Event(RequestState.Loading))
        walletConnectUseCase.dispose()
        walletConnectUseCase.execute(
            {
                _requestConnectCallback.value = Event(RequestState.Success(it))
            },
            {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
                _requestConnectCallback.value =
                    Event(RequestState.ShowError(errorHandler.getError(it)))
            },
            WalletConnectUseCase.Param(
                contents,
                walletAddress,
                onSessionRequest,
                onEthSendTransaction,
                onEthSign,
                onDisconnect,
                onFailure
            )
        )
    }

    fun approveSession(walletAddress: String, meta: WCPeerMeta) {
        walletConnectApproveSessionUseCase.execute(
            {
                _approveSessionCallback.value =
                    Event(SessionRequestState.Success(it, meta))
            },
            {
                it.printStackTrace()
            },
            WalletConnectApproveSessionUseCase.Param(walletAddress)

        )
    }

    fun sendTransaction(id: Long, transaction: WCEthereumTransaction, wallet: Wallet) {
        walletConnectApproveSessionUseCase.dispose()
        _sendTransactionCallback.postValue(Event(RequestState.Loading))
        walletConnectSendTransactionUseCase.execute(
            {
                _sendTransactionCallback.value = Event(RequestState.Success(true))
            },
            {
                _sendTransactionCallback.value = Event(RequestState.ShowError(it.localizedMessage))
            },
            WalletConnectSendTransactionUseCase.Param(id, transaction, wallet)
        )
    }

    fun rejectTransaction(id: Long) {
        rejectTransactionUseCase.execute(
            {
            },
            {
                it.printStackTrace()
            },
            WalletConnectRejectTransactionUseCase.Param(id)
        )
    }

    fun sign(id: Long, signedMessage: WCEthereumSignMessage, wallet: Wallet) {
        walletConnectSignedTransactionUseCase.execute(
            { },
            { },
            WalletConnectSignedTransactionUseCase.Param(id, signedMessage, wallet)
        )
    }

    fun decodeTransaction(
        id: Long,
        wcTransaction: WCEthereumTransaction,
        wallet: Wallet,
        walletConnect: WalletConnect?
    ) {
        decodeTransactionUseCase.execute(
            {
                _decodeTransactionCallback.value =
                    Event(DecodeTransactionState.Success(id, wcTransaction, it))

                val wc = walletConnect?.copy(
                    address = wallet.address,
                    wcEthSendTransaction = WcEthSendTransaction(
                        id,
                        wcTransaction,
                        it
                    )
                )
                saveWalletConnect(wc)
            },
            {
                _decodeTransactionCallback.value =
                    Event(DecodeTransactionState.ShowError(errorHandler.getError(it)))
            },
            DecodeTransactionUseCase.Param(wcTransaction, wallet)
        )
    }

    fun rejectSession() {
        walletConnectRejectSessionUseCase.execute(
            {
                _rejectSessionCallback.value = Event(RequestState.Success(it))
            },
            {
                _rejectSessionCallback.value = Event(RequestState.ShowError(it.message))
            },
            WalletConnectRejectSessionUseCase.Param()
        )
    }

    fun killSession() {
        killSessionUseCase.execute(
            {
                _killSessionCallback.value = Event(RequestState.Success(it))
            },
            {
                it.printStackTrace()
                Timber.e("error: %s", it.localizedMessage)
                _killSessionCallback.value =
                    Event(RequestState.ShowError(errorHandler.getError(it)))
            },
            WalletConnectKillSessionUseCase.Param()
        )
    }

    override fun onCleared() {
        walletConnectUseCase.dispose()
        walletConnectSendTransactionUseCase.dispose()
        walletConnectRejectSessionUseCase.dispose()
        walletConnectApproveSessionUseCase.dispose()
        walletConnectSignedTransactionUseCase.dispose()
        killSessionUseCase.dispose()
        decodeTransactionUseCase.dispose()
        rejectTransactionUseCase.dispose()
        updateWalletConnectUseCase.dispose()
        getWalletConnectUseCase.dispose()
        super.onCleared()
    }
}