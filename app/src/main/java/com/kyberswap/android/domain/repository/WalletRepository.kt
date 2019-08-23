package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.usecase.wallet.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface WalletRepository {
    fun createWallet(param: CreateWalletUseCase.Param): Single<Triple<Wallet, List<Word>, List<Token>>>

    fun createWallet(param: ApplyKyberCodeUseCase.Param): Single<Pair<Wallet, List<Token>>>

    fun getMnemonic(param: GetMnemonicUseCase.Param): Single<List<Word>>

    fun importWallet(param: ImportWalletFromJsonUseCase.Param): Single<Pair<Wallet, List<Token>>>

    fun importWallet(param: ImportWalletFromPrivateKeyUseCase.Param): Single<Pair<Wallet, List<Token>>>

    fun importWallet(param: ImportWalletFromSeedUseCase.Param): Single<Pair<Wallet, List<Token>>>

    fun getSelectedWallet(): Flowable<Wallet>

    fun getWalletByAddress(param: String): Flowable<Wallet>

    fun getAllWallet(): Flowable<List<Wallet>>

    fun getSelectedUnit(): Flowable<String>

    fun setSelectedUnit(unit: String): Completable

    fun updateWallet(param: Wallet): Completable

    fun updatedSelectedWallet(param: UpdateSelectedWalletUseCase.Param): Single<Wallet>

    fun exportKeystore(param: ExportKeystoreWalletUseCase.Param): Single<String>

    fun exportPrivateKey(param: ExportPrivateKeyWalletUseCase.Param): Single<String>

    fun exportMnemonic(param: ExportMnemonicWalletUseCase.Param): Single<String>

    fun deleteWallet(param: DeleteWalletUseCase.Param): Single<VerifyStatus>

    fun saveWallet(param: SaveWalletUseCase.Param): Completable
}
