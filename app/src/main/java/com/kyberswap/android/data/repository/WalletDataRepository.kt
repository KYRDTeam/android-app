package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.PromoApi
import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.db.ContactDao
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.NonceDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.data.db.UnitDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.mapper.PromoMapper
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.model.EligibleWalletStatus
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Unit
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.repository.WalletRepository
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
import com.kyberswap.android.presentation.main.kybercode.KyberCodeException
import com.kyberswap.android.util.HMAC
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.isApproveTx
import com.kyberswap.android.util.ext.isFromKyberSwap
import com.kyberswap.android.util.ext.isSwapTx
import com.kyberswap.android.util.ext.isTransferETHTx
import com.kyberswap.android.util.ext.isTransferTokenTx
import com.kyberswap.android.util.ext.toBigIntSafe
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toWalletAddress
import com.trustwallet.walletconnect.WCClient
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import com.trustwallet.walletconnect.models.session.WCSession
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.WalletManager
import org.consenlabs.tokencore.wallet.model.BIP44Util
import org.consenlabs.tokencore.wallet.model.ChainType
import org.consenlabs.tokencore.wallet.model.Metadata
import org.consenlabs.tokencore.wallet.model.Network
import org.web3j.crypto.WalletUtils
import org.web3j.utils.Convert
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.SecureRandom
import java.util.Locale
import javax.inject.Inject

