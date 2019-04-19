package com.kyberswap.android.data.repository

import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetMnemonicUseCase
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.Wallet
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import javax.inject.Inject


class WalletDataRepository @Inject constructor() : WalletRepository {

    override fun getMnemonic(param: GetMnemonicUseCase.Param): Single<List<Word>> {
        return Single.fromCallable {
            val words = mutableListOf<Word>()
            WalletManager.exportMnemonic(
                param.walletId,
                param.password
            ).mnemonic.split(" ").forEachIndexed { index, s ->
                words.add(Word(index + 1, s))
    
            words


    }

    override fun createWallet(param: CreateWalletUseCase.Param): Single<Wallet> {

        return Single.fromCallable {
            val identity =
                Identity.createIdentity(
                    null,
                    param.pinLock,
                    null,
                    Network.MAINNET,
                    Metadata.P2WPKH
                )
            identity.wallets[0]

    }
}