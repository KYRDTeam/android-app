package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Wallet

interface WalletRepository {

    fun createWallet(param: CreateWalletUseCase.Param): Single<Wallet>

}
