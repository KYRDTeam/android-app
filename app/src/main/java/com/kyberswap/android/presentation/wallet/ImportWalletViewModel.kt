package com.kyberswap.android.presentation.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.token.GetTokenBalanceUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

open class ImportWalletViewModel @Inject constructor(
    private val getTokenBalanceUseCase: GetTokenBalanceUseCase
) : ViewModel() {
    val importWalletCallback: MutableLiveData<ImportWalletState> = MutableLiveData()
    private var numberOfToken = 0

    fun loadBalances(pair: Pair<Wallet, List<Token>>) {
        numberOfToken = 0
        pair.second.forEach { token ->
            getTokenBalanceUseCase.execute(
                Action {
                    numberOfToken++
                    if (numberOfToken == pair.second.size) {
                        importWalletCallback.value = ImportWalletState.Success(pair.first)
                    }
                },
                Consumer {
                    numberOfToken++
                    if (numberOfToken == pair.second.size) {
                        importWalletCallback.value = ImportWalletState.Success(pair.first)
                    }
                },
                token
            )
        }
//        getTokenBalanceUseCase.execute(
//            Action {
//                importWalletCallback.value = ImportWalletState.Success(pair.first)
//            },
//            Consumer {
//                it.printStackTrace()
//                importWalletCallback.value = ImportWalletState.Success(pair.first)
//            },
//            GetTokensBalanceUseCase.Param(pair.first, pair.second)
//        )
    }

    override fun onCleared() {
        super.onCleared()
        getTokenBalanceUseCase.dispose()
    }
}