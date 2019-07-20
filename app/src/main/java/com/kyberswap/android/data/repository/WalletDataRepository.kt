package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.PromoApi
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.UnitDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.mapper.PromoMapper
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.model.Unit
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.wallet.*
import com.kyberswap.android.util.HMAC
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
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject


class WalletDataRepository @Inject constructor(
    val context: Context,
    private val walletDao: WalletDao,
    private val unitDao: UnitDao,
    private val tokenDao: TokenDao,
    private val promoApi: PromoApi,
    private val promoMapper: PromoMapper
) : WalletRepository {

    override fun updatedSelectedWallet(param: UpdateSelectedWalletUseCase.Param): Single<Wallet> {
        return Single.fromCallable {
            updateSelectedWallet(param.wallet)
            updateWalletToMonitorBalance(param.wallet.copy(isSelected = true))
            param.wallet
        }
    }

    override fun updateWallet(param: Wallet): Completable {
        return Completable.fromCallable {
            walletDao.updateWallet(param)
        }
    }

    override fun getWalletByAddress(param: String): Flowable<Wallet> {
        return walletDao.loadWalletByAddress(param)
    }

    override fun setSelectedUnit(unit: String): Completable {
        return Completable.fromCallable {
            unitDao.updateUnit(Unit(unit))
        }
    }

    override fun getSelectedUnit(): Flowable<String> {
        return unitDao.unit.map { it.unit }
    }

    override fun getAllWallet(): Flowable<List<Wallet>> {
        return walletDao.allWalletsFlowable

    }

    override fun getSelectedWallet(): Flowable<Wallet> {
        if (walletDao.all.isEmpty()) {
            return Flowable.error(Throwable("empty"))
        }
        return walletDao.findSelectedWallet()

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
            updateWalletToMonitorBalance(wallet)
            wallet
        }
    }

    override fun importWallet(param: ImportWalletFromPrivateKeyUseCase.Param): Single<Wallet> {
        return Single.fromCallable {
            if (param.promo?.error != null && param.promo.error.isNotEmpty()) {
                Wallet(promo = param.promo)
            } else {
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
                    true
                )

                val wallet = Wallet(
                    importWalletFromPrivateKey.address.toWalletAddress(),
                    importWalletFromPrivateKey.id,
                    param.walletName,
                    cipher(generatedPassword),
                    true,
                    promo = param.promo
                )

                updateSelectedWallet(wallet)
                updateWalletToMonitorBalance(wallet)
                wallet
            }

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

            metadata.source = Metadata.FROM_KEYSTORE

            createIdentity(param.walletName, param.password)
            val importWalletFromKeystore = WalletManager.importWalletFromKeystore(
                metadata,
                keystoreContent,
                param.password,
                true
            )


            val wallet = Wallet(
                importWalletFromKeystore.address.toWalletAddress(),
                importWalletFromKeystore.id,
                param.walletName,
                cipher(param.password),
                true
            )

            updateSelectedWallet(wallet)
            updateWalletToMonitorBalance(wallet)
            wallet
        }
    }

    private fun updateWalletToMonitorBalance(wallet: Wallet): List<Token> {
        val tokens = tokenDao.allTokens.map {
            it.updateSelectedWallet(wallet)
        }
        tokenDao.updateTokens(tokens)
        return tokens
    }

    private fun updateSelectedWallet(wallet: Wallet): List<Wallet> {
        val wallets = mutableListOf<Wallet>()
        wallets.addAll(walletDao.all)
        if (wallets.find { it.address == wallet.address } == null) {
            wallets.add(wallet)
        }

        walletDao.batchUpdate(wallets.map {
            if (it.address == wallet.address) {
                it.copy(isSelected = true)
            } else {
                it.copy(isSelected = false)
            }
        })
        return wallets
    }

    override fun addWalletToBalanceMonitor(param: AddWalletToBalanceMonitorUseCase.Param): Completable {
        return Completable.fromCallable {
            updateWalletToMonitorBalance(param.wallet)
        }
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
        }
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
            }
            words
        }
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
            updateWalletToMonitorBalance(wallet)

            val words = mutableListOf<Word>()
            WalletManager.exportMnemonic(
                ethereumWallet.id,
                generatedPassword
            ).mnemonic.split(" ").forEachIndexed { index, s ->
                words.add(Word(index + 1, s))
            }

            Pair(wallet, words)
        }
    }

    override fun createWallet(param: ApplyKyberCodeUseCase.Param): Single<Wallet> {
        val privateKey = context.getString(R.string.kyber_code_api_key)
        val nonce = System.currentTimeMillis()
        val input = "code=${param.kyberCode}&isInternalApp=True&nonce=$nonce"
        val signedMessage = HMAC.hmacDigest(input, privateKey, "HmacSHA512")
        return promoApi.getPromo(
            signedMessage ?: "",
            "True",
            param.kyberCode,
            nonce
        ).map {
            promoMapper.transform(it)
        }.flatMap {
            importWallet(
                ImportWalletFromPrivateKeyUseCase.Param(
                    it.privateKey,
                    param.walletName,
                    it
                )
            )
        }
    }

    override fun exportKeystore(param: ExportKeystoreWalletUseCase.Param): Single<String> {
        return Single.fromCallable {
            var password = ""
            if (context is KyberSwapApplication) {
                password = String(
                    context.aead.decrypt(
                        Base64.decode(param.wallet.cipher, Base64.DEFAULT), ByteArray(0)
                    ), Charsets.UTF_8
                )
            }

            WalletManager.changePassword(param.wallet.walletId, password, param.password)
            val json = WalletManager.exportKeystore(param.wallet.walletId, param.password)
            WalletManager.changePassword(param.wallet.walletId, param.password, password)
            json
        }
    }

    override fun exportPrivateKey(param: ExportPrivateKeyWalletUseCase.Param): Single<String> {
        return Single.fromCallable {
            var password = ""
            if (context is KyberSwapApplication) {
                password = String(
                    context.aead.decrypt(
                        Base64.decode(param.wallet.cipher, Base64.DEFAULT), ByteArray(0)
                    ), Charsets.UTF_8
                )
            }

            WalletManager.exportPrivateKey(param.wallet.walletId, password)
        }
    }

    override fun exportMnemonic(param: ExportMnemonicWalletUseCase.Param): Single<String> {
        return Single.fromCallable {
            var password = ""
            if (context is KyberSwapApplication) {
                password = String(
                    context.aead.decrypt(
                        Base64.decode(param.wallet.cipher, Base64.DEFAULT), ByteArray(0)
                    ), Charsets.UTF_8
                )
            }

            WalletManager.exportMnemonic(param.wallet.walletId, password).mnemonic
        }
    }

    override fun deleteWallet(param: DeleteWalletUseCase.Param): Single<VerifyStatus> {
        return Single.fromCallable {
            val wallet = param.wallet
            val file =
                File(WalletManager.storage.keystoreDir.toString() + "/wallets/" + param.wallet.walletId + ".json")
            if (file.delete()) {
                val status = VerifyStatus(true)
                val wallets = walletDao.all.toMutableList()
                if (wallet.isSelected) {
                    val firstOrNull = wallets.firstOrNull()
                    firstOrNull?.let {
                        updateSelectedWallet(it)
                    }
                }
                wallets.remove(wallet)
                walletDao.deleteWallet(wallet)
                status.copy(isEmptyWallet = wallets.isEmpty())
            } else {
                VerifyStatus(false)
            }
        }
    }

    override fun saveWallet(param: SaveWalletUseCase.Param): Completable {
        return Completable.fromCallable {

            val currentWallet = walletDao.findWalletByAddress(param.wallet.address)
            walletDao.updateWallet(currentWallet.copy(name = param.wallet.name))
        }
    }
}