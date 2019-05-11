package com.kyberswap.android.util

import com.kyberswap.android.domain.model.Token
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Convert
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
        token.currentBalance = if (token.isETH()) {
            Convert.fromWei(BigDecimal(getEthBalance(owner)), Convert.Unit.ETHER)
        } else {
            (getBalance(owner, token.tokenAddress) ?: BigDecimal.ZERO).divide(
                BigDecimal(10).pow(
                    token.tokenDecimal
                )
            )
        }
        return token
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
            val rateString = Convert.fromWei(
                BigDecimal(rate.value),
                Convert.Unit.ETHER
            ).toPlainString()
            rateString.toDouble() * 0.97
            rateResult.add(
                (rateString.toDouble() * 0.97).toString()
            )
        }
        return rateResult
    }
}
