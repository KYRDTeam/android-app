package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.usecase.wallet.*
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Wallet

interface WalletRepository {
    fun createWallet(param: CreateWalletUseCase.Param): Single<Wallet>

    fun getMnemonic(param: GetMnemonicUseCase.Param): Single<List<Word>>

    fun importWallet(param: ImportWalletFromJsonUseCase.Param): Single<Wallet>

    fun importWallet(param: ImportWalletFromPrivateKeyUseCase.Param): Single<Wallet>

    fun importWallet(param: ImportWalletFromSeedUseCase.Param): Single<Wallet>
}
