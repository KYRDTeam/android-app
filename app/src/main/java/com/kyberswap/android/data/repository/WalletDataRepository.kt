package com.kyberswap.android.data.repository

import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.Wallet
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import javax.inject.Inject

class WalletDataRepository @Inject constructor() : WalletRepository {
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