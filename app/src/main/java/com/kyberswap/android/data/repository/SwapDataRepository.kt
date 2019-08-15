package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.db.ContactDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.data.db.UserDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.mapper.CapMapper
import com.kyberswap.android.data.mapper.GasMapper
import com.kyberswap.android.data.mapper.UserMapper
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.Cap
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.model.EstimateAmount
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendUseCase
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateAmountUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateGasUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateTransferGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetCapUseCase
import com.kyberswap.android.domain.usecase.swap.GetCombinedCapUseCase
import com.kyberswap.android.domain.usecase.swap.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapUseCase
import com.kyberswap.android.domain.usecase.swap.SwapTokenUseCase
import com.kyberswap.android.presentation.common.DEFAULT_NAME
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.WalletManager
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.pow


class SwapDataRepository @Inject constructor(
    private val context: Context,
    private val walletDao: WalletDao,
    private val swapDao: SwapDao,
    private val tokenDao: TokenDao,
    private val sendTokenDao: SendDao,
    private val contactDao: ContactDao,
    private val api: SwapApi,
    private val mapper: GasMapper,
    private val capMapper: CapMapper,
    private val tokenClient: TokenClient,
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val userMapper: UserMapper
) : SwapRepository {
    override fun getCap(param: GetCombinedCapUseCase.Param): Single<Cap> {
        return Single.fromCallable {
            userDao.getUser() ?: UserInfo()
        }.flatMap {
            if (it.uid > 0) {
                userApi.getUserStats()
            } else {
                api.getCap(param.wallet.address)
            }
        }.map { capMapper.transform(it) }
    }

    override fun estimateAmount(param: EstimateAmountUseCase.Param): Single<EstimateAmount> {
        return api.sourceAmount(
            param.source,
            param.dest,
            param.destAmount
        )
    }

    override fun saveSend(param: SaveSendUseCase.Param): Completable {
        return Completable.fromCallable {
            val send = if (param.address.isNotBlank()) {
                val findContactByAddress = contactDao.findContactByAddress(param.address)
                val contact = findContactByAddress?.copy(
                    walletAddress = param.send.walletAddress,
                    address = param.address
                ) ?: Contact(param.send.walletAddress, param.address, DEFAULT_NAME)
                param.send.copy(contact = contact)
            } else {
                param.send
            }
            val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL) ?: Token()
            sendTokenDao.updateSend(send.copy(ethToken = ethToken))
        }
    }

    override fun getSendData(param: GetSendTokenUseCase.Param): Flowable<Send> {

        val wallet = param.wallet
        val send = sendTokenDao.findSendByAddress(wallet.address)
        val defaultSend = if (send == null) {
            val defaultSourceToken = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()
            Send(
                wallet.address,
                defaultSourceToken
            )
        } else {
            val tokenSource =
                tokenDao.getTokenBySymbol(send.tokenSource.tokenSymbol)
                    ?: Token()
            send.copy(tokenSource = tokenSource)
        }

        val sendByWallet =
            defaultSend.copy(tokenSource = defaultSend.tokenSource.updateSelectedWallet(wallet))

        val ethToken = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()
        val eth = ethToken.updateSelectedWallet(wallet)

        val updatedSend = sendByWallet.copy(ethToken = eth)
        sendTokenDao.insertSend(updatedSend)
        return sendTokenDao.findSendByAddressFlowable(param.wallet.address).defaultIfEmpty(
            updatedSend
        ).map {
            it.copy(ethToken = eth)
        }
    }

    override fun swapToken(param: SwapTokenUseCase.Param): Single<ResponseStatus> {
        return Single.fromCallable {
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
                WalletManager.storage.keystoreDir.toString() + "/wallets/" + param.wallet.walletId + ".json"
            )

            val hash = tokenClient.doSwap(
                param,
                credentials,
                context.getString(R.string.kyber_address)
            )
            val resetSwap = swapDao.findSwapByAddress(param.wallet.address)
            resetSwap?.let {
                it.reset()
                swapDao.updateSwap(it)
            }
            hash?.let {
                val swap = param.swap
                transactionDao.insertTransaction(
                    Transaction(
                        hash = it,
                        transactionStatus = Transaction.PENDING_TRANSACTION_STATUS,
                        timeStamp = System.currentTimeMillis() / 1000L,
                        from = swap.tokenSource.tokenAddress,
                        gas = swap.gasLimit,
                        gasUsed = swap.gasLimit,
                        gasPrice = Convert.toWei(
                            swap.gasPrice.toBigDecimalOrDefaultZero(),
                            Convert.Unit.GWEI
                        ).toString(),
                        to = swap.tokenDest.tokenAddress,
                        tokenSource = swap.tokenSource.tokenSymbol,
                        tokenDest = swap.tokenDest.tokenSymbol,
                        sourceAmount = swap.sourceAmount,
                        destAmount = swap.destAmount,
                        walletAddress = swap.walletAddress,
                        type = Transaction.TransactionType.SWAP
                    )
                )
            }

            hash ?: ""
        }.flatMap {
            userApi.submitTx(it)
        }.map {
            userMapper.transform(it)
        }
    }

    override fun transferToken(param: TransferTokenUseCase.Param): Single<ResponseStatus> {
        return Single.fromCallable {
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
                WalletManager.storage.keystoreDir.toString() + "/wallets/" + param.wallet.walletId + ".json"
            )

            val hash = tokenClient.doTransferTransaction(
                param,
                credentials
            )
            val resetSend = param.send.copy()
            resetSend.let {
                sendTokenDao.updateSend(
                    it.copy(
                        sourceAmount = ""
                    )
                )
            }

            hash?.let {
                val transfer = param.send
                transactionDao.insertTransaction(
                    Transaction(
                        hash = it,
                        transactionStatus = Transaction.PENDING_TRANSACTION_STATUS,
                        timeStamp = System.currentTimeMillis() / 1000L,
                        from = transfer.tokenSource.tokenAddress,
                        gas = transfer.gasLimit,
                        gasUsed = transfer.gasLimit,
                        gasPrice = Convert.toWei(
                            transfer.gasPrice.toBigDecimalOrDefaultZero(),
                            Convert.Unit.GWEI
                        ).toString(),
                        to = transfer.contact.address,
                        value = transfer.amountUnit.toString(),
                        tokenDecimal = transfer.tokenSource.tokenDecimal.toString(),
                        tokenSymbol = transfer.tokenSource.tokenSymbol,
                        walletAddress = param.send.walletAddress,
                        type = Transaction.TransactionType.SEND
                    )
                )
            }

            hash ?: ""

        }
            .flatMap {
                userApi.submitTx(it)
            }.map {
                userMapper.transform(it)
            }
    }

    override fun estimateGas(param: EstimateGasUseCase.Param): Single<EthEstimateGas> {
        return Single.fromCallable {
            tokenClient.estimateGas(
                param.wallet.address,
                context.getString(R.string.kyber_address),
                param.tokenSource.tokenAddress,
                param.tokenDest.tokenAddress,
                param.sourceAmount.toBigDecimalOrDefaultZero().times(
                    10.0.pow(param.tokenSource.tokenDecimal)
                        .toBigDecimal()
                ).toBigInteger(),
                param.minConversionRate,
                param.tokenSource.isETH
            )
        }
    }

    override fun estimateGas(param: EstimateTransferGasUseCase.Param): Single<EthEstimateGas> {
        return Single.fromCallable {
            tokenClient.estimateGasForTransfer(
                param.wallet.address,
                param.send.tokenSource.tokenAddress,
                param.send.estimateSource.toBigDecimalOrDefaultZero().times(
                    BigDecimal.TEN.pow(param.send.tokenSource.tokenDecimal)
                ).toBigInteger().toString(),
                param.send.tokenSource.isETH
            )
        }
    }

    override fun getCap(param: GetCapUseCase.Param): Single<Cap> {
        return api.getCap(param.walletAddress).map { capMapper.transform(it) }
    }

    override fun getGasPrice(): Flowable<Gas> {
        return api.getGasPrice().map { it.data }
            .map { mapper.transform(it) }
            .repeatWhen {
                it.delay(30, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    override fun saveSwap(param: SaveSwapUseCase.Param): Completable {
        return Completable.fromCallable {
            val swap = param.swap
            if (swap.gasLimit.isEmpty()) {
                val sourceToken = tokenDao.getTokenBySymbol(swap.tokenSource.tokenSymbol)
                swap.gasLimit = sourceToken?.gasLimit ?: swap.gasLimit
            }
            swapDao.insertSwap(swap)
        }
    }


    override fun saveSend(param: SaveSendTokenUseCase.Param): Completable {
        return Completable.fromCallable {
            val address = param.walletAddress
            val token = param.token
            sendTokenDao.add(Send(address, token))

        }
    }

    override fun saveSwapData(param: SaveSwapDataTokenUseCase.Param): Completable {
        return Completable.fromCallable {
            val swapByWalletAddress =
                swapDao.findSwapByAddressFlowable(param.walletAddress).blockingFirst()
            val tokenBySymbol = tokenDao.getTokenBySymbol(param.token.tokenSymbol)
            val swap = if (param.isSourceToken) {
                swapByWalletAddress.copy(tokenSource = tokenBySymbol ?: Token())
            } else {
                swapByWalletAddress.copy(tokenDest = tokenBySymbol ?: Token())
            }
            swapDao.updateSwap(swap)
        }
    }

    override fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap> {
        return Flowable.fromCallable {
            val wallet = param.wallet
            val alert = param.alert
            val localSwap = swapDao.findSwapByAddress(wallet.address)
            val defaultSwap =
                when {
                    alert != null -> {
                        val sourceToken =
                            if (alert.base == Alert.BASE_USD) Token.ETH else alert.token
                        val destToken = if (alert.base == Alert.BASE_USD) Token.KNC else Token.ETH
                        val defaultSourceToken = tokenDao.getTokenBySymbol(sourceToken)
                        val defaultDestToken = tokenDao.getTokenBySymbol(destToken)
                        Swap(
                            wallet.address,
                            defaultSourceToken ?: Token(),
                            defaultDestToken ?: Token()
                        )
                    }
                    localSwap == null -> {

                        val promo = wallet.promo
                        val sourceToken: String
                        val destToken: String
                        if (wallet.isPromo) {
                            sourceToken = context.getString(R.string.promo_source_token)
                            destToken =
                                if (promo?.destinationToken?.isNotEmpty() == true) promo.destinationToken else Token.KNC
                        } else {
                            sourceToken = Token.ETH
                            destToken = Token.KNC
                        }

                        val defaultSourceToken = tokenDao.getTokenBySymbol(sourceToken)
                        val defaultDestToken = tokenDao.getTokenBySymbol(destToken)

                        Swap(
                            wallet.address,
                            defaultSourceToken ?: Token(),
                            defaultDestToken ?: Token()
                        )
                    }
                    else -> {
                        val tokenSource =
                            tokenDao.getTokenBySymbol(localSwap.tokenSource.tokenSymbol)
                                ?: Token()
                        val tokenDest =
                            tokenDao.getTokenBySymbol(localSwap.tokenDest.tokenSymbol)
                                ?: Token()

                        val ethToken = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()
                        localSwap.copy(
                            tokenSource = tokenSource,
                            tokenDest = tokenDest,
                            ethToken = ethToken
                        )
                    }
                }

            val swap = defaultSwap.copy(
                tokenSource =
                if (wallet.address != defaultSwap.tokenSource.selectedWalletAddress) {
                    defaultSwap.tokenSource.updateSelectedWallet(wallet)
                } else {
                    defaultSwap.tokenSource
                }
                ,
                tokenDest =
                if (wallet.address != defaultSwap.tokenDest.selectedWalletAddress) {
                    defaultSwap.tokenDest.updateSelectedWallet(wallet)
                } else {
                    defaultSwap.tokenDest
                },
                ethToken = if (wallet.address != defaultSwap.ethToken.selectedWalletAddress) {
                    defaultSwap.ethToken.updateSelectedWallet(wallet)
                } else {
                    defaultSwap.ethToken
                }

            )
            swapDao.insertSwap(swap)
            swap
        }.flatMap { swap ->
            swapDao.findSwapByAddressFlowable(param.wallet.address).defaultIfEmpty(
                swap
            ).map {
                it.ethToken = swap.ethToken
                it
            }
        }
    }
}