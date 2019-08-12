package com.kyberswap.android.presentation.main.setting.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.token.GetTokenBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.DeleteWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetAllWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.wallet.UpdateWalletState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ManageWalletViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase,
    private val getAllWalletUseCase: GetAllWalletUseCase,
    private val updateSelectedWalletUseCase: UpdateSelectedWalletUseCase,
    private val deleteWalletUseCase: DeleteWalletUseCase,
    private val getTokenBalanceUseCase: GetTokenBalanceUseCase
) : ViewModel() {


    private val _getMnemonicCallback = MutableLiveData<Event<CreateWalletState>>()
    val createWalletCallback: LiveData<Event<CreateWalletState>>
        get() = _getMnemonicCallback

    private val _getAllWalletStateCallback = MutableLiveData<Event<GetAllWalletState>>()
    val getAllWalletStateCallback: LiveData<Event<GetAllWalletState>>
        get() = _getAllWalletStateCallback

    private val _updateWalletStateCallback = MutableLiveData<Event<UpdateWalletState>>()
    val updateWalletStateCallback: LiveData<Event<UpdateWalletState>>
        get() = _updateWalletStateCallback

    private val _deleteWalletCallback = MutableLiveData<Event<DeleteWalletState>>()
    val deleteWalletCallback: LiveData<Event<DeleteWalletState>>
        get() = _deleteWalletCallback

    private var numberOfToken = 0


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

    private fun loadBalances(pair: Pair<Wallet, List<Token>>) {
        numberOfToken = 0
        pair.second.forEach { token ->
            getTokenBalanceUseCase.execute(
                Action {
                    numberOfToken++
                    if (numberOfToken == pair.second.size) {
                        _updateWalletStateCallback.value =
                            Event(UpdateWalletState.Success(pair.first))
                    }
                },
                Consumer {
                    numberOfToken++
                    if (numberOfToken == pair.second.size) {
                        _updateWalletStateCallback.value =
                            Event(UpdateWalletState.Success(pair.first))
                    }
                },
                token
            )
        }
    }

    fun updateSelectedWallet(wallet: Wallet) {
        _updateWalletStateCallback.postValue(Event(UpdateWalletState.Loading))
        updateSelectedWalletUseCase.execute(
            Consumer { wl ->
                loadBalances(wl)

            },
            Consumer {
                it.printStackTrace()
                _updateWalletStateCallback.value =
                    Event(UpdateWalletState.ShowError(it.localizedMessage))
            },
            UpdateSelectedWalletUseCase.Param(wallet)
        )
    }


}