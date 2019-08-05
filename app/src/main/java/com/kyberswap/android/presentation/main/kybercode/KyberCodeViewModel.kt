package com.kyberswap.android.presentation.main.kybercode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.GetTokenBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.ApplyKyberCodeUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class KyberCodeViewModel @Inject constructor(
    private val applyKyberCodeUseCase: ApplyKyberCodeUseCase,
    private val getTokenBalance: GetTokenBalanceUseCase
) : ViewModel() {
    private val _getKyberCodeCallback = MutableLiveData<Event<KyberCodeState>>()
    val getKyberCodeCallback: LiveData<Event<KyberCodeState>>
        get() = _getKyberCodeCallback

    fun createWalletByKyberCode(kyberCode: String, walletName: String) {
        _getKyberCodeCallback.postValue(Event(KyberCodeState.Loading))
        applyKyberCodeUseCase.execute(
            Consumer { pair ->
                var numberOfToken = 0
                pair.second.forEach { token ->
                    getTokenBalance.execute(
                        Action {
                            numberOfToken++
                            if (numberOfToken == pair.second.size) {
                                if (pair.first.promo?.error.isNullOrEmpty()) {
                                    _getKyberCodeCallback.value =
                                        Event(KyberCodeState.Success(pair.first))
                                } else {
                                    _getKyberCodeCallback.value =
                                        Event(KyberCodeState.ShowError(pair.first.promo?.error))
                                }
                            }
                        },
                        Consumer {
                            numberOfToken++
                            if (numberOfToken == pair.second.size) {
                                if (pair.first.promo?.error.isNullOrEmpty()) {
                                    _getKyberCodeCallback.value =
                                        Event(KyberCodeState.Success(pair.first))
                                } else {
                                    _getKyberCodeCallback.value =
                                        Event(KyberCodeState.ShowError(pair.first.promo?.error))
                                }
                            }
                        },
                        token
                    )
                }


            },
            Consumer {
                it.printStackTrace()
                _getKyberCodeCallback.value = Event(KyberCodeState.ShowError(it.localizedMessage))
            },
            ApplyKyberCodeUseCase.Param(kyberCode, walletName)
        )
    }


}