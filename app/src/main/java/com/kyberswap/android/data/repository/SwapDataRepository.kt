package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.db.*
import com.kyberswap.android.data.mapper.CapMapper
import com.kyberswap.android.data.mapper.GasMapper
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendUseCase
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.domain.usecase.swap.*
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
    private val transactionDao: TransactionDao
) : SwapRepository {
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


        val send = sendTokenDao.findSendByAddress(param.walletAddress)
        val defaultSend = if (send == null) {
            val defaultSourceToken = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()
            Send(
                param.walletAddress,
                defaultSourceToken
            )
        } else {
            send
        }
        sendTokenDao.insertSend(defaultSend)

        return sendTokenDao.findSendByAddressFlowable(param.walletAddress).defaultIfEmpty(
            defaultSend
        )
    }

    override fun swapToken(param: SwapTokenUseCase.Param): Single<String> {
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

            hash
        }
    }

    override fun transferToken(param: TransferTokenUseCase.Param): Single<String> {
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

            hash

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
                param.send.sourceAmount.toBigDecimalOrDefaultZero().times(
                    BigDecimal.TEN.pow(param.send.tokenSource.tokenDecimal)
                ).toBigInteger().toString(),
                param.send.tokenSource.isETH
            )
        }
    }

    override fun getCap(param: GetCapUseCase.Param): Single<Cap> {
        return api.getCap(param.walletAddress).map { capMapper.transform(it) }
            .doAfterSuccess { cap ->
                param.walletAddress?.let {
                    val wallet = walletDao.findWalletByAddress(it)
                    if (wallet.cap != cap) {
                        walletDao.updateWallet(wallet.copy(cap = cap))
                    }
                }
            }
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
        val wallet = param.wallet
        val alert = param.alert
        val swapByWalletAddress = swapDao.findSwapByAddress(wallet.address)
        val defaultSwap =
            when {
                alert != null -> {
                    val sourceToken = if (alert.base == Alert.BASE_USD) Token.ETH else alert.token
                    val destToken = if (alert.base == Alert.BASE_USD) Token.KNC else Token.ETH
                    val defaultSourceToken = tokenDao.getTokenBySymbol(sourceToken)
                    val defaultDestToken = tokenDao.getTokenBySymbol(destToken)
                    Swap(
                        wallet.address,
                        defaultSourceToken ?: Token(),
                        defaultDestToken ?: Token()
                    )
                }
                swapByWalletAddress == null -> {

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
                        tokenDao.getTokenBySymbol(swapByWalletAddress.tokenSource.tokenSymbol)
                            ?: Token()
                    val tokenDest =
                        tokenDao.getTokenBySymbol(swapByWalletAddress.tokenDest.tokenSymbol)
                            ?: Token()
                    swapByWalletAddress.copy(tokenSource = tokenSource, tokenDest = tokenDest)
                }
            }


        val swap = if (defaultSwap.tokenSource.selectedWalletAddress != wallet.address) {
            defaultSwap.copy(tokenSource = defaultSwap.tokenSource.updateSelectedWallet(wallet))
        } else {
            defaultSwap
        }

        val ethToken = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()
        val eth = if (ethToken.selectedWalletAddress != wallet.address) {
            ethToken.updateSelectedWallet(wallet)
        } else {
            ethToken
        }

        swapDao.insertSwap(swap)
        return swapDao.findSwapByAddressFlowable(wallet.address).defaultIfEmpty(
            swap
        ).map {
            it.ethToken = eth
            it
        }
    }

}