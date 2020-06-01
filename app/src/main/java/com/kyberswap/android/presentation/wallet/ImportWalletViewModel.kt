package com.kyberswap.android.presentation.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.token.GetTokenBalanceUseCase
import com.kyberswap.android.domain.usecase.token.GetTokensBalanceUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

open class ImportWalletViewModel @Inject constructor(
    private val getBatchTokenBalanceUseCase: GetTokensBalanceUseCase,
    private val getTokenBalanceUseCase: GetTokenBalanceUseCase
) : ViewModel() {
    val importWalletCallback: MutableLiveData<ImportWalletState> = MutableLiveData()
//    private var numberOfToken = 0

    fun loadBalances(pair: Pair<Wallet, List<Token>>) {
        val tokenList = pair.second.toMutableList()
        val listedTokens = tokenList.filter { it.isListed && !it.isOther }
        tokenList.removeAll(listedTokens)
        loadListedBalances(Pair(pair.first, listedTokens))
        loadOtherBalances(tokenList)

//        numberOfToken = 0
//        pair.second.forEach { token ->
//            getTokenBalanceUseCase.execute(
//                Action {
//                    numberOfToken++
//                    if (numberOfToken == pair.second.size) {
//                        importWalletCallback.value = ImportWalletState.Success(pair.first)
//                    }
//                },
//                Consumer {
//                    numberOfToken++
//                    if (numberOfToken == pair.second.size) {
//                        importWalletCallback.value = ImportWalletState.Success(pair.first)
//                    }
//                },
//                token
//            )
//        }
    }

    private fun loadListedBalances(
        pair: Pair<Wallet, List<Token>>
    ) {
        getBatchTokenBalanceUseCase.dispose()
        getBatchTokenBalanceUseCase.execute(
            Action {
                importWalletCallback.value = ImportWalletState.Success(pair.first)
            },
            Consumer {
                Timber.e(it.localizedMessage)
                importWalletCallback.value = ImportWalletState.Success(pair.first)
                it.printStackTrace()
            },
            GetTokensBalanceUseCase.Param(pair.first, pair.second)
        )
    }

    private fun loadOtherBalances(others: List<Token>) {
        others.sortedByDescending { it.currentBalance }.forEach { token ->
            getTokenBalanceUseCase.execute(
                Action {

                },
                Consumer {
                    Timber.e(token.symbol)
                    Timber.e(it.localizedMessage)
                    it.printStackTrace()
                },
                token
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        getTokenBalanceUseCase.dispose()
    }
}