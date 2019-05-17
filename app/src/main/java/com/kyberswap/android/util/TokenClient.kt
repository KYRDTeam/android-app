package com.kyberswap.android.util

import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.usecase.wallet.SwapTokenUseCase
import com.kyberswap.android.presentation.common.DEFAULT_MAX_AMOUNT
import com.kyberswap.android.presentation.common.DEFAULT_WALLET_ID
import com.kyberswap.android.presentation.common.PERM
import com.kyberswap.android.util.ext.toBytes32
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.utils.Convert
import timber.log.Timber
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import javax.inject.Inject


class TokenClient @Inject constructor(private val web3j: Web3j) {

    private fun balanceOf(owner: String): Function {
        return Function(
            "balanceOf",
            listOf(Address(owner)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {
            })
        )
    }

    private fun getExpectedRate(
        srcToken: String,
        destToken: String,
        srcTokenAmount: BigInteger
    ): Function {
        return Function(
            "getExpectedRate",
            listOf(
                Address(srcToken),
                Address(destToken),
                Uint256(srcTokenAmount)
            ),
            listOf<TypeReference<*>>(
                object : TypeReference<Uint256>() {
                },
                object : TypeReference<Uint256>() {

                })
        )
    }

    @Throws(Exception::class)
    fun getEthBalance(owner: String): BigInteger {
        return web3j
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
        fromAddress: String
    ): String {
        val encodedFunction = FunctionEncoder.encode(function)
        val response = web3j.ethCall(
            Transaction.createEthCallTransaction(fromAddress, contractAddress, encodedFunction),
            DefaultBlockParameterName.LATEST
        )
            .send()

        return response.value
    }