class WalletDataRepository @Inject constructor(
    val context: Context,
    private val walletDao: WalletDao,
    private val unitDao: UnitDao,
    private val tokenDao: TokenDao,
    private val promoApi: PromoApi,
    private val promoMapper: PromoMapper,
    private val swapDao: SwapDao,
    private val sendDao: SendDao,
    private val limitOrderDao: LocalLimitOrderDao,
    private val wcClient: WCClient,
    private val tokenClient: TokenClient,
    private val transactionDao: TransactionDao,
    private val contactDao: ContactDao,
    private val nonceDao: NonceDao,
    private val userApi: UserApi
) : WalletRepository {

    override fun updatedSelectedWallet(param: UpdateSelectedWalletUseCase.Param): Single<Wallet> {
        return Single.fromCallable {
            updateWalletToMonitorBalance(updateSelectedWallet(param.wallet))
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
        return Flowable.fromCallable {
            walletDao.all.isEmpty()
        }.flatMap {
            if (it) {
                Flowable.error(Throwable("empty"))
            } else {
                walletDao.findSelectedWalletFlowable()
            }
        }
    }

    override fun importWallet(param: ImportWalletFromSeedUseCase.Param): Single<Pair<Wallet, List<Token>>> {
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
                false
            )

            val wallet = Wallet(
                importWalletFromMnemonic.address.toWalletAddress(),
                importWalletFromMnemonic.id,
                param.walletName,
                cipher(generatedPassword),
                isSelected = true,
                mnemonicAvailable = true,
                hasBackup = true
            )

            createContact(wallet)
            val tokens = updateWalletToMonitorBalance(updateSelectedWallet(wallet))
            Pair(wallet, tokens)
        }
    }

    private fun createContact(wallet: Wallet) {
        val contactByAddress =
            contactDao.findContactByAddress(wallet.address.toLowerCase(Locale.getDefault()))
        if (contactByAddress == null) {
            val contact = Contact(
                wallet.address,
                wallet.address.toLowerCase(Locale.getDefault()),
                wallet.name,
                System.currentTimeMillis() / 1000
            )
            contactDao.insertContact(contact)
        }
    }

    override fun importWallet(param: ImportWalletFromPrivateKeyUseCase.Param): Single<Pair<Wallet, List<Token>>> {
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
            val importWalletFromPrivateKey =
                if (param.promo?.error != null && param.promo.error.isNotEmpty()) {
                    WalletManager.importWalletFromPrivateKey(
                        metadata,
                        param.promo.privateKey,
                        generatedPassword,
                        false
                    )
                } else {
                    WalletManager.importWalletFromPrivateKey(
                        metadata,
                        param.privateKey,
                        generatedPassword,
                        param.promo != null
                    )
                }

            val wallet = Wallet(
                importWalletFromPrivateKey.address.toWalletAddress(),
                importWalletFromPrivateKey.id,
                param.walletName,
                cipher(generatedPassword),
                isSelected = true,
                promo = param.promo
            )
            val tokens = updateWalletToMonitorBalance(updateSelectedWallet(wallet))
            createContact(wallet)
            Pair(wallet, tokens)

        }
    }

    override fun importWallet(param: ImportWalletFromJsonUseCase.Param): Single<Pair<Wallet, List<Token>>> {
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
                false
            )

            val wallet = Wallet(
                importWalletFromKeystore.address.toWalletAddress(),
                importWalletFromKeystore.id,
                param.walletName,
                cipher(param.password),
                isSelected = true
            )

            val tokens = updateWalletToMonitorBalance(updateSelectedWallet(wallet))
            createContact(wallet)
            Pair(wallet, tokens)
        }
    }

    private fun updateWalletToMonitorBalance(wallets: List<Wallet>): List<Token> {
        val tokens = tokenDao.allTokens.map {
            it.updateSelectedWallet(wallets)
        }
        tokenDao.updateTokens(tokens)
        return tokens
    }

    private fun deleteWalletBalance(wallet: Wallet): List<Token> {
        val tokens = tokenDao.allTokens.map {
            it.deleteWallet(wallet)
        }
        tokenDao.updateTokens(tokens)
        return tokens
    }

    private fun updateSelectedWallet(wallet: Wallet): List<Wallet> {
        val wallets = walletDao.all.toMutableList()
        if (wallets.find { it.address == wallet.address } == null) {
            wallets.add(wallet)
        }

        val selectedWallets = wallets.map {
            if (it.address == wallet.address) {
                it.copy(isSelected = true, name = wallet.name)
            } else {
                it.copy(isSelected = false)
            }
        }
        walletDao.batchUpdate(selectedWallets)
        return selectedWallets
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

    override fun createWallet(param: CreateWalletUseCase.Param): Single<Triple<Wallet, List<Word>, List<Token>>> {

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
                isSelected = true,
                mnemonicAvailable = true
            )

            val tokens = updateWalletToMonitorBalance(updateSelectedWallet(wallet))
            createContact(wallet)
            val words = mutableListOf<Word>()
            WalletManager.exportMnemonic(
                ethereumWallet.id,
                generatedPassword
            ).mnemonic.split(" ").forEachIndexed { index, s ->
                words.add(Word(index + 1, s))
            }

            Triple(wallet, words, tokens)
        }
    }

    override fun createWallet(param: ApplyKyberCodeUseCase.Param): Single<Pair<Wallet, List<Token>>> {
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
            if (!it.error.isNullOrBlank()) {
                throw KyberCodeException(it.error)
            } else {
                promoMapper.transform(it)
            }
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
                File(param.wallet.walletPath)
            if (file.delete()) {
                val status = VerifyStatus(true)
                val wallets = walletDao.all.toMutableList()
                if (wallet.isSelected) {
                    val firstOrNull = wallets.firstOrNull {
                        it.address != wallet.address
                    }
                    firstOrNull?.let {
                        updateWalletToMonitorBalance(updateSelectedWallet(it))
                    }
                }
                wallets.remove(wallet)
                walletDao.deleteWallet(wallet)
                deleteWalletBalance(wallet)
                deleteLocalInfo(wallet)
                status.copy(isEmptyWallet = wallets.isEmpty())
            } else {
                VerifyStatus(false)
            }
        }
    }

    override fun walletConnect(param: WalletConnectUseCase.Param): Single<Boolean> {
        return Single.fromCallable {
            if (wcClient.isConnected) {
                wcClient.killSession()
            }
            val session = WCSession.from(param.sessionInfo)
            if (session != null) {
                val peerMetadata =
                    WCPeerMeta("Wallet Connect", "https://github.com/KyberSwap/android-app")
                wcClient.connect(session, peerMetadata)
                wcClient.onSessionRequest = param.onSessionRequest
                wcClient.onEthSendTransaction = param.onEthSendTransaction
                wcClient.onEthSign = param.onEthSign
                wcClient.onDisconnect = param.onDisconnect
                wcClient.onFailure = param.onFailure
            }
            session != null
        }
    }

    override fun approveSession(param: WalletConnectApproveSessionUseCase.Param): Single<Boolean> {
        return Single.fromCallable {
            wcClient.approveSesssion(listOf(param.walletAddress), param.chainId)
        }
    }

    override fun rejectSession(param: WalletConnectRejectSessionUseCase.Param): Single<Boolean> {
        return Single.fromCallable {
            wcClient.rejectSession()
        }
    }

    override fun killSession(param: WalletConnectKillSessionUseCase.Param): Single<Boolean> {
        return Single.fromCallable {
            wcClient.killSession()
        }
    }

    override fun sendTransaction(param: WalletConnectSendTransactionUseCase.Param): Completable {
        return Completable.fromCallable {
            var password = ""
            if (context is KyberSwapApplication) {
                password = String(
                    context.aead.decrypt(
                        Base64.decode(param.wallet.cipher, Base64.DEFAULT), ByteArray(0)
                    ), Charsets.UTF_8
                )
            }
            val credentials = WalletUtils.loadCredentials(
                password,
                param.wallet.walletPath
            )

            val (hash, nonce) = tokenClient.sendTransaction(param.transaction, credentials)

            hash?.let {
                val tx =
                    decodeData(param.transaction, it, param.wallet).copy(nonce = nonce.toString())
                val currentTx = transactionDao.findTransaction(tx.hash, tx.transactionStatus)
                if (currentTx != null) {
                    transactionDao.updateTransaction(tx)
                } else {
                    transactionDao.insertTransaction(tx)
                }

            }
            wcClient.approveRequest(param.id, hash)
        }
    }

    override fun rejectTransaction(param: WalletConnectRejectTransactionUseCase.Param): Single<Boolean> {
        return Single.fromCallable {
            wcClient.rejectRequest(param.id)
        }
    }

    override fun signTransaction(param: WalletConnectSignedTransactionUseCase.Param): Completable {
        return Completable.fromCallable {
            var password = ""
            if (context is KyberSwapApplication) {
                password = String(
                    context.aead.decrypt(
                        Base64.decode(param.wallet.cipher, Base64.DEFAULT), ByteArray(0)
                    ), Charsets.UTF_8
                )
            }
            val credentials = WalletUtils.loadCredentials(
                password,
                param.wallet.walletPath
            )

            val hash = tokenClient.signMessage(credentials, param.message)

            hash.let {
                wcClient.approveRequest(param.id, hash)
            }
        }
    }

    override fun decodeTransaction(param: DecodeTransactionUseCase.Param): Single<Transaction> {
        return Single.fromCallable {
            decodeData(param.message, "", param.wallet)
        }
    }

    override fun checkEligible(param: CheckEligibleWalletUseCase.Param): Single<EligibleWalletStatus> {
        return userApi.checkEligibleWallet(param.wallet.address).map {
            EligibleWalletStatus(it)
        }
    }

    private fun decodeData(
        wcEthereumTransaction: WCEthereumTransaction,
        hash: String,
        wallet: Wallet
    ): Transaction {
        val data = wcEthereumTransaction.data
        val methodId = data.take(10)
        val list = data.removePrefix(methodId).chunked(64)
        return if (wcEthereumTransaction.isFromKyberSwap()) {
            if (wcEthereumTransaction.isSwapTx()) {
                val tokenSourceAddress =
                    list[TRADE_WITH_HINT_SOURCE_POSITION].takeLast(40).toWalletAddress()
                val srcToken = tokenDao.getTokenByAddress(tokenSourceAddress) ?: Token()

                val sourceAmount = if (srcToken.tokenDecimal > 0) {
                    list[TRADE_WITH_HINT_SOURCE_AMOUNT_POSITION].toBigIntSafe()
                        .toBigDecimal().divide(
                            BigDecimal(10).pow(
                                srcToken.tokenDecimal
                            ), 18, RoundingMode.HALF_EVEN
                        )
                } else {
                    BigDecimal.ZERO
                }

                val tokenDestAddress =
                    list[TRADE_WITH_HINT_DEST_POSITION].takeLast(40).toWalletAddress()
                val destSymbol = tokenDao.getTokenByAddress(tokenDestAddress)?.symbol ?: ""
                val minConvertionRate = Convert.fromWei(
                    list[TRADE_WITH_HINT_MIN_CONVERSION_RATE_POSITION].toBigIntSafe().toString(),
                    Convert.Unit.ETHER
                )

                Transaction(
                    hash = hash.toLowerCase(Locale.getDefault()),
                    transactionStatus = Transaction.PENDING_TRANSACTION_STATUS,
                    timeStamp = System.currentTimeMillis() / 1000L,
                    from = wcEthereumTransaction.from,
                    gas = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                    gasUsed = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                    gasPrice = wcEthereumTransaction.gasPrice?.toBigIntSafe().toString(),
                    to = wcEthereumTransaction.from,
                    tokenSource = srcToken.tokenSymbol,
                    tokenDest = destSymbol,
                    sourceAmount = sourceAmount.toDisplayNumber(),
                    destAmount = sourceAmount.multiply(minConvertionRate).toDisplayNumber(),
                    walletAddress = wallet.address,
                    type = Transaction.TransactionType.SWAP
                )
            } else if (wcEthereumTransaction.isApproveTx()) {
                val token = tokenDao.getTokenByAddress(wcEthereumTransaction.to ?: "") ?: Token()
                Transaction(
                    hash = hash.toLowerCase(Locale.getDefault()),
                    transactionStatus = Transaction.PENDING_TRANSACTION_STATUS,
                    timeStamp = System.currentTimeMillis() / 1000L,
                    from = wcEthereumTransaction.from,
                    gas = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                    gasUsed = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                    gasPrice = wcEthereumTransaction.gasPrice?.toBigIntSafe().toString(),
                    to = wcEthereumTransaction.to ?: "",
                    value = wcEthereumTransaction.value?.toBigIntSafe().toString(),
                    tokenDecimal = 18.toString(),
                    tokenSymbol = Token.ETH_SYMBOL,
                    tokenSource = token.tokenSymbol,
                    walletAddress = wallet.address,
                    type = Transaction.TransactionType.SEND
                )
            } else {
                val token = if (wcEthereumTransaction.isTransferTokenTx()) {
                    tokenDao.getTokenByAddress(wcEthereumTransaction.to ?: "") ?: Token()
                } else {
                    tokenDao.getTokenBySymbol(Token.ETH_SYMBOL) ?: Token()
                }

                Transaction(
                    hash = hash.toLowerCase(Locale.getDefault()),
                    transactionStatus = Transaction.PENDING_TRANSACTION_STATUS,
                    timeStamp = System.currentTimeMillis() / 1000L,
                    from = wcEthereumTransaction.from,
                    gas = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                    gasUsed = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                    gasPrice = wcEthereumTransaction.gasPrice?.toBigIntSafe().toString(),
                    to = if (wcEthereumTransaction.isTransferETHTx()) {
                        wcEthereumTransaction.to
                    } else {
                        list[TRANFER_TOKEN_ADDRESS_POSITION].takeLast(40).toWalletAddress()
                    } ?: "",
                    value = if (wcEthereumTransaction.isTransferETHTx()) {
                        wcEthereumTransaction.value?.toBigIntSafe().toString()
                    } else {
                        list[TRANFER_TOKEN_AMOUNT_POSITION].toBigIntSafe().toString()
                    },
                    tokenDecimal = token.tokenDecimal.toString(),
                    tokenSymbol = token.tokenSymbol,
                    walletAddress = wallet.address,
                    type = Transaction.TransactionType.SEND
                )
            }
        } else {
            Transaction(
                hash = hash.toLowerCase(Locale.getDefault()),
                transactionStatus = Transaction.PENDING_TRANSACTION_STATUS,
                timeStamp = System.currentTimeMillis() / 1000L,
                from = wcEthereumTransaction.from,
                gas = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                gasUsed = wcEthereumTransaction.gasLimit?.toBigIntSafe().toString(),
                gasPrice = wcEthereumTransaction.gasPrice?.toBigIntSafe().toString(),
                to = wcEthereumTransaction.to ?: "",
                value = wcEthereumTransaction.value?.toBigIntSafe().toString(),
                tokenDecimal = 18.toString(),
                tokenSymbol = Token.ETH_SYMBOL,
                walletAddress = wallet.address,
                type = Transaction.TransactionType.SEND
            )
        }
    }

    private fun deleteLocalInfo(wallet: Wallet) {
        val currentSwap = swapDao.findSwapByAddress(wallet.address)
        currentSwap?.let {
            swapDao.delete(currentSwap)
        }
        val currentSend = sendDao.findSendByAddress(wallet.address)
        currentSend?.let {
            sendDao.delete(currentSend)
        }
        val currentLimitOrder = limitOrderDao.findLocalLimitOrderByAddress(wallet.address)
        currentLimitOrder?.let {
            limitOrderDao.delete(currentLimitOrder)
        }
        nonceDao.deleteNonces(wallet.address)
    }

    override fun saveWallet(param: SaveWalletUseCase.Param): Completable {
        return Completable.fromCallable {

            val currentWallet = walletDao.findWalletByAddress(param.wallet.address)
            walletDao.updateWallet(currentWallet.copy(name = param.wallet.name))
        }
    }

    companion object {
        /**
         * Define position for tradeWithHintFunction
         * Function: tradeWithHint(
         * address src, uint256 srcAmount, address dest, address destAddress, uint256 maxDestAmount, uint256 minConversionRate, address walletId, bytes hint)
         *
         *
         * Function: tradeWithHintAndFee(
         * address src, uint256 srcAmount, address dest, address destAddress, uint256 maxDestAmount, uint256 minConversionRate, address platformWallet, uint256 platformFeeBps, bytes hint)
         */

        const val METHOD_ID_SWAP = "0x29589f61"
        const val METHOD_ID_TRADE_WITH_HINT_AND_FEE = "0xae591d54"
        const val METHOD_ID_TRANSFER = "0xa9059cbb"
        const val METHOD_ID_APPROVE = "0x095ea7b3"
        const val TRADE_WITH_HINT_SOURCE_POSITION = 0
        const val TRADE_WITH_HINT_SOURCE_AMOUNT_POSITION = 1
        const val TRADE_WITH_HINT_DEST_POSITION = 2
        const val TRADE_WITH_HINT_DEST_ADDRESS_POSITION = 3
        const val TRADE_WITH_HINT_MAX_DEST_AMOUNT_POSITION = 4
        const val TRADE_WITH_HINT_MIN_CONVERSION_RATE_POSITION = 5
        const val TRADE_WITH_HINT_WALLET_ID_POSITION = 6
        const val TRADE_WITH_HINT_AND_FEE_PLATFORM_FEE_POSITION = 7
        const val TRANFER_TOKEN_ADDRESS_POSITION = 0
        const val TRANFER_TOKEN_AMOUNT_POSITION = 1
    }
}