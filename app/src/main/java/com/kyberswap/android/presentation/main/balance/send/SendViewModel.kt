package com.kyberswap.android.presentation.main.balance.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.contact.DeleteContactUseCase
import com.kyberswap.android.domain.usecase.contact.GetContactUseCase
import com.kyberswap.android.domain.usecase.contact.SaveContactUseCase
import com.kyberswap.android.domain.usecase.send.ENSResolveUseCase
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateTransferGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetGasPriceUseCase
import com.kyberswap.android.domain.usecase.wallet.CheckEligibleWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.ADDITIONAL_SEND_GAS_LIMIT
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.common.MIN_SUPPORT_AMOUNT
import com.kyberswap.android.presentation.common.calculateDefaultGasLimitTransfer
import com.kyberswap.android.presentation.common.specialGasLimitDefault
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.main.swap.DeleteContactState
import com.kyberswap.android.presentation.main.swap.GetContactState
import com.kyberswap.android.presentation.main.swap.GetENSAddressState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.math.BigInteger
import javax.inject.Inject

class SendViewModel @Inject constructor(
    private val saveContactUseCase: SaveContactUseCase,
    private val getSendTokenUseCase: GetSendTokenUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val saveSendUseCase: SaveSendUseCase,
    private val getContactUseCase: GetContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val estimateTransferGasUseCase: EstimateTransferGasUseCase,
    private val ensResolveUseCase: ENSResolveUseCase,
    private val checkEligibleWalletUseCase: CheckEligibleWalletUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {
    val compositeDisposable = CompositeDisposable()
    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback

    private val _getSendCallback = MutableLiveData<Event<GetSendState>>()
    val getSendCallback: LiveData<Event<GetSendState>>
        get() = _getSendCallback

    private val _getContactCallback = MutableLiveData<Event<GetContactState>>()
    val getContactCallback: LiveData<Event<GetContactState>>
        get() = _getContactCallback

    private val _saveSendCallback = MutableLiveData<Event<SaveSendState>>()
    val saveSendCallback: LiveData<Event<SaveSendState>>
        get() = _saveSendCallback

    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback

    private val _getGetENSCallback = MutableLiveData<Event<GetENSAddressState>>()
    val getGetENSCallback: LiveData<Event<GetENSAddressState>>
        get() = _getGetENSCallback

    private val _saveContactCallback = MutableLiveData<Event<SaveContactState>>()
    val saveContactCallback: LiveData<Event<SaveContactState>>
        get() = _saveContactCallback

    private val _deleteContactCallback = MutableLiveData<Event<DeleteContactState>>()
    val deleteContactCallback: LiveData<Event<DeleteContactState>>
        get() = _deleteContactCallback

    val currentContactSelection: LiveData<Event<Contact>>
        get() = _currentSelection

    private val _currentSelection = MutableLiveData<Event<Contact>>()

    private val _checkEligibleWalletCallback = MutableLiveData<Event<CheckEligibleWalletState>>()
    val checkEligibleWalletCallback: LiveData<Event<CheckEligibleWalletState>>
        get() = _checkEligibleWalletCallback

    fun updateCurrentContact(contact: Contact) {
        _currentSelection.value = Event(contact)
    }

    fun getSendInfo(wallet: Wallet) {
        getSendTokenUseCase.dispose()
        getSendTokenUseCase.execute(
            Consumer {
                it.gasLimit =
                    calculateGasLimit(it).toString()
                _getSendCallback.value = Event(GetSendState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getSendCallback.value = Event(GetSendState.ShowError(errorHandler.getError(it)))
            },
            GetSendTokenUseCase.Param(wallet)
        )
    }

    private fun calculateGasLimit(send: Send): BigInteger {
        return calculateDefaultGasLimitTransfer(send.tokenSource)
    }

    fun deleteContact(contact: Contact) {
        deleteContactUseCase.execute(
            Action {
                _deleteContactCallback.value = Event(DeleteContactState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _deleteContactCallback.value =
                    Event(DeleteContactState.ShowError(errorHandler.getError(it)))
            },
            DeleteContactUseCase.Param(contact)
        )
    }

    fun saveSendContact(walletAddress: String, contact: Contact) {
        saveContactUseCase.execute(
            Action {
                _saveContactCallback.value = Event(SaveContactState.Success())
            },
            Consumer {
                it.printStackTrace()
                _saveContactCallback.value =
                    Event(SaveContactState.ShowError(errorHandler.getError(it)))
            },
            SaveContactUseCase.Param(walletAddress, contact.address, contact.name)
        )
    }

    fun getGasPrice() {
        getGasPriceUseCase.dispose()
        getGasPriceUseCase.execute(
            Consumer {
                _getGetGasPriceCallback.value = Event(GetGasPriceState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getGetGasPriceCallback.value =
                    Event(GetGasPriceState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun checkEligibleWallet(wallet: Wallet) {
        checkEligibleWalletUseCase.dispose()
        _checkEligibleWalletCallback.postValue(Event(CheckEligibleWalletState.Loading))
        checkEligibleWalletUseCase.execute(
            Consumer {
                _checkEligibleWalletCallback.value =
                    Event(CheckEligibleWalletState.Success(it))
            },
            Consumer {
                _checkEligibleWalletCallback.value =
                    Event(CheckEligibleWalletState.ShowError(errorHandler.getError(it)))
            },
            CheckEligibleWalletUseCase.Param(wallet)
        )
    }

    fun saveSend(send: Send?, address: String = "") {
        send?.let {
            saveSendUseCase.execute(
                Action {
                    if (address.isNotEmpty()) {
                        _saveSendCallback.value = Event(SaveSendState.Success(""))
                    }
                },
                Consumer { error ->
                    error.printStackTrace()
                    _saveSendCallback.value =
                        Event(SaveSendState.ShowError(errorHandler.getError(error)))
                },
                SaveSendUseCase.Param(it, address)
            )
        }
    }

    fun getContact() {
        getContactUseCase.execute(
            Consumer {
                _getContactCallback.value = Event(GetContactState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getContactCallback.value =
                    Event(GetContactState.ShowError(errorHandler.getError(it)))
            },
            GetContactUseCase.Param()
        )
    }

    fun getGasLimit(send: Send?, wallet: Wallet?) {
        if (send == null || wallet == null) return
        if (send.tokenSource.isETH && send.contact.address.isEmpty()) return
        if (send.ethToken.currentBalance <= MIN_SUPPORT_AMOUNT) return
        estimateTransferGasUseCase.dispose()
        estimateTransferGasUseCase.execute(
            Consumer {
                val gasLimit = if (it.error != null) {
                    calculateDefaultGasLimitTransfer(send.tokenSource)
                } else {
                    calculateDefaultGasLimitTransfer(send.tokenSource)
                        .min(
                            it.amountUsed.multiply(120.toBigInteger())
                                .divide(100.toBigInteger())
                        )
                } + if (send.tokenSource.isETH) BigInteger.ZERO else ADDITIONAL_SEND_GAS_LIMIT.toBigInteger()

                val specialGasLimit = specialGasLimitDefault(send.tokenSource, send.tokenSource)

                _getGetGasLimitCallback.value = Event(
                    GetGasLimitState.Success(
                        if (specialGasLimit != null) {
                            specialGasLimit.min(gasLimit)
                        } else {
                            gasLimit
                        }
                    )
                )
            },
            Consumer {
                it.printStackTrace()
            },
            EstimateTransferGasUseCase.Param(wallet, send)
        )
    }

    override fun onCleared() {
        getSendTokenUseCase.dispose()
        getGasPriceUseCase.dispose()
        saveSendUseCase.dispose()
        getContactUseCase.dispose()
        estimateTransferGasUseCase.dispose()
        checkEligibleWalletUseCase.dispose()
        saveContactUseCase.dispose()
        deleteContactUseCase.dispose()
        ensResolveUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun resolve(name: CharSequence, isFromContinue: Boolean = false) {
//        if (isFromContinue) {
//            _getGetENSCallback.postValue(Event(GetENSAddressState.Loading))
//        }
        ensResolveUseCase.dispose()
        ensResolveUseCase.execute(
            Consumer {
                _getGetENSCallback.value =
                    Event(GetENSAddressState.Success(name.toString(), it, isFromContinue))
            },
            Consumer {
                _getGetENSCallback.value = Event(GetENSAddressState.ShowError(it.message))
            },
            ENSResolveUseCase.Param(name.toString())
        )
    }
}