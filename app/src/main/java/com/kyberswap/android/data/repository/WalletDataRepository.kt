package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.data.db.UnitDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.domain.model.Unit
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.wallet.*
import com.kyberswap.android.util.ext.toWalletAddress
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.Wallet
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.BIP44Util
import org.consenlabs.tokencore.wallet.model.ChainType
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import javax.inject.Inject

class WalletDataRepository @Inject constructor(
    val context: Context,
    private val walletDao: WalletDao,
    private val unitDao: UnitDao,
    private val mediator: StorageMediator
) : WalletRepository {

    override fun updateWallet(param: com.kyberswap.android.domain.model.Wallet): Completable {
        return Completable.fromCallable {
            walletDao.updateWallet(param)

    }

    override fun getWalletByAddress(param: String): Flowable<com.kyberswap.android.domain.model.Wallet> {
        return walletDao.loadWalletByAddress(param)
    }

    override fun setSelectedUnit(unit: String): Completable {
        return Completable.fromCallable {
            unitDao.updateUnit(Unit(unit))

    }

    override fun getSelectedUnit(): Flowable<String> {
        return unitDao.unit.map { it.unit }
    }

    override fun getAllWallet(): Single<List<com.kyberswap.android.domain.model.Wallet>> {
        return Single.fromCallable {
            walletDao.all


    }

    override fun getSelectedWallet(): Single<com.kyberswap.android.domain.model.Wallet> {
        return Single.fromCallable {
            val all = walletDao.all
            var selectedWallet = all.firstOrNull { wallet -> wallet.isSelected }
            if (selectedWallet == null) {
                selectedWallet = all.first()
    
            selectedWallet


    }

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
            walletDao.insertWallet(
                com.kyberswap.android.domain.model.Wallet(
                    importWalletFromMnemonic.address.toWalletAddress(), param.walletName
                )
            )
            importWalletFromMnemonic

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
            val importWalletFromPrivateKey = WalletManager.importWalletFromPrivateKey(
                metadata,
                param.privateKey,
                "",
                false
            )
            walletDao.insertWallet(
                com.kyberswap.android.domain.model.Wallet(
                    importWalletFromPrivateKey.address.toWalletAddress(), param.walletName
                )
            )
            importWalletFromPrivateKey

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

            walletDao.insertWallet(
                com.kyberswap.android.domain.model.Wallet(
                    importWalletFromKeystore.address.toWalletAddress(), param.walletName
                )
            )
            importWalletFromKeystore

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
                    "",
                    param.pinLock,
                    "",
                    Network.MAINNET,
                    Metadata.P2WPKH
                )
            val wallet = identity.wallets[0]
            walletDao.insertWallet(
                com.kyberswap.android.domain.model.Wallet(
                    wallet.address.toWalletAddress(),
                    "",
                    true
                )
            )
            wallet

    }
}