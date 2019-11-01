package com.kyberswap.android.presentation.main.kybercode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.GetTokensBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.ApplyKyberCodeUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import org.consenlabs.tokencore.wallet.model.TokenException
import javax.inject.Inject

class KyberCodeViewModel @Inject constructor(
    private val applyKyberCodeUseCase: ApplyKyberCodeUseCase,
    private val getTokenBalanceUseCase: GetTokensBalanceUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    private val _getKyberCodeCallback = MutableLiveData<Event<KyberCodeState>>()
    val getKyberCodeCallback: LiveData<Event<KyberCodeState>>
        get() = _getKyberCodeCallback

    fun createWalletByKyberCode(kyberCode: String, walletName: String) {
        _getKyberCodeCallback.postValue(Event(KyberCodeState.Loading))
        applyKyberCodeUseCase.execute(
            Consumer { pair ->
                //                var numberOfToken = 0
//                pair.second.forEach { token ->
//                    getTokenBalance.execute(
//                        Action {
//                            numberOfToken++
//                            if (numberOfToken == pair.second.size) {
//                                if (pair.first.promo?.error.isNullOrEmpty()) {
//                                    _getKyberCodeCallback.value =
//                                        Event(KyberCodeState.Success(pair.first))
//                                } else {
//                                    _getKyberCodeCallback.value =
//                                        Event(KyberCodeState.ShowError(pair.first.promo?.error))
//                                }
//                            }
//                        },
//                        Consumer {
//                            numberOfToken++
//                            if (numberOfToken == pair.second.size) {
//                                if (pair.first.promo?.error.isNullOrEmpty()) {
//                                    _getKyberCodeCallback.value =
//                                        Event(KyberCodeState.Success(pair.first))
//                                } else {
//                                    _getKyberCodeCallback.value =
//                                        Event(KyberCodeState.ShowError(pair.first.promo?.error))
//                                }
//                            }
//                        },
//                        token
//                    )
//                }

                getTokenBalanceUseCase.execute(
                    Action {
                        if (pair.first.promo?.error.isNullOrEmpty()) {
                            _getKyberCodeCallback.value =
                                Event(KyberCodeState.Success(pair.first))
                        } else {
                            _getKyberCodeCallback.value =
                                Event(KyberCodeState.ShowError(pair.first.promo?.error))
                        }
                    },
                    Consumer {
                        if (pair.first.promo?.error.isNullOrEmpty()) {
                            _getKyberCodeCallback.value =
                                Event(KyberCodeState.Success(pair.first))
                        } else {
                            _getKyberCodeCallback.value =
                                Event(KyberCodeState.ShowError(pair.first.promo?.error))
                        }
                    },
                    GetTokensBalanceUseCase.Param(pair.first, pair.second)
                )

            },
            Consumer {
                it.printStackTrace()
                if (it is TokenException) {
                    _getKyberCodeCallback.value = Event(KyberCodeState.ShowError(it.message))
                } else {
                    _getKyberCodeCallback.value =
                        Event(KyberCodeState.ShowError(errorHandler.getError(it)))
                }

            },
            ApplyKyberCodeUseCase.Param(kyberCode, walletName)
        )
    }
}