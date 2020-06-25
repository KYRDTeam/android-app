package com.kyberswap.android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.BuildConfig
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.db.NonceDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Nonce
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SwapTokenUseCase
import com.kyberswap.android.presentation.common.DEFAULT_MAX_AMOUNT
import com.kyberswap.android.presentation.common.DEFAULT_WALLET_ID
import com.kyberswap.android.presentation.common.PERM
import com.kyberswap.android.presentation.common.PLATFORM_FEE_BPS
import com.kyberswap.android.presentation.common.calculateDefaultGasLimit
import com.kyberswap.android.presentation.common.calculateDefaultGasLimitTransfer
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.fromAddress
import com.kyberswap.android.util.ext.isFromKyberSwap
import com.kyberswap.android.util.ext.isSwapTx
import com.kyberswap.android.util.ext.isTransferETHTx
import com.kyberswap.android.util.ext.minConversionRate
import com.kyberswap.android.util.ext.params
import com.kyberswap.android.util.ext.shortenValue
import com.kyberswap.android.util.ext.toAddress
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntSafe
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.transferAmount
import com.kyberswap.android.util.ext.transferToAddress
import com.kyberswap.android.util.ext.txValue
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.Hash
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.Sign
import org.web3j.crypto.WalletUtils
import org.web3j.ens.EnsResolver
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.tx.RawTransactionManager
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.ArrayList
import javax.inject.Inject
import kotlin.math.pow


