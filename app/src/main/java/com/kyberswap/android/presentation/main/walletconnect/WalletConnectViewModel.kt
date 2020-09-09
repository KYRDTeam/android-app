package com.kyberswap.android.presentation.main.walletconnect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
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
            { },
            { },
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

    fun decodeTransaction(id: Long, wcTransaction: WCEthereumTransaction, wallet: Wallet) {
        decodeTransactionUseCase.execute(
            {
                _decodeTransactionCallback.value =
                    Event(DecodeTransactionState.Success(id, wcTransaction, it))
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
                _killSessionCallback.value =
                    Event(RequestState.ShowError(errorHandler.getError(it)))
            },
            WalletConnectKillSessionUseCase.Param()
        )
    }
}