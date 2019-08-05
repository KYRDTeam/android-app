package com.kyberswap.android.presentation.main.setting.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.DeleteWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetAllWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.splash.GetWalletState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ManageWalletViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase,
    private val getAllWalletUseCase: GetAllWalletUseCase,
    private val updateSelectedWalletUseCase: UpdateSelectedWalletUseCase,
    private val deleteWalletUseCase: DeleteWalletUseCase
) : ViewModel() {


    private val _getMnemonicCallback = MutableLiveData<Event<CreateWalletState>>()
    val createWalletCallback: LiveData<Event<CreateWalletState>>
        get() = _getMnemonicCallback

    private val _getAllWalletStateCallback = MutableLiveData<Event<GetAllWalletState>>()
    val getAllWalletStateCallback: LiveData<Event<GetAllWalletState>>
        get() = _getAllWalletStateCallback

    private val _getWalletStateCallback = MutableLiveData<Event<GetWalletState>>()
    val getWalletStateCallback: LiveData<Event<GetWalletState>>
        get() = _getWalletStateCallback

    private val _deleteWalletCallback = MutableLiveData<Event<DeleteWalletState>>()
    val deleteWalletCallback: LiveData<Event<DeleteWalletState>>
        get() = _deleteWalletCallback


    fun createWallet(walletName: String = "Untitled") {
        _getMnemonicCallback.postValue(Event(CreateWalletState.Loading))
        createWalletUseCase.execute(
            Consumer {
                _getMnemonicCallback.value =
                    Event(CreateWalletState.Success(it.first, it.second))
            },
            Consumer {
                it.printStackTrace()
                _getMnemonicCallback.value =
                    Event(CreateWalletState.ShowError(it.localizedMessage))
            },
            CreateWalletUseCase.Param(walletName)
        )
    }

    fun deleteWallet(wallet: Wallet) {
        _deleteWalletCallback.postValue(Event(DeleteWalletState.Loading))
        deleteWalletUseCase.execute(
            Consumer {
                _deleteWalletCallback.value = Event(DeleteWalletState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _deleteWalletCallback.value =
                    Event(DeleteWalletState.ShowError(it.localizedMessage))
            },
            DeleteWalletUseCase.Param(wallet)
        )
    }

    fun getWallets() {
        getAllWalletUseCase.execute(
            Consumer {
                _getAllWalletStateCallback.value = Event(
                    GetAllWalletState.Success(
                        it
                    )
                )
            },
            Consumer {
                it.printStackTrace()
                _getAllWalletStateCallback.value =
                    Event(
                        GetAllWalletState.ShowError(
                            it.localizedMessage
                        )
                    )
            },
            null
        )
    }

    fun updateSelectedWallet(wallet: Wallet) {
        updateSelectedWalletUseCase.execute(
            Consumer { wl ->
                _getWalletStateCallback.value = Event(GetWalletState.Success(wl))

            },
            Consumer {
                it.printStackTrace()
                _getWalletStateCallback.value =
                    Event(GetWalletState.ShowError(it.localizedMessage))
            },
            UpdateSelectedWalletUseCase.Param(wallet)
        )
    }


}