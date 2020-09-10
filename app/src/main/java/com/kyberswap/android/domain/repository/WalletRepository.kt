package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.EligibleWalletStatus
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletConnect
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.usecase.wallet.ApplyKyberCodeUseCase
import com.kyberswap.android.domain.usecase.wallet.CheckEligibleWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.DeleteWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.ExportKeystoreWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.ExportMnemonicWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.ExportPrivateKeyWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetMnemonicUseCase
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromJsonUseCase
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromPrivateKeyUseCase
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromSeedUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.walletconnect.DecodeTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectApproveSessionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectKillSessionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectRejectSessionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectRejectTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectSendTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectSignedTransactionUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectUseCase
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

    fun walletConnect(param: WalletConnectUseCase.Param): Single<Boolean>

    fun approveSession(param: WalletConnectApproveSessionUseCase.Param): Single<Boolean>

    fun rejectSession(param: WalletConnectRejectSessionUseCase.Param): Single<Boolean>

    fun killSession(param: WalletConnectKillSessionUseCase.Param): Single<Boolean>

    fun sendTransaction(param: WalletConnectSendTransactionUseCase.Param): Completable

    fun rejectTransaction(param: WalletConnectRejectTransactionUseCase.Param): Single<Boolean>

    fun signTransaction(param: WalletConnectSignedTransactionUseCase.Param): Completable

    fun decodeTransaction(param: DecodeTransactionUseCase.Param): Single<Transaction>

    fun checkEligible(param: CheckEligibleWalletUseCase.Param): Single<EligibleWalletStatus>

    fun updateWalletConnect(param: WalletConnect): Completable

    fun getWalletConnect(address: String): Flowable<WalletConnect>

}