    @Throws(Exception::class)
    fun getBalance(walletAddress: String, tokenAddress: String): BigDecimal? {
        val function = balanceOf(walletAddress)
        val responseValue = callSmartContractFunction(function, tokenAddress, walletAddress)

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
    fun getBalance(owner: String, token: Token): Token {
        return token.copy(
            currentBalance = if (token.isETH()) {
                Convert.fromWei(BigDecimal(getEthBalance(owner)), Convert.Unit.ETHER)
            } else {
                (getBalance(owner, token.tokenAddress) ?: BigDecimal.ZERO).divide(
                    BigDecimal(10).pow(
                        token.tokenDecimal
                    )
                )
            }
        )
    }

    @Throws(Exception::class)
    fun getExpectedRate(
        walletAddress: String,
        contractAddress: String,
        srcToken: String,
        destToken: String,
        srcTokenAmount: BigInteger
    ): List<String> {
        val function = getExpectedRate(srcToken, destToken, srcTokenAmount)

        val responseValue = callSmartContractFunction(function, contractAddress, walletAddress)

        val responses = FunctionReturnDecoder.decode(
            responseValue, function.outputParameters
        )
        val rateResult = ArrayList<String>()
        for (rates in responses) {
            val rate = rates as Uint256
            val toEther = Convert.fromWei(
                BigDecimal(rate.value),
                Convert.Unit.ETHER
            )
            rateResult.add(
                (toEther * 0.97.toBigDecimal()).toPlainString()
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
        value: String,
        isEth: Boolean
    ): EthEstimateGas? {

        val function = Function(
            "tradeWithHint",
            listOf(
                Address(fromAddress),
                Uint256(BigInteger(value)),
                Address(toAddress),
                Address(walletAddress),
                Uint256(DEFAULT_MAX_AMOUNT),
                Uint256(BigInteger.ZERO),
                Address(DEFAULT_WALLET_ID),
                PERM.toBytes32()
            ),
            listOf<TypeReference<*>>()
        )

        return web3j.ethEstimateGas(
            Transaction(
                walletAddress,
                null,
                null,
                null,
                contractAddress,
                if (isEth) BigInteger(value) else BigInteger.ZERO,
                FunctionEncoder.encode(function)
            )
        ).send()


    }

    @Throws(IOException::class)
    private fun getNonce(addr: String): BigInteger {
        val getNonce =
            web3j.ethGetTransactionCount(addr, DefaultBlockParameterName.PENDING).send()
        return getNonce.transactionCount

    }

    @Throws(IOException::class)
    private fun tradeWithHint(
        fromAddress: String,
        value: String,
        toAddress: String,
        walletAddress: String
    ): Function {
        return Function(
            "tradeWithHint",
            listOf(
                Address(fromAddress),
                Uint256(BigInteger(value)),
                Address(toAddress),
                Address(walletAddress),
                Uint256(DEFAULT_MAX_AMOUNT),
                Uint256(BigInteger.ZERO),
                Address(DEFAULT_WALLET_ID),
                PERM.toBytes32()
            ),
            listOf<TypeReference<*>>()
        )
    }

    @Throws(IOException::class)
    fun doTransaction(
        param: SwapTokenUseCase.Param, credentials: Credentials,
        contractAddress: String
    ): String? {
        val gasPrice = Convert.toWei(
            param.swap.gasPrice,
            Convert.Unit.GWEI
        ).toBigInteger()
        val gasLimit = param.swap.gasLimit.toBigInteger()

        val amount = if (param.swap.tokenSource.isETH()) Convert.toWei(
            param.swap.sourceAmount,
            Convert.Unit.ETHER
        ).toBigIntegerExact() else BigInteger.ZERO

        val fromAddress = param.swap.tokenSource.tokenAddress

        val toAddress = param.swap.tokenDest.tokenAddress

        val walletAddress = param.wallet.address
        val txManager = RawTransactionManager(web3j, credentials)

        return if (param.swap.tokenSource.isETH()) {
            executeTradeWithHint(
                fromAddress,
                toAddress,
                amount,
                gasPrice,
                gasLimit,
                contractAddress,
                walletAddress,
                txManager
            )
        } else {
            handleTransferToken(
                fromAddress,
                amount,
                gasPrice,
                gasLimit,
                toAddress,
                walletAddress,
                contractAddress,
                txManager
            )
        }

    }

    private fun executeTradeWithHint(
        fromAddress: String,
        toAddress: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        contractAddress: String,
        walletAddress: String,
        txManager: TransactionManager

    ): String? {
        val transactionResponse = txManager.sendTransaction(
            gasPrice,
            gasLimit,
            contractAddress,
            FunctionEncoder.encode(
                tradeWithHint(
                    fromAddress,
                    amount.toString(),
                    toAddress,
                    walletAddress
                )
            ),
            amount
        )

        if (transactionResponse.hasError()) run {
            throw RuntimeException(
                "Error processing transaction request: " +
                    transactionResponse.error.message
            )
        }
        return transactionResponse.transactionHash
    }

    private fun handleTransferToken(
        fromAddress: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        toAddress: String,
        walletAddress: String,
        contractAddress: String,
        txManager: TransactionManager
    ): String? {
        val allowanceAmount =
            getContractAllowanceAmount(walletAddress, fromAddress, contractAddress, txManager)
        if (allowanceAmount < amount) {
            sendContractApproveTransferWithCondition(
                allowanceAmount,
                fromAddress,
                contractAddress,
                gasPrice,
                gasLimit,
                txManager
            )
        }
        return executeTradeWithHint(
            fromAddress,
            toAddress,
            amount,
            gasPrice,
            gasLimit,
            contractAddress,
            walletAddress,
            txManager
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
        transactionManager: TransactionManager
    ): BigInteger {

        val function = allowance(walletAddress, contractAddress)

        val ethCall = web3j.ethCall(
            Transaction.createEthCallTransaction(
                transactionManager.fromAddress, tokenAddress, FunctionEncoder.encode(function)
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
            Arrays.asList(
                Address(contractAddress),
                Uint256(amount)
            ),
            emptyList()
        )
    }

    private fun sendContractApproveTransferWithCondition(
        allowanceAmount: BigInteger,
        tokenAddress: String,
        contractAddress: String,
        gasPriceWei: BigInteger,
        gasLimit: BigInteger,
        transactionManager: TransactionManager
    ) {
        if (allowanceAmount > BigInteger.ZERO) {
            sendContractApproveTransfer(
                BigInteger.ZERO,
                tokenAddress,
                contractAddress,
                gasPriceWei,
                gasLimit,
                transactionManager
            )

        }
        sendContractApproveTransfer(
            DEFAULT_MAX_AMOUNT,
            tokenAddress,
            contractAddress,
            gasPriceWei,
            gasLimit,
            transactionManager
        )
    }

    private fun sendContractApproveTransfer(
        allowanceAmount: BigInteger,
        tokenAddress: String,
        contractAddress: String,
        gasPriceWei: BigInteger,
        gasLimit: BigInteger,
        transactionManager: TransactionManager
    ) {
        val function = approve(contractAddress, allowanceAmount)
        val encodedFunction = FunctionEncoder.encode(function)

        val transactionResponse = transactionManager.sendTransaction(
            gasPriceWei, gasLimit, tokenAddress,
            encodedFunction, BigInteger.ZERO
        )

        if (transactionResponse.hasError()) {
            throw RuntimeException("Error processing transaction request: " + transactionResponse.error.message)
        }
        Timber.e(transactionResponse.transactionHash)
    }
}
