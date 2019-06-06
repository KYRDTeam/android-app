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
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.WalletManager
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.methods.response.EthEstimateGas
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
    private val tokenClient: TokenClient
) : SwapRepository {

    override fun saveSend(param: SaveSendUseCase.Param): Completable {
        return Completable.fromCallable {
            if (param.address.isNotBlank()) {
                val findContactByAddress = contactDao.findContactByAddress(param.address)
                val contact = findContactByAddress?.copy(
                    walletAddress = param.send.walletAddress,
                    address = param.address
                ) ?: Contact(param.send.walletAddress, param.address, DEFAULT_NAME)
                sendTokenDao.updateSend(param.send.copy(contact = contact))

            } else {
                sendTokenDao.updateSend(param.send)
            }
        }
    }

    override fun getSendData(param: GetSendTokenUseCase.Param): Flowable<Send> {
        return sendTokenDao.findSendByAddressFlowable(param.walletAddress)
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
            val resetSend = sendTokenDao.findSendByAddress(param.wallet.address)
            resetSend?.let {
                it.reset()
                sendTokenDao.updateSend(it)
            }

            hash

        }
    }

    override fun estimateGas(param: EstimateGasUseCase.Param): Single<EthEstimateGas> {
        return Single.fromCallable {
            tokenClient.estimateGas(
                param.wallet.address,
                context.getString(R.string.kyber_address),
                param.swap.tokenSource.tokenAddress,
                param.swap.tokenDest.tokenAddress,
                param.swap.sourceAmount.toBigDecimalOrDefaultZero().times(
                    10.0.pow(param.swap.tokenSource.tokenDecimal)
                        .toBigDecimal()
                ).toBigInteger(),
                param.swap.minConversionRate,
                param.swap.tokenSource.isETH()
            )
        }
    }

    override fun estimateGas(param: EstimateTransferGasUseCase.Param): Single<EthEstimateGas> {
        return Single.fromCallable {
            tokenClient.estimateGasForTransfer(
                param.wallet.address,
                param.send.tokenSource.tokenAddress,
                param.send.sourceAmount.toBigDecimalOrDefaultZero().times(
                    10.0.pow(param.send.tokenSource.tokenDecimal)
                        .toBigDecimal()
                ).toPlainString(),
                param.send.tokenSource.isETH()
            )
        }
    }

    override fun getCap(param: GetCapUseCase.Param): Single<Cap> {
        return api.getCap(param.walletAddress).map { capMapper.transform(it) }
            .doAfterSuccess { cap ->
                param.walletAddress?.let {
                    val wallet = walletDao.findWalletByAddress(it)
                    walletDao.updateWallet(wallet.copy(cap = cap))
                }
            }
    }

    override fun getGasPrice(): Single<Gas> {
        return api.getGasPrice().map { it.data }
            .map { mapper.transform(it) }
    }

    override fun saveSwap(param: SaveSwapUseCase.Param): Completable {
        return Completable.fromCallable {
            val swap = param.swap
            if (swap.gasLimit.isEmpty()) {
                val sourceToken = tokenDao.getTokenBySymbol(swap.tokenSource.tokenSymbol)
                swap.gasLimit = sourceToken?.gasLimit ?: swap.gasLimit
            }
            swapDao.insertSwap(param.swap)
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
            val currentSwapForWalletAddress =
                swapDao.findSwapDataByAddress(param.walletAddress).blockingFirst()
            val tokenBySymbol = tokenDao.getTokenBySymbol(param.token.tokenSymbol)
            val token = if (param.isSourceToken) {
                currentSwapForWalletAddress.copy(tokenSource = tokenBySymbol ?: Token())
            } else {
                currentSwapForWalletAddress.copy(tokenDest = tokenBySymbol ?: Token())
            }
            swapDao.updateSwap(token)
        }
    }

    override fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap> {
        val swap = swapDao.findSwapByAddress(param.walletAddress)
        val defaultSwap = if (swap == null) {
            val defaultSourceToken = tokenDao.getTokenBySymbol(Token.ETH)
            val defaultDestToken = tokenDao.getTokenBySymbol(Token.KNC)
            Swap(
                param.walletAddress,
                defaultSourceToken ?: Token(),
                defaultDestToken ?: Token(),
                sourceAmount = "",
                destAmount = "",
                expectedRate = "",
                slippageRate = ""
            )

        } else {
            val tokenSource = tokenDao.getTokenBySymbol(swap.tokenSource.tokenSymbol) ?: Token()
            val tokenDest = tokenDao.getTokenBySymbol(swap.tokenDest.tokenSymbol) ?: Token()
            swap.copy(tokenSource = tokenSource, tokenDest = tokenDest)
        }
        swapDao.insertSwap(defaultSwap)
        return swapDao.findSwapDataByAddress(param.walletAddress).defaultIfEmpty(
            defaultSwap
        )
    }

}