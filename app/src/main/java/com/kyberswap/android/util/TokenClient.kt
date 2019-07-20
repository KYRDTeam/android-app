package com.kyberswap.android.util

import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SwapTokenUseCase
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT
import com.kyberswap.android.presentation.common.DEFAULT_MAX_AMOUNT
import com.kyberswap.android.presentation.common.DEFAULT_WALLET_ID
import com.kyberswap.android.presentation.common.PERM
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.Hash
import org.web3j.crypto.Sign
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import javax.inject.Inject
import kotlin.math.pow


class TokenClient @Inject constructor(private val web3j: Web3j) {

    private fun balanceOf(owner: String): Function {
        return Function(
            "balanceOf",
            listOf(Address(owner)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {
    )
        )
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
        ,
                object : TypeReference<Uint256>() {

        )
        )
    }

    @Throws(Exception::class)
    fun getEthBalance(owner: String?): BigInteger {
        if (owner == null) return BigInteger.ZERO
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
    fun updateBalance(walletAddress: String?, tokenAddress: String?): BigDecimal? {
        if (walletAddress == null || tokenAddress == null) return BigDecimal.ZERO
        val function = balanceOf(walletAddress)
        val responseValue = callSmartContractFunction(function, tokenAddress, walletAddress)

        val response = FunctionReturnDecoder.decode(
            responseValue, function.outputParameters
        )
        return if (response.size == 1) {
            BigDecimal((response[0] as Uint256).value)
 else {
            null

    }

    @Throws(Exception::class)
    fun updateBalance(token: Token): Token {
        return token.updateBalance(
            if (token.isETH) {
                Convert.fromWei(BigDecimal(getEthBalance(token.owner)), Convert.Unit.ETHER)
     else {
                (updateBalance(token.owner, token.tokenAddress) ?: BigDecimal.ZERO).divide(
                    BigDecimal(10).pow(
                        token.tokenDecimal
                    )
                )
    
        )

    }

    @Throws(Exception::class)
    fun getExpectedRate(
        walletAddress: String,
        contractAddress: String,
        tokenSource: Token,
        tokenDest: Token,
        srcTokenAmount: BigInteger
    ): List<String> {
        val function =
            getExpectedRate(tokenSource.tokenAddress, tokenDest.tokenAddress, srcTokenAmount)

        val responseValue = callSmartContractFunction(function, contractAddress, walletAddress)

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
                toEther.toDisplayNumber()
            )

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

        val function = tradeWithHint(
            fromAddress,
            toAddress,
            amount,
            minConversionRate,
            walletAddress
        )

        return web3j.ethEstimateGas(
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
        contractAddress: String,
        value: String,
        isEth: Boolean
    ): EthEstimateGas? {

        return web3j.ethEstimateGas(
            Transaction(
                walletAddress,
                null,
                null,
                null,
                contractAddress,
                if (isEth) value.toBigIntegerOrDefaultZero() else BigInteger.ZERO,
                FunctionEncoder.encode(transfer(contractAddress, value))
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
    fun doSwap(
        param: SwapTokenUseCase.Param, credentials: Credentials,
        contractAddress: String
    ): String? {
        val gasPrice = Convert.toWei(
            param.swap.gasPrice.toBigDecimalOrDefaultZero(),
            Convert.Unit.GWEI
        ).toBigInteger()
        val gasLimit =
            if (param.swap.gasLimit.toBigInteger() == BigInteger.ZERO) DEFAULT_GAS_LIMIT
            else param.swap.gasLimit.toBigInteger()

        val tradeWithHintAmount = param.swap.sourceAmount.toBigDecimalOrDefaultZero()
            .times(10.0.pow(param.swap.tokenSource.tokenDecimal).toBigDecimal()).toBigInteger()

        val transactionAmount =
            if (param.swap.tokenSource.isETH) tradeWithHintAmount else BigInteger.ZERO

        val minConversionRate = param.swap.minConversionRate

        val fromAddress = param.swap.tokenSource.tokenAddress

        val toAddress = param.swap.tokenDest.tokenAddress

        val walletAddress = if (param.wallet.isPromoPayment) param.wallet.promo?.receiveAddress
            ?: param.wallet.address else
            param.wallet.address


        val txManager = RawTransactionManager(web3j, credentials)

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
                txManager
            )
 else {
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
                txManager
            )


    }


    @Throws(IOException::class)
    fun doTransferTransaction(
        param: TransferTokenUseCase.Param, credentials: Credentials
    ): String? {
        val gasPrice = Convert.toWei(
            param.send.gasPrice.toBigDecimalOrDefaultZero(),
            Convert.Unit.GWEI
        ).toBigInteger()
        val gasLimit = param.send.gasLimit.toBigIntegerOrDefaultZero()

        val isEth = param.send.tokenSource.isETH


        val amount = param.send
            .sourceAmount
            .toBigDecimalOrDefaultZero().multiply(
                10.toBigDecimal().pow(param.send.tokenSource.tokenDecimal)
            ).toBigInteger()

        val transactionAmount = if (isEth) amount else BigInteger.ZERO

        val txManager = RawTransactionManager(web3j, credentials)

        val transactionResponse = txManager.sendTransaction(
            gasPrice,
            gasLimit,
            if (isEth) param.send.contact.address else param.send.tokenSource.tokenAddress,
            if (isEth) "" else
                FunctionEncoder.encode(
                    transfer(
                        param.send.contact.address,
                        amount.toString()
                    )
                ),
            transactionAmount
        )

        if (transactionResponse.hasError()) run {
            throw RuntimeException(
                "Error processing transaction request: " +
                    transactionResponse.error.message
            )

        return transactionResponse.transactionHash
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
        txManager: TransactionManager

    ): String? {
        val transactionResponse = txManager.sendTransaction(
            gasPrice,
            gasLimit,
            contractAddress,
            FunctionEncoder.encode(
                tradeWithHint(
                    fromAddress,
                    toAddress,
                    tradeWithHintAmount,
                    minConversionRate,
                    walletAddress
                )
            ),
            transactionAmount
        )

        if (transactionResponse.hasError()) run {
            throw RuntimeException(
                "Error processing transaction request: " +
                    transactionResponse.error.message
            )

        return transactionResponse.transactionHash
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
        txManager: TransactionManager
    ): String? {
        val allowanceAmount =
            getContractAllowanceAmount(
                walletAddress,
                fromToken.tokenAddress,
                contractAddress,
                txManager
            )
        if (allowanceAmount < tradeWithHintAmount) {
            sendContractApproveTransferWithCondition(
                allowanceAmount,
                fromToken,
                contractAddress,
                gasPrice,
                gasLimit,
                txManager
            )

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
    )
        )
    }

    fun getContractAllowanceAmount(
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
            listOf(
                Address(contractAddress),
                Uint256(amount)
            ),
            emptyList()
        )
    }

    private fun sendContractApproveTransferWithCondition(
        allowanceAmount: BigInteger,
        token: Token,
        contractAddress: String,
        gasPriceWei: BigInteger,
        gasLimit: BigInteger,
        transactionManager: TransactionManager
    ) {
        if (allowanceAmount > BigInteger.ZERO) {
            sendContractApproveTransfer(
                BigInteger.ZERO,
                token,
                contractAddress,
                gasPriceWei,
                if (token.gasApprove > BigDecimal.ZERO) token.gasApprove.toBigInteger() else gasLimit,
                transactionManager
            )


        sendContractApproveTransfer(
            DEFAULT_MAX_AMOUNT,
            token,
            contractAddress,
            gasPriceWei,
            gasLimit,
            transactionManager
        )
    }

    private fun sendContractApproveTransfer(
        allowanceAmount: BigInteger,
        token: Token,
        contractAddress: String,
        gasPriceWei: BigInteger,
        gasLimit: BigInteger,
        transactionManager: TransactionManager
    ) {
        val function = approve(contractAddress, allowanceAmount)
        val encodedFunction = FunctionEncoder.encode(function)

        val transactionResponse = transactionManager.sendTransaction(
            gasPriceWei, gasLimit, token.tokenAddress,
            encodedFunction, BigInteger.ZERO
        )

        if (transactionResponse.hasError()) {
            throw RuntimeException("Error processing transaction request: " + transactionResponse.error.message)

    }

    fun monitorPendingTransactions(transactions: List<com.kyberswap.android.domain.model.Transaction>): List<com.kyberswap.android.domain.model.Transaction> {
        val ethTransactions = mutableListOf<com.kyberswap.android.domain.model.Transaction>()
        for (s in transactions.map {
            it.hash
) {
            val transaction = web3j.ethGetTransactionByHash(s).send().transaction
            if (transaction.isPresent) {
                val tx = transaction.get()
                if (tx.hash.isNotEmpty()) {
                    val ethGetTransactionReceipt =
                        web3j.ethGetTransactionReceipt(tx.hash).send().transactionReceipt
                    if (ethGetTransactionReceipt.isPresent) {
                        val txReceipt = ethGetTransactionReceipt.get()
                        ethTransactions.add(com.kyberswap.android.domain.model.Transaction(txReceipt))
             else {
                        ethTransactions.add(
                            com.kyberswap.android.domain.model.Transaction(
                                tx
                            )
                        )
            
         else {
                    ethTransactions.add(com.kyberswap.android.domain.model.Transaction(tx).copy(hash = s))
        
    

        if (ethTransactions.isEmpty()) return transactions
        return ethTransactions.toList()
    }

    fun signOrder(
        order: LocalLimitOrder,
        credentials: Credentials,
        contractAddress: String
    ): String {

        val txManager = RawTransactionManager(web3j, credentials)
        val allowanceAmount =
            getContractAllowanceAmount(
                order.userAddr,
                order.tokenSource.tokenAddress,
                contractAddress,
                txManager
            )
        if (allowanceAmount < order.sourceAmountWithPrecision) {
            sendContractApproveTransferWithCondition(
                allowanceAmount,
                order.tokenSource,
                contractAddress,
                Convert.toWei(
                    order.gasPrice.toBigDecimalOrDefaultZero(),
                    Convert.Unit.GWEI
                ).toBigInteger(),
                order.gasLimit,
                txManager
            )



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
}
