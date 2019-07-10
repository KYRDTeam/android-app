package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.usecase.wallet.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface WalletRepository {
    fun createWallet(param: CreateWalletUseCase.Param): Single<Pair<Wallet, List<Word>>>

    fun createWallet(param: ApplyKyberCodeUseCase.Param): Single<Wallet>

    fun getMnemonic(param: GetMnemonicUseCase.Param): Single<List<Word>>

    fun importWallet(param: ImportWalletFromJsonUseCase.Param): Single<Wallet>

    fun importWallet(param: ImportWalletFromPrivateKeyUseCase.Param): Single<Wallet>

    fun importWallet(param: ImportWalletFromSeedUseCase.Param): Single<Wallet>

    fun getSelectedWallet(): Flowable<Wallet>

    fun getWalletByAddress(param: String): Flowable<Wallet>

    fun getAllWallet(): Flowable<List<Wallet>>

    fun getSelectedUnit(): Flowable<String>

    fun setSelectedUnit(unit: String): Completable

    fun updateWallet(param: Wallet): Completable

    fun addWalletToBalanceMonitor(param: AddWalletToBalanceMonitorUseCase.Param): Completable

    fun updatedSelectedWallet(param: UpdateSelectedWalletUseCase.Param): Single<Wallet>

    fun exportKeystore(param: ExportKeystoreWalletUseCase.Param): Single<String>

    fun exportPrivateKey(param: ExportPrivateKeyWalletUseCase.Param): Single<String>

    fun exportMnemonic(param: ExportMnemonicWalletUseCase.Param): Single<String>

    fun deleteWallet(param: DeleteWalletUseCase.Param): Single<VerifyStatus>
}
