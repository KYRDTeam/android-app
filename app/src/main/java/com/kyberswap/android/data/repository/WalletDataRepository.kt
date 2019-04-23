package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.wallet.*
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.Wallet
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.BIP44Util
import org.consenlabs.tokencore.wallet.model.ChainType
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import javax.inject.Inject


class WalletDataRepository @Inject constructor(val context: Context) : WalletRepository {
    override fun importWallet(param: ImportWalletFromSeedUseCase.Param): Single<Wallet> {
        return Single.fromCallable {
            val metadata =
                Metadata(
                    ChainType.ETHEREUM,
                    Network.MAINNET,
                    param.walletName,
                    null
                )
            createIdentityWhenNull(param.walletName, "")
            val importWalletFromMnemonic = WalletManager.importWalletFromMnemonic(
                metadata,
                param.seed,
                BIP44Util.ETHEREUM_PATH,
                "",
                false
            )
            importWalletFromMnemonic
        }
    }

    override fun importWallet(param: ImportWalletFromPrivateKeyUseCase.Param): Single<Wallet> {
        return Single.fromCallable {
            val metadata =
                Metadata(
                    ChainType.ETHEREUM,
                    Network.MAINNET,
                    param.walletName,
                    null
                )
            metadata.source = Metadata.FROM_PRIVATE
            createIdentityWhenNull(param.walletName, "")
            WalletManager.importWalletFromPrivateKey(
                metadata,
                param.privateKey,
                "",
                false
            )
        }
    }

    override fun importWallet(param: ImportWalletFromJsonUseCase.Param): Single<Wallet> {
        return Single.fromCallable {
            val openInputStream = context.contentResolver?.openInputStream(param.uri)
            val keystoreContent = openInputStream?.bufferedReader().use { it?.readText() }
            val metadata =
                Metadata(
                    ChainType.ETHEREUM,
                    Network.MAINNET,
                    param.walletName,
                    null
                )

            createIdentityWhenNull(param.walletName, param.password)
            val importWalletFromKeystore = WalletManager.importWalletFromKeystore(
                metadata,
                keystoreContent,
                param.password,
                false
            )
            importWalletFromKeystore
        }
    }

    private fun createIdentityWhenNull(name: String, password: String) {
        val identity = Identity.getCurrentIdentity()
        if (identity == null) {
            Identity.createIdentity(
                name,
                password,
                null,
                Network.MAINNET,
                Metadata.P2WPKH
            )

        }
    }


    override fun getMnemonic(param: GetMnemonicUseCase.Param): Single<List<Word>> {
        return Single.fromCallable {
            val words = mutableListOf<Word>()
            WalletManager.exportMnemonic(
                param.walletId,
                param.password
            ).mnemonic.split(" ").forEachIndexed { index, s ->
                words.add(Word(index + 1, s))
            }
            words

        }
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
}