class TokenClient @Inject constructor(
    private val web3jAlchemyNode: Web3j,
    private val web3jSemiNode: Web3j,
    private val web3jInfuraNode: Web3j,
    private val tokenDao: TokenDao,
    private val transactionDao: TransactionDao,
    private val nonceDao: NonceDao,
    private val context: Context,
    private val analytics: FirebaseAnalytics
) {

    private val event = Event(
        "ExecuteTrade",
        listOf<TypeReference<*>>(
            object : TypeReference<Address>() {
            },
            object : TypeReference<Address>() {
            },
            object : TypeReference<Uint256>() {
            },
            object : TypeReference<Uint256>() {
            }
        ))

    private val executeTradeEvent = Event(
        "ExecuteTrade",
        listOf<TypeReference<*>>(
            object : TypeReference<Address>() {
            },
            object : TypeReference<Address>() {
            },
            object : TypeReference<Address>() {
            },
            object : TypeReference<Uint256>() {
            },
            object : TypeReference<Uint256>() {
            },
            object : TypeReference<Address>() {
            },
            object : TypeReference<Uint256>() {
            }
        ))

    private fun balanceOf(owner: String): Function {
        return Function(
            "balanceOf",
            listOf(Address(owner)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {
            })
        )
    }

    private fun executeTrade(): Function {
        return Function(
            "executeTrade",
            listOf(),
            listOf<TypeReference<*>>(
                object : TypeReference<DynamicArray<Uint256>>() {
                }
            ))
    }

    private fun getExpectedRate(
        srcToken: String,
        destToken: String,
        srcTokenAmount: BigInteger
    ): Function {
        val amount = srcTokenAmount or 2.toBigInteger().pow(255)
        return Function(
            "getExpectedRate",
            listOf(
                Address(srcToken),
                Address(destToken),
                Uint256(amount)
            ),
            listOf<TypeReference<*>>(
                object : TypeReference<Uint256>() {
                },
                object : TypeReference<Uint256>() {

                })
        )
    }

    private fun getExpectedRateAfterFee(
        srcToken: String,
        destToken: String,
        srcTokenAmount: BigInteger,
        platformFeeBps: BigInteger = PLATFORM_FEE_BPS.toBigInteger(),
        hint: String = ""
    ): Function {
        return Function(
            "getExpectedRateAfterFee",
            listOf(
                Address(srcToken),
                Address(destToken),
                Uint256(srcTokenAmount),
                Uint256(platformFeeBps),
                DynamicBytes(hint.toByteArray())
            ),
            listOf<TypeReference<*>>(
                object : TypeReference<Uint256>() {
                })
        )
    }

    @Throws(Exception::class)
    fun getEthBalance(owner: String?): BigInteger {
        if (owner == null) return BigInteger.ZERO
        return web3jAlchemyNode
            .ethGetBalance(
                owner,
                DefaultBlockParameterName.LATEST
            )
            .send()
            .balance
    }

    @Throws(Exception::class)
    private fun callSmartContractFunction(
        function: Function,
        contractAddress: String,
        fromAddress: String?,
        isOtherToken: Boolean = false
    ): String? {
        val encodedFunction = FunctionEncoder.encode(function)
        val response = if (isOtherToken) {
            web3jSemiNode.ethCall(
                Transaction.createEthCallTransaction(fromAddress, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            )
                .send()
        } else {
            web3jAlchemyNode.ethCall(
                Transaction.createEthCallTransaction(fromAddress, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            )
                .send()
        }
        return response?.value
    }

    @Throws(Exception::class)
    fun updateBalance(
        walletAddress: String?,
        tokenAddress: String?,
        isOtherToken: Boolean = false
    ): BigDecimal? {
        if (walletAddress == null || tokenAddress == null) return BigDecimal.ZERO
        val function = balanceOf(walletAddress)
        val responseValue = callSmartContractFunction(function, tokenAddress, null, isOtherToken)

        val response = FunctionReturnDecoder.decode(
            responseValue, function.outputParameters
        )

        return if (response.size == 1) {
            BigDecimal((response[0] as Uint256).value)
        } else {
            null
        }
    }

    @Throws(Exception::class)
    private fun getBalances(
        address: String,
        tokens: List<String>
    ): Function {

        return Function(
            "getBalances",
            listOf(
                Address(address),
                DynamicArray(Address::class.java, tokens.map { Address(it) })
            ),
            listOf<TypeReference<*>>(
                object : TypeReference<DynamicArray<Uint256>>() {
                }
            ))
    }

    @Throws(Exception::class)
    fun updateBalance(token: Token): Token {
        return token.updateBalance(
            if (token.isETH) {
                Convert.fromWei(BigDecimal(getEthBalance(token.owner)), Convert.Unit.ETHER)
            } else {
                (updateBalance(token.owner, token.tokenAddress, token.isOther)
                    ?: BigDecimal.ZERO).divide(
                    BigDecimal(10).pow(
                        token.tokenDecimal
                    ), 18, RoundingMode.HALF_EVEN
                )
            }
        )
    }

    @Throws(Exception::class)
    fun updateBalances(
        contractAddress: String,
        tokens: List<Token>
    ): List<Token> {
        try {
            val ethPosition = tokens.indexOfFirst { it.isETH }
            val ethToken = tokens[ethPosition]
            val walletAddress = ethToken.selectedWalletAddress
            val erc20Tokens = tokens.toMutableList()
            erc20Tokens.remove(ethToken)

            val eth = ethToken.updateBalance(
                Convert.fromWei(
                    BigDecimal(getEthBalance(walletAddress)),
                    Convert.Unit.ETHER
                )
            )

            val balances = getBalances(walletAddress, erc20Tokens.map { it.tokenAddress })
            val responseValueBalances = callSmartContractFunction(
                balances,
                contractAddress,
                walletAddress
            )
            val responseBalance = FunctionReturnDecoder.decode(
                responseValueBalances, balances.outputParameters
            )
            val erc20List = if (responseBalance.size > 0) {
                val tokenBalances = responseBalance[0].value as List<Uint256>
                erc20Tokens.mapIndexed { index, token ->
                    token.updateBalance(
                        BigDecimal(tokenBalances[index].value).divide(
                            BigDecimal(10).pow(
                                token.tokenDecimal
                            ), 18, RoundingMode.HALF_EVEN
                        )
                    )
                }.toMutableList()
            } else {
                erc20Tokens
            }

            erc20List.add(ethPosition, eth)
            return erc20List
        } catch (ex: Exception) {
            ex.printStackTrace()
            return tokens
        }
    }

    @Throws(Exception::class)
    fun getExpectedRate(
        contractAddress: String,
        tokenSource: Token,
        tokenDest: Token,
        srcTokenAmount: BigInteger
    ): List<String> {
        val function =
            if (BuildConfig.FLAVOR == "dev") {
                getExpectedRateAfterFee(
                    tokenSource.tokenAddress,
                    tokenDest.tokenAddress,
                    srcTokenAmount
                )
            } else {
                getExpectedRate(
                    tokenSource.tokenAddress,
                    tokenDest.tokenAddress,
                    srcTokenAmount
                )
            }

        val responseValue = callSmartContractFunction(function, contractAddress, null)

        val responses = FunctionReturnDecoder.decode(
            responseValue, function.outputParameters
        )
        val rateResult = ArrayList<String>()
        for (rates in responses) {
            val rate = rates as Uint256
            val toEther =
                Convert.fromWei(
                    BigDecimal(rate.value),
                    Convert.Unit.ETHER
                )
            rateResult.add(
                toEther.toPlainString()
            )
        }

        return rateResult
    }

    @Throws(java.lang.Exception::class)
    fun estimateGas(
        walletAddress: String,
        contractAddress: String,
        fromAddress: String,
        toAddress: String,
        amount: BigInteger,
        minConversionRate: BigInteger,
        isEth: Boolean
    ): EthEstimateGas? {

        val function =
            if (BuildConfig.FLAVOR == "dev") {
                tradeWithHintAndFee(
                    fromAddress,
                    toAddress,
                    amount,
                    minConversionRate,
                    walletAddress
                )
            } else {
                tradeWithHint(
                    fromAddress,
                    toAddress,
                    amount,
                    minConversionRate,
                    walletAddress
                )
            }

        return web3jAlchemyNode.ethEstimateGas(
            Transaction(
                walletAddress,
                null,
                null,
                null,
                contractAddress,
                if (isEth) amount else BigInteger.ZERO,
                FunctionEncoder.encode(function)
            )
        ).send()
    }

    private fun transfer(
        walletAddress: String,
        value: String
    ): Function {
        return Function(
            "transfer",
            listOf(
                Address(walletAddress),
                Uint256(BigInteger(value))
            ),
            emptyList()
        )
    }

    @Throws(java.lang.Exception::class)
    fun estimateGasForTransfer(
        walletAddress: String,
        tokenAddress: String,
        contactAddress: String,
        value: String,
        isEth: Boolean
    ): EthEstimateGas? {

        return web3jAlchemyNode.ethEstimateGas(
            Transaction(
                walletAddress,
                null,
                null,
                null,
                if (isEth) contactAddress else tokenAddress,
                if (isEth) value.toBigIntegerOrDefaultZero() else BigInteger.ZERO,
                FunctionEncoder.encode(
                    transfer(
                        if (contactAddress.isNotEmpty()) contactAddress else walletAddress,
                        value
                    )
                )
            )
        ).send()
    }

    @Throws(IOException::class)
    private fun tradeWithHint(
        fromAddress: String,
        toAddress: String,
        value: BigInteger,
        minConversionRate: BigInteger,
        walletAddress: String
    ): Function {

        return Function(
            "tradeWithHint",
            listOf(
                Address(fromAddress),
                Uint256(value),
                Address(toAddress),
                Address(walletAddress),
                Uint256(DEFAULT_MAX_AMOUNT),
                Uint256(minConversionRate),
                Address(DEFAULT_WALLET_ID),
                DynamicBytes(PERM.toByteArray())
            ),
            listOf<TypeReference<*>>()
        )
    }

    @Throws(IOException::class)
    private fun tradeWithHintAndFee(
        fromAddress: String,
        toAddress: String,
        value: BigInteger,
        minConversionRate: BigInteger,
        walletAddress: String,
        platformFeeBps: BigInteger = PLATFORM_FEE_BPS.toBigInteger(),
        hint: String = ""
    ): Function {

        return Function(
            "tradeWithHintAndFee",
            listOf(
                Address(fromAddress),
                Uint256(value),
                Address(toAddress),
                Address(walletAddress),
                Uint256(DEFAULT_MAX_AMOUNT),
                Uint256(minConversionRate),
                Address(DEFAULT_WALLET_ID),
                Uint256(platformFeeBps),
                DynamicBytes(hint.toByteArray())
            ),
            listOf<TypeReference<*>>()
        )
    }

    @Throws(IOException::class)
    fun doSwap(
        param: SwapTokenUseCase.Param,
        credentials: Credentials,
        contractAddress: String
    ): Pair<String?, BigInteger> {
        val gasPrice = Convert.toWei(
            param.swap.gasPrice.toBigDecimalOrDefaultZero(),
            Convert.Unit.GWEI
        ).toBigInteger()
        val gasLimit =
            if (param.swap.gasLimit.toBigInteger() == BigInteger.ZERO) calculateDefaultGasLimit(
                param.swap.tokenSource,
                param.swap.tokenDest
            )
            else param.swap.gasLimit.toBigInteger()

        val tradeWithHintAmount = if (param.swap.isSwapAll) {
            param.swap.tokenSource.currentBalance
        } else {
            param.swap.sourceAmount.toBigDecimalOrDefaultZero()
        }.times(10.0.pow(param.swap.tokenSource.tokenDecimal).toBigDecimal()).toBigInteger()

        val transactionAmount =
            if (param.swap.tokenSource.isETH) tradeWithHintAmount else BigInteger.ZERO

        val minConversionRate = param.swap.minConversionRate

        val fromAddress = param.swap.tokenSource.tokenAddress

        val toAddress = param.swap.tokenDest.tokenAddress

        val walletAddress = if (param.wallet.isPromoPayment) param.wallet.promo?.receiveAddress
            ?: param.wallet.address else
            param.wallet.address


        return if (param.swap.tokenSource.isETH) {
            executeTradeWithHint(
                fromAddress,
                toAddress,
                transactionAmount,
                tradeWithHintAmount,
                minConversionRate,
                gasPrice,
                gasLimit,
                contractAddress,
                walletAddress,
                credentials
            )
        } else {
            handleSwapERC20Token(
                param.swap.tokenSource,
                transactionAmount,
                tradeWithHintAmount,
                minConversionRate,
                gasPrice,
                gasLimit,
                param.swap.tokenDest,
                walletAddress,
                contractAddress,
                credentials
            )
        }
    }

    @Throws(IOException::class)
    fun sendTransaction(
        wcTransaction: WCEthereumTransaction,
        credentials: Credentials
    ): Pair<String?, BigInteger> {

//        val txManager = RawTransactionManager(web3jAlchemyNode, credentials)

        val txManagerWithAlchemyNode = RawTransactionManager(web3jAlchemyNode, credentials)
        val txManagerWithInfuraNode = RawTransactionManager(web3jInfuraNode, credentials)
        val txManagerWithSemiNode = RawTransactionManager(web3jSemiNode, credentials)

        val walletAddress = credentials.address

        val localNonce = getTransactionNonce(walletAddress)

        val tx = RawTransaction.createTransaction(
            localNonce,
            wcTransaction.gasPrice.toBigIntSafe(),
            wcTransaction.gasLimit.toBigIntSafe(),
            wcTransaction.to,
            wcTransaction.value.toBigIntSafe(),
            wcTransaction.data
        )

//        val transactionResponse = txManager.signAndSend(
//            RawTransaction.createTransaction(
//                localNonce,
//                wcTransaction.gasPrice.toBigIntSafe(),
//                wcTransaction.gasLimit.toBigIntSafe(),
//                wcTransaction.to,
//                wcTransaction.value.toBigIntSafe(),
//                wcTransaction.data
//            )
//        )

        val transactionResponseAlchemyNode = txManagerWithAlchemyNode.signAndSend(tx)
        val transactionResponseInfuraNode = txManagerWithInfuraNode.signAndSend(tx)
        val transactionResponseSemiNode = txManagerWithSemiNode.signAndSend(tx)

        if (transactionResponseAlchemyNode?.hasError() == true &&
            transactionResponseInfuraNode?.hasError() == true &&
            transactionResponseSemiNode?.hasError() == true
        ) run {
            analytics.logEvent(
                ALCHEMY_BROADCAST_NODE_ERROR,
                Bundle().createEvent(
                    (transactionResponseAlchemyNode.error?.code?.toString() + "|" + transactionResponseAlchemyNode.error?.message).take(
                        99
                    )
                )
            )

            if (transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_1),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_2),
                    true
                ) == true
            ) {
                throw RuntimeException(
                    context.getString(R.string.error_underpriced_wallet_connect)
                )
            } else {
                throw RuntimeException(
                    "Error processing transaction request: " +
                        transactionResponseAlchemyNode.error?.message
                )
            }
        }

        val hash = transactionResponseAlchemyNode?.transactionHash
            ?: transactionResponseInfuraNode?.transactionHash
            ?: transactionResponseSemiNode?.transactionHash
        saveLocalNonce(walletAddress, localNonce, hash)

        return Pair(hash, localNonce)
    }

    @Throws(IOException::class)
    fun doTransferTransaction(
        param: TransferTokenUseCase.Param, credentials: Credentials
    ): Pair<String?, BigInteger> {
        val gasPrice = Convert.toWei(
            param.send.gasPrice.toBigDecimalOrDefaultZero(),
            Convert.Unit.GWEI
        ).toBigInteger()

        val gasLimit =

            if (param.send.gasLimit.toBigIntegerOrDefaultZero() > BigInteger.ZERO) {
                param.send.gasLimit.toBigIntegerOrDefaultZero()
            } else {
                calculateDefaultGasLimitTransfer(param.send.tokenSource)
            }

        val isEth = param.send.tokenSource.isETH

        val amount = if (param.send.isSendAll) {
            param.send.tokenSource.currentBalance
        } else {
            param.send.sourceAmount.toBigDecimalOrDefaultZero()
        }.multiply(
            10.toBigDecimal().pow(param.send.tokenSource.tokenDecimal)
        ).toBigInteger()

        val transactionAmount = if (isEth) amount else BigInteger.ZERO
        val txManagerWithAlchemyNode = RawTransactionManager(web3jAlchemyNode, credentials)
        val txManagerWithInfuraNode = RawTransactionManager(web3jInfuraNode, credentials)
        val txManagerWithSemiNode = RawTransactionManager(web3jSemiNode, credentials)

        val walletAddress = credentials.address
        val localNonce = getTransactionNonce(walletAddress)
        val tx = RawTransaction.createTransaction(
            localNonce,
            gasPrice,
            gasLimit,
            if (isEth) param.send.contact.address else param.send.tokenSource.tokenAddress,
            transactionAmount,
            if (isEth) "" else
                FunctionEncoder.encode(
                    transfer(
                        param.send.contact.address,
                        amount.toString()
                    )
                )
        )

        val transactionResponseAlchemyNode = txManagerWithAlchemyNode.signAndSend(tx)
        val transactionResponseInfuraNode = txManagerWithInfuraNode.signAndSend(tx)
        val transactionResponseSemiNode = txManagerWithSemiNode.signAndSend(tx)

        if (transactionResponseAlchemyNode?.hasError() == true &&
            transactionResponseInfuraNode?.hasError() == true &&
            transactionResponseSemiNode?.hasError() == true
        ) run {
            analytics.logEvent(
                ALCHEMY_BROADCAST_NODE_ERROR,
                Bundle().createEvent(
                    (transactionResponseAlchemyNode.error?.code?.toString() + "|" + transactionResponseAlchemyNode.error?.message).take(
                        99
                    )
                )
            )

            if (transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_1),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_2),
                    true
                ) == true
            ) {
                throw RuntimeException(
                    context.getString(R.string.error_underpriced_transfer)
                )
            } else {
                throw RuntimeException(
                    "Error processing transaction request: " +
                        transactionResponseAlchemyNode.error?.message
                )
            }
        }

        val hash = transactionResponseAlchemyNode?.transactionHash
            ?: transactionResponseInfuraNode?.transactionHash
            ?: transactionResponseSemiNode?.transactionHash
        saveLocalNonce(walletAddress, localNonce, hash)

        return Pair(hash, localNonce)
    }

    @Synchronized
    private fun getTransactionNonce(address: String): BigInteger {
        val minedNonce = getMinedNonce(address)
        if (minedNonce == getPendingNonce(address)) return minedNonce
        return minedNonce.max(getLocalNonce(address))
    }

    private fun executeTradeWithHint(
        fromAddress: String,
        toAddress: String,
        transactionAmount: BigInteger,
        tradeWithHintAmount: BigInteger,
        minConversionRate: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        contractAddress: String,
        walletAddress: String,
        credentials: Credentials

    ): Pair<String?, BigInteger> {
        val localNonce = getTransactionNonce(credentials.address)
        val txManagerWithAlchemyNode = RawTransactionManager(web3jAlchemyNode, credentials)
        val txManagerWithInfuraNode = RawTransactionManager(web3jInfuraNode, credentials)
        val txManagerWithSemiNode = RawTransactionManager(web3jSemiNode, credentials)

        val tx = RawTransaction.createTransaction(
            localNonce,
            gasPrice,
            gasLimit,
            contractAddress,
            transactionAmount,
            FunctionEncoder.encode(
                if (BuildConfig.FLAVOR == "dev") {
                    tradeWithHintAndFee(
                        fromAddress,
                        toAddress,
                        tradeWithHintAmount,
                        minConversionRate,
                        walletAddress
                    )
                } else {
                    tradeWithHint(
                        fromAddress,
                        toAddress,
                        tradeWithHintAmount,
                        minConversionRate,
                        walletAddress
                    )
                }
            )
        )

        val transactionResponseAlchemyNode = txManagerWithAlchemyNode.signAndSend(tx)
        val transactionResponseInfuraNode = txManagerWithInfuraNode.signAndSend(tx)
        val transactionResponseSemiNode = txManagerWithSemiNode.signAndSend(tx)


        if (transactionResponseAlchemyNode?.hasError() == true &&
            transactionResponseInfuraNode?.hasError() == true &&
            transactionResponseSemiNode?.hasError() == true
        ) run {
            analytics.logEvent(
                ALCHEMY_BROADCAST_NODE_ERROR,
                Bundle().createEvent(
                    (transactionResponseAlchemyNode.error?.code?.toString() + "|" + transactionResponseAlchemyNode.error?.message).take(
                        99
                    )
                )
            )
            if (transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_1),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_2),
                    true
                ) == true
            ) {
                throw RuntimeException(
                    context.getString(R.string.replacement_underpriced)
                )
            } else {
                throw RuntimeException(
                    "Error processing transaction request: " +
                        transactionResponseAlchemyNode.error?.message
                )
            }
        }

        val hash = transactionResponseAlchemyNode?.transactionHash
            ?: transactionResponseInfuraNode?.transactionHash
            ?: transactionResponseSemiNode?.transactionHash

        // Insert into local nonce
        saveLocalNonce(walletAddress, localNonce, hash)

        return Pair(hash, localNonce)
    }

    @Synchronized
    private fun saveLocalNonce(walletAddress: String, localNonce: BigInteger, hash: String?) {
        // Insert into local nonce
        nonceDao.insertNonce(Nonce(walletAddress = walletAddress, nonce = localNonce, hash = hash))
    }

    @Synchronized
    @Throws(IOException::class)
    private fun getMinedNonce(walletAddress: String): BigInteger {
        val ethGetTransactionCount = web3jAlchemyNode.ethGetTransactionCount(
            walletAddress, DefaultBlockParameterName.LATEST
        ).send()
        if (ethGetTransactionCount?.hasError() == true) {
            analytics.logEvent(
                NODE_GET_NONCE_ERROR,
                Bundle().createEvent(
                    ("mined|" + ethGetTransactionCount.error?.code?.toString() + "|" + ethGetTransactionCount.error?.message).take(
                        99
                    )
                )
            )
            throw RuntimeException("Error processing transaction request:" + ethGetTransactionCount.error?.message)
        }
        return ethGetTransactionCount?.transactionCount ?: BigInteger.ZERO
    }

    @Synchronized
    @Throws(IOException::class)
    private fun getPendingNonce(walletAddress: String): BigInteger {
        val ethGetTransactionCount = web3jAlchemyNode.ethGetTransactionCount(
            walletAddress, DefaultBlockParameterName.PENDING
        ).send()

        if (ethGetTransactionCount?.hasError() == true) {
            analytics.logEvent(
                NODE_GET_NONCE_ERROR,
                Bundle().createEvent(
                    ("pending|" + ethGetTransactionCount.error?.code?.toString() + "|" + ethGetTransactionCount.error?.message).take(
                        99
                    )
                )
            )
            throw RuntimeException("Error processing transaction request:" + ethGetTransactionCount.error?.message)
        }
        return ethGetTransactionCount.transactionCount ?: BigInteger.ZERO
    }

    @Synchronized
    @Throws(IOException::class)
    private fun getLocalNonce(walletAddress: String): BigInteger {
        val currentNonce = nonceDao.findNonce(walletAddress)?.nonce ?: BigInteger.ZERO
        return currentNonce.plus(BigInteger.ONE)
    }

    private fun handleSwapERC20Token(
        fromToken: Token,
        transactionAmount: BigInteger,
        tradeWithHintAmount: BigInteger,
        minConversionRate: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        toToken: Token,
        walletAddress: String,
        contractAddress: String,
        credentials: Credentials
    ): Pair<String?, BigInteger> {
        val allowanceAmount =
            getContractAllowanceAmount(
                walletAddress,
                fromToken.tokenAddress,
                contractAddress,
                credentials
            )
        if (allowanceAmount < tradeWithHintAmount) {
            sendContractApprovalWithCondition(
                allowanceAmount,
                fromToken,
                contractAddress,
                gasPrice,
                credentials,
                walletAddress
            )
        }
        return executeTradeWithHint(
            fromToken.tokenAddress,
            toToken.tokenAddress,
            transactionAmount,
            tradeWithHintAmount,
            minConversionRate,
            gasPrice,
            gasLimit,
            contractAddress,
            walletAddress,
            credentials
        )
    }

    private fun allowance(walletAddress: String, contractAddress: String): Function {
        return Function(
            "allowance",
            listOf(
                Address(walletAddress),
                Address(contractAddress)
            ),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {
            })
        )
    }

    private fun getContractAllowanceAmount(
        walletAddress: String,
        tokenAddress: String,
        contractAddress: String, // Token address
        credentials: Credentials
    ): BigInteger {
        val txManager = RawTransactionManager(web3jAlchemyNode, credentials)
        val function = allowance(walletAddress, contractAddress)

        val ethCall = web3jAlchemyNode.ethCall(
            Transaction.createEthCallTransaction(
                txManager.fromAddress, tokenAddress, FunctionEncoder.encode(function)
            ),
            DefaultBlockParameterName.LATEST
        ).send()

        val enableResult = ethCall.value

        val values = FunctionReturnDecoder.decode(enableResult, function.outputParameters)
        val allowAmount = (values[0] as Uint256)
        return allowAmount.value
    }

    private fun approve(contractAddress: String, amount: BigInteger): Function {

        return Function(
            "approve",
            listOf(
                Address(contractAddress),
                Uint256(amount)
            ),
            emptyList()
        )
    }

    private fun sendContractApprovalWithCondition(
        allowanceAmount: BigInteger,
        token: Token,
        contractAddress: String,
        gasPriceWei: BigInteger,
        credentials: Credentials,
        walletAddress: String
    ) {

        if (allowanceAmount > BigInteger.ZERO) {
            sendContractApproval(
                BigInteger.ZERO,
                token,
                contractAddress,
                gasPriceWei,
                if (token.gasApprove > BigDecimal.ZERO) token.gasApprove.toBigInteger() else Token.APPROVE_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger(),
                credentials,
                walletAddress
            )
        }
        sendContractApproval(
            DEFAULT_MAX_AMOUNT,
            token,
            contractAddress,
            gasPriceWei,
            if (token.gasApprove > BigDecimal.ZERO) token.gasApprove.toBigInteger() else Token.APPROVE_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger(),
            credentials,
            walletAddress
        )
    }

    private fun sendContractApproval(
        allowanceAmount: BigInteger,
        token: Token,
        contractAddress: String,
        gasPriceWei: BigInteger,
        gasLimit: BigInteger,
        credentials: Credentials,
        walletAddress: String
    ) {
        val function = approve(contractAddress, allowanceAmount)
        val encodedFunction = FunctionEncoder.encode(function)

//        val transactionResponse = transactionManager.sendTransaction(
//            gasPriceWei,
//            gasLimit,
//            token.tokenAddress,
//            encodedFunction,
//            BigInteger.ZERO
//        )

        val localNonce = getTransactionNonce(credentials.address)
//        val txManager = RawTransactionManager(web3jAlchemyNode, credentials)

        val txManagerWithAlchemyNode = RawTransactionManager(web3jAlchemyNode, credentials)
        val txManagerWithInfuraNode = RawTransactionManager(web3jInfuraNode, credentials)
        val txManagerWithSemiNode = RawTransactionManager(web3jSemiNode, credentials)

        val tx = RawTransaction.createTransaction(
            localNonce,
            gasPriceWei,
            gasLimit,
            token.tokenAddress,
            BigInteger.ZERO,
            encodedFunction
        )

        val transactionResponseAlchemyNode = txManagerWithAlchemyNode.signAndSend(tx)
        val transactionResponseInfuraNode = txManagerWithInfuraNode.signAndSend(tx)
        val transactionResponseSemiNode = txManagerWithSemiNode.signAndSend(tx)

//        val transactionResponse = txManager.signAndSend(
//            RawTransaction.createTransaction(
//                localNonce,
//                gasPriceWei,
//                gasLimit,
//                token.tokenAddress,
//                BigInteger.ZERO,
//                encodedFunction
//            )
//        )

        if (transactionResponseAlchemyNode?.hasError() == true &&
            transactionResponseInfuraNode?.hasError() == true &&
            transactionResponseSemiNode?.hasError() == true
        ) run {
            analytics.logEvent(
                ALCHEMY_BROADCAST_NODE_ERROR,
                Bundle().createEvent(
                    (transactionResponseAlchemyNode.error?.code?.toString() + "|" + transactionResponseAlchemyNode.error?.message).take(
                        99
                    )
                )
            )
            if (transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_1),
                    true
                ) == true ||
                transactionResponseAlchemyNode.error?.message?.contains(
                    context.getString(R.string.under_price_error_message_2),
                    true
                ) == true
            ) {
                throw RuntimeException(
                    context.getString(R.string.erorr_underpriced_transaction)
                )
            } else {
                throw RuntimeException(
                    "Error processing transaction request: " +
                        transactionResponseAlchemyNode.error?.message
                )
            }
        }
        val hash = transactionResponseAlchemyNode?.transactionHash
            ?: transactionResponseInfuraNode?.transactionHash
            ?: transactionResponseSemiNode?.transactionHash

        saveLocalNonce(walletAddress, localNonce, hash)
    }

    @Throws(Exception::class)
    private fun cancel(
        wallet: Wallet,
        tx: org.web3j.protocol.core.methods.response.Transaction,
        gasPrice: BigInteger
    ): String {
        if (context is KyberSwapApplication) {
            val password = String(
                context.aead.decrypt(
                    Base64.decode(wallet.cipher, Base64.DEFAULT), ByteArray(0)
                ), Charsets.UTF_8
            )
            val credentials = WalletUtils.loadCredentials(
                password,
                wallet.walletPath
            )

            val rawTransaction =
                RawTransaction.createTransaction(
                    tx.nonce,
                    gasPrice,
                    tx.gas,
                    wallet.walletAddress,
                    BigInteger.ZERO,
                    ""
                )

            val txManagerWithAlchemyNode = RawTransactionManager(web3jAlchemyNode, credentials)
            val txManagerWithInfuraNode = RawTransactionManager(web3jInfuraNode, credentials)
            val txManagerWithSemiNode = RawTransactionManager(web3jSemiNode, credentials)

            val transactionResponseAlchemyNode =
                txManagerWithAlchemyNode.signAndSend(rawTransaction)
            val transactionResponseInfuraNode = txManagerWithInfuraNode.signAndSend(rawTransaction)
            val transactionResponseSemiNode = txManagerWithSemiNode.signAndSend(rawTransaction)

            if (transactionResponseAlchemyNode?.hasError() == true &&
                transactionResponseInfuraNode?.hasError() == true &&
                transactionResponseSemiNode?.hasError() == true
            ) run {
                analytics.logEvent(
                    ALCHEMY_BROADCAST_NODE_ERROR,
                    Bundle().createEvent(
                        (transactionResponseAlchemyNode.error?.code?.toString() + "|" + transactionResponseAlchemyNode.error?.message).take(
                            99
                        )
                    )
                )
                throw RuntimeException(
                    "Error processing transaction request: " +
                        transactionResponseAlchemyNode.error?.message
                )
            }
            return transactionResponseAlchemyNode?.transactionHash
                ?: transactionResponseInfuraNode?.transactionHash
                ?: transactionResponseSemiNode?.transactionHash ?: ""
        }

        return ""
    }

    @Throws(Exception::class)
    private fun speedUp(
        wallet: Wallet,
        tx: org.web3j.protocol.core.methods.response.Transaction,
        gasPrice: BigInteger
    ): String {
        if (context is KyberSwapApplication) {
            val password = String(
                context.aead.decrypt(
                    Base64.decode(wallet.cipher, Base64.DEFAULT), ByteArray(0)
                ), Charsets.UTF_8
            )
            val credentials = WalletUtils.loadCredentials(
                password,
                wallet.walletPath
            )

            if (tx.isFromKyberSwap()) {
                val params = tx.params()

                val txManagerWithAlchemyNode = RawTransactionManager(web3jAlchemyNode, credentials)
                val txManagerWithInfuraNode = RawTransactionManager(web3jInfuraNode, credentials)
                val txManagerWithSemiNode = RawTransactionManager(web3jSemiNode, credentials)
                if (tx.isSwapTx()) {

                    val input = FunctionEncoder.encode(
                        if (BuildConfig.FLAVOR == "dev") {
                            tradeWithHintAndFee(
                                tx.fromAddress(params),
                                tx.toAddress(params),
                                tx.txValue(params),
                                tx.minConversionRate(params),
                                wallet.walletAddress
                            )
                        } else {
                            tradeWithHint(
                                tx.fromAddress(params),
                                tx.toAddress(params),
                                tx.txValue(params),
                                tx.minConversionRate(params),
                                wallet.walletAddress
                            )
                        }

                    )

                    val rawTransaction =
                        RawTransaction.createTransaction(
                            tx.nonce,
                            gasPrice,
                            tx.gas,
                            tx.to,
                            tx.value,
                            input
                        )

                    val transactionResponseAlchemyNode =
                        txManagerWithAlchemyNode.signAndSend(rawTransaction)
                    val transactionResponseInfuraNode =
                        txManagerWithInfuraNode.signAndSend(rawTransaction)
                    val transactionResponseSemiNode =
                        txManagerWithSemiNode.signAndSend(rawTransaction)

                    if (transactionResponseAlchemyNode?.hasError() == true &&
                        transactionResponseInfuraNode?.hasError() == true &&
                        transactionResponseSemiNode?.hasError() == true
                    ) run {
                        analytics.logEvent(
                            ALCHEMY_BROADCAST_NODE_ERROR,
                            Bundle().createEvent(
                                (transactionResponseAlchemyNode.error?.code?.toString() + "|" + transactionResponseAlchemyNode.error?.message).take(
                                    99
                                )
                            )
                        )

                        if (transactionResponseAlchemyNode.error?.message?.contains(
                                context.getString(R.string.under_price_error_message),
                                true
                            ) == true ||
                            transactionResponseAlchemyNode.error?.message?.contains(
                                context.getString(R.string.under_price_error_message_1),
                                true
                            ) == true ||
                            transactionResponseAlchemyNode.error?.message?.contains(
                                context.getString(R.string.under_price_error_message_2),
                                true
                            ) == true
                        ) {
                            throw RuntimeException(
                                context.getString(R.string.replacement_underpriced)
                            )
                        } else {
                            throw RuntimeException(
                                "Error processing transaction request: " +
                                    transactionResponseAlchemyNode.error?.message
                            )
                        }
                    }

                    return transactionResponseAlchemyNode?.transactionHash
                        ?: transactionResponseInfuraNode?.transactionHash
                        ?: transactionResponseSemiNode?.transactionHash ?: ""
                } else {

                    val to = if (tx.isTransferETHTx()) {
                        tx.to
                    } else {
                        tx.transferToAddress(params)
                    }

                    val input = if (tx.isTransferETHTx()) "" else
                        FunctionEncoder.encode(
                            transfer(
                                to,
                                tx.transferAmount(params).toString()
                            )
                        )

                    val rawTransaction =
                        RawTransaction.createTransaction(
                            tx.nonce,
                            gasPrice,
                            tx.gas,
                            tx.to,
                            tx.value,
                            input
                        )

                    val transactionResponseAlchemyNode =
                        txManagerWithAlchemyNode.signAndSend(rawTransaction)
                    val transactionResponseInfuraNode =
                        txManagerWithInfuraNode.signAndSend(rawTransaction)
                    val transactionResponseSemiNode =
                        txManagerWithSemiNode.signAndSend(rawTransaction)


                    if (transactionResponseAlchemyNode?.hasError() == true &&
                        transactionResponseInfuraNode?.hasError() == true &&
                        transactionResponseSemiNode?.hasError() == true
                    ) run {
                        analytics.logEvent(
                            ALCHEMY_BROADCAST_NODE_ERROR,
                            Bundle().createEvent(
                                (transactionResponseAlchemyNode.error?.code?.toString() + "|" + transactionResponseAlchemyNode.error?.message).take(
                                    99
                                )
                            )
                        )

                        if (transactionResponseAlchemyNode.error?.message?.contains(
                                context.getString(R.string.under_price_error_message),
                                true
                            ) == true ||
                            transactionResponseAlchemyNode.error?.message?.contains(
                                context.getString(R.string.under_price_error_message_1),
                                true
                            ) == true ||
                            transactionResponseAlchemyNode.error?.message?.contains(
                                context.getString(R.string.under_price_error_message_2),
                                true
                            ) == true
                        ) {
                            throw RuntimeException(
                                context.getString(R.string.error_underpriced_transfer)
                            )
                        } else {
                            throw RuntimeException(
                                "Error processing transaction request: " +
                                    transactionResponseAlchemyNode.error?.message
                            )
                        }
                    }

                    return transactionResponseAlchemyNode?.transactionHash
                        ?: transactionResponseInfuraNode?.transactionHash
                        ?: transactionResponseSemiNode?.transactionHash ?: ""
                }
            }
        }

        return ""
    }


    fun monitorPendingTransactions(
        transactions: List<com.kyberswap.android.domain.model.Transaction>,
        wallet: Wallet
    ): List<com.kyberswap.android.domain.model.Transaction> {
        val transactionsList = mutableListOf<com.kyberswap.android.domain.model.Transaction>()
        for (pending in transactions) {
            try {
                val transaction =
                    web3jAlchemyNode.ethGetTransactionByHash(pending.hash).send().transaction
                if (transaction.isPresent) {
                    val tx = transaction.get()
                    if (tx.hash.isNotEmpty()) {
                        val transactionReceipt =
                            web3jAlchemyNode.ethGetTransactionReceipt(tx.hash)
                                .send().transactionReceipt
                        if (transactionReceipt.isPresent) {
                            val txReceipt = transactionReceipt.get()

                            val filter = txReceipt.logs.firstOrNull {
                                it.address.equals(
                                    context.getString(R.string.kyber_address),
                                    true
                                ) &&
                                    it.topics.isNotEmpty() && it.topics.first().equals(
                                    context.getString(R.string.kyber_event_topic),
                                    true
                                )
                            }

                            val txDetail = if (filter != null) {

                                val values = FunctionReturnDecoder.decode(
                                    filter.data,
                                    if (BuildConfig.FLAVOR == "dev") {
                                        executeTradeEvent.nonIndexedParameters
                                    } else {
                                        event.nonIndexedParameters
                                    }
                                )
                                val tokenBySymbol =
                                    tokenDao.getTokenBySymbol(pending.tokenDest)
                                val destAmount = if (BuildConfig.FLAVOR == "dev") {
                                    if (values.size > 4) {
                                        if (tokenBySymbol != null) {
                                            (values[4] as Uint256).value.toBigDecimal().divide(
                                                BigDecimal.TEN
                                                    .pow(tokenBySymbol.tokenDecimal),
                                                18,
                                                RoundingMode.UP
                                            ).toDisplayNumber()
                                        } else {
                                            pending.destAmount
                                        }
                                    } else {
                                        pending.destAmount
                                    }
                                } else {
                                    if (values.size > 3) {
                                        if (tokenBySymbol != null) {
                                            (values[3] as Uint256).value.toBigDecimal().divide(
                                                BigDecimal.TEN
                                                    .pow(tokenBySymbol.tokenDecimal),
                                                18,
                                                RoundingMode.UP
                                            ).toDisplayNumber()
                                        } else {
                                            pending.destAmount
                                        }
                                    } else {
                                        pending.destAmount
                                    }
                                }


                                pending.copy(destAmount = destAmount)
                            } else {
                                pending
                            }
                            transactionsList.add(txDetail.with(txReceipt))
                        } else {
                            transactionsList.add(pending.with(tx))
                        }
                    } else {
                        transactionsList.add(
                            com.kyberswap.android.domain.model.Transaction(tx).copy(
                                hash = pending.hash
                            )
                        )
                    }
                } else {
                    if ((System.currentTimeMillis() / 1000 - pending.timeStamp) / 60f > 10f) {
                        transactionsList.add(pending.copy(blockNumber = com.kyberswap.android.domain.model.Transaction.DEFAULT_DROPPED_BLOCK_NUMBER.toString()))
                    } else {
                        val latestTx = transactionDao.getLatestTransaction(wallet.address)
                        if (pending.nonce.toBigDecimalOrDefaultZero() > BigDecimal.ZERO &&
                            latestTx?.nonce.toBigDecimalOrDefaultZero() >= pending.nonce.toBigDecimalOrDefaultZero() &&
                            !pending.hash.equals(latestTx?.nonce, true) &&
                            System.currentTimeMillis() / 1000 - pending.timeStamp > 30
                        ) {
                            analytics.logEvent(
                                TX_DROPPED_EVENT,
                                Bundle().createEvent(pending.displayTransaction)
                            )
                            if (!pending.isCancel) {
                                showDroppedNotification(pending)
                            }
                            transactionDao.delete(pending)
                        } else {
                            transactionsList.add(pending)
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (transactionsList.isEmpty()) return transactions
        return transactionsList.toList()
    }

    private fun showDroppedNotification(
        transaction: com.kyberswap.android.domain.model.Transaction
    ) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse(context.getString(R.string.transaction_etherscan_endpoint_url) + transaction.hash)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val title: String = context.getString(R.string.notification_dropped)
        val message: String = String.format(
            context.getString(R.string.notification_dropped_message),
            transaction.hash.shortenValue()
        )

        val channelId = context.getString(R.string.default_notification_channel_id)
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
            .setColor(ContextCompat.getColor(context, R.color.notification_background))
            .setContentTitle(title)
            .setContentText(
                message
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            Numeric.toBigInt(transaction.hash).toInt(),
            notificationBuilder.build()
        )
    }

    @Throws(Exception::class)
    fun speedUpOrCancelTx(
        wallet: Wallet,
        tx: com.kyberswap.android.domain.model.Transaction,
        isCancel: Boolean
    ): com.kyberswap.android.domain.model.Transaction {
        val optionalResponse = web3jAlchemyNode.ethGetTransactionByHash(tx.hash).send().transaction
        val txResponse = optionalResponse.get()
        val newHash = if (isCancel) cancel(
            wallet,
            txResponse,
            tx.gasPrice.toBigIntegerOrDefaultZero()
        ) else speedUp(wallet, txResponse, tx.gasPrice.toBigIntegerOrDefaultZero())
        val newTx =
            web3jAlchemyNode.ethGetTransactionByHash(newHash).send().transaction.get()


        return if (newHash.isNotEmpty() && !web3jAlchemyNode.ethGetTransactionReceipt(tx.hash)
                .send().transactionReceipt.isPresent
        ) {
            val nonce = if (newTx.nonce != null) newTx.nonce else txResponse.nonce
            transactionDao.updateTransaction(
                tx.copy(
                    isCancel = true,
                    nonce = nonce.toString()
                )
            )

            val t = tx.with(newTx)
            val pendingTx = if (isCancel) {
                com.kyberswap.android.domain.model.Transaction(
                    hash = t.hash,
                    transactionStatus = com.kyberswap.android.domain.model.Transaction.PENDING_TRANSACTION_STATUS,
                    timeStamp = System.currentTimeMillis() / 1000L,
                    from = wallet.address,
                    gas = t.gas,
                    gasUsed = t.gasUsed,
                    gasPrice = t.gasPrice,
                    to = wallet.address,
                    value = 0.toString(),
                    tokenDecimal = Token.ETH_DECIMAL.toString(),
                    tokenSymbol = Token.ETH,
                    walletAddress = wallet.address,
                    nonce = nonce.toString(),
                    type = com.kyberswap.android.domain.model.Transaction.TransactionType.SEND
                )
            } else {
                t.copy(value = tx.value)
            }
            transactionDao.insertTransaction(pendingTx)
            pendingTx
        } else {
            tx.with(txResponse)
        }
    }


    fun signOrder(
        order: LocalLimitOrder,
        credentials: Credentials,
        contractAddress: String
    ): String {
        val allowanceAmount =
            getContractAllowanceAmount(
                order.userAddr,
                order.tokenSource.tokenAddress,
                contractAddress,
                credentials
            )
        if (allowanceAmount < order.sourceAmountWithPrecision) {
            sendContractApprovalWithCondition(
                allowanceAmount,
                order.tokenSource,
                contractAddress,
                Convert.toWei(
                    order.gasPrice.toBigDecimalOrDefaultZero(),
                    Convert.Unit.GWEI
                ).toBigInteger(),
                credentials,
                credentials.address
            )
        }

        val signValue = StringBuilder()
            .append(order.userAddr.removePrefix("0x"))
            .append(order.nonce.removePrefix("0x"))
            .append(order.tokenSource.tokenAddress.removePrefix("0x"))
            .append(String.format("%064x", order.sourceAmountWithPrecision))
            .append(order.tokenDest.tokenAddress.removePrefix("0x"))
            .append(order.userAddr.removePrefix("0x"))
            .append(String.format("%064x", order.minRateWithPrecision))
            .append(String.format("%064x", order.feeAmountWithPrecision))
            .toString()
        val hash = Hash.sha3(signValue)
        val data = Numeric.hexStringToByteArray(hash)
        val sign: Sign.SignatureData = Sign.signPrefixedMessage(data, credentials.ecKeyPair)
        return Numeric.toHexString(sign.r.plus(sign.s).plus(sign.v))
    }

    fun signMessage(credentials: Credentials, message: WCEthereumSignMessage): String {
        val sign: Sign.SignatureData = Sign.signPrefixedMessage(
            Numeric.hexStringToByteArray(message.raw.first()),
            credentials.ecKeyPair
        )
        return Numeric.toHexString(sign.r.plus(sign.s).plus(sign.v))
    }

    fun resolve(name: String): String {
        return EnsResolver(web3jAlchemyNode).resolve(name)
    }

    fun revertResolve(address: String): String {
        return EnsResolver(web3jAlchemyNode).reverseResolve(address)
    }
}
