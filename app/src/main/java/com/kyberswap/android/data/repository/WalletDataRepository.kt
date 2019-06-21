package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.UnitDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.model.Unit
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.wallet.*
import com.kyberswap.android.util.ext.toWalletAddress
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.BIP44Util
import org.consenlabs.tokencore.wallet.model.ChainType
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import java.math.BigDecimal
import java.security.SecureRandom
import javax.inject.Inject

class WalletDataRepository @Inject constructor(
    val context: Context,
    private val walletDao: WalletDao,
    private val unitDao: UnitDao,
    private val tokenDao: TokenDao
) : WalletRepository {

    override fun updateWallet(param: Wallet): Completable {
        return Completable.fromCallable {
            walletDao.updateWallet(param)

    }

    override fun getWalletByAddress(param: String): Flowable<Wallet> {
        return walletDao.loadWalletByAddress(param)
    }

    override fun setSelectedUnit(unit: String): Completable {
        return Completable.fromCallable {
            unitDao.updateUnit(Unit(unit))

    }

    override fun getSelectedUnit(): Flowable<String> {
        return unitDao.unit.map { it.unit }
    }

    override fun getAllWallet(): Flowable<List<Wallet>> {
        return walletDao.allWalletsFlowable

    }

    override fun getSelectedWallet(): Single<Wallet> {
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

            val generatedPassword = generatePassword()
            createIdentity(param.walletName, generatedPassword)
            val importWalletFromMnemonic = WalletManager.importWalletFromMnemonic(
                metadata,
                param.seed,
                BIP44Util.ETHEREUM_PATH,
                generatedPassword,
                true
            )

            val wallet = Wallet(
                importWalletFromMnemonic.address.toWalletAddress(),
                importWalletFromMnemonic.id,
                param.walletName,
                cipher(generatedPassword),
                true
            )
            updateSelectedWallet(wallet)
            addWalletToMonitorBalance(wallet)
            wallet

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

            val generatedPassword = generatePassword()

            createIdentity(param.walletName, generatedPassword)
            val importWalletFromPrivateKey = WalletManager.importWalletFromPrivateKey(
                metadata,
                param.privateKey,
                generatedPassword,
                false
            )

            val wallet = Wallet(
                importWalletFromPrivateKey.address.toWalletAddress(),
                importWalletFromPrivateKey.id,
                param.walletName,
                cipher(generatedPassword),
                true
            )

            updateSelectedWallet(wallet)
            addWalletToMonitorBalance(wallet)
            wallet

    }

    private fun addWalletToMonitorBalance(wallet: Wallet): List<Token> {
        val tokens = tokenDao.allTokens.map {
            val walletBalances = it.wallets.toMutableList()

            if (it.wallets.find { it.walletAddress == wallet.address } == null) {
                walletBalances.add(
                    WalletBalance(
                        wallet.address,
                        BigDecimal.ZERO,
                        wallet.isSelected
                    )
                )
    

            it.copy(wallets = walletBalances)

        tokenDao.updateTokens(tokens)
        return tokens
    }

    private fun updateSelectedWallet(wallet: Wallet): List<Wallet> {
        val wallets = walletDao.all.map {
            if (it.address == wallet.address) {
                it.copy(isSelected = true)
     else {
                it.copy(isSelected = false)
    
.toMutableList()

        if (wallets.find { it.address == wallet.address } == null) {
            wallets.add(wallet)


        walletDao.batchUpdate(wallets)
        return wallets
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

            createIdentity(param.walletName, param.password)
            val importWalletFromKeystore = WalletManager.importWalletFromKeystore(
                metadata,
                keystoreContent,
                param.password,
                false
            )

            val cipher = cipher(param.password)
            val wallet = Wallet(
                importWalletFromKeystore.address.toWalletAddress(),
                importWalletFromKeystore.id,
                param.walletName,
                cipher,
                true
            )

            updateSelectedWallet(wallet)
            addWalletToMonitorBalance(wallet)
            wallet

    }

    override fun addWalletToBalanceMonitor(param: AddWalletToBalanceMonitorUseCase.Param): Completable {
        return Completable.fromCallable {
            addWalletToMonitorBalance(param.wallet)

    }

    @Throws(Exception::class)
    private fun cipher(password: String): String {
        if (context is KyberSwapApplication) {
            return Base64.encodeToString(
                context.aead.encrypt(
                    password.toByteArray(Charsets.UTF_8),
                    ByteArray(0)
                ), Base64.DEFAULT
            )

        return ""
    }

    private fun createIdentity(name: String, password: String): Identity {
        return Identity.createIdentity(
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

    private fun generatePassword(): String {
        val bytes = ByteArray(16)
        val random = SecureRandom()
        random.nextBytes(bytes)
        Base64.encodeToString(bytes, Base64.DEFAULT)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun createWallet(param: CreateWalletUseCase.Param): Single<Pair<Wallet, List<Word>>> {

        return Single.fromCallable {

            val generatedPassword = generatePassword()

            val identity =
                Identity.createIdentity(
                    param.walletName,
                    generatedPassword,
                    "",
                    Network.MAINNET,
                    Metadata.P2WPKH
                )
            val ethereumWallet = identity.wallets[0]
            val wallet = Wallet(
                ethereumWallet.address.toWalletAddress(),
                ethereumWallet.id,
                param.walletName,
                cipher(generatedPassword),
                true
            )

            updateSelectedWallet(wallet)
            addWalletToMonitorBalance(wallet)

            val words = mutableListOf<Word>()
            WalletManager.exportMnemonic(
                ethereumWallet.id,
                generatedPassword
            ).mnemonic.split(" ").forEachIndexed { index, s ->
                words.add(Word(index + 1, s))
    

            Pair(wallet, words)

    }
}