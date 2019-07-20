package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.transaction.TransactionEntity
import com.kyberswap.android.data.db.TransactionTypeConverter
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.safeToString
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "transactions",
    primaryKeys = ["hash", "from", "to"],
    indices = [Index(value = ["hash", "transactionStatus"])]
)
@Parcelize
data class Transaction(
    val blockHash: String = "",
    val blockNumber: String = "",
    val confirmations: String = "",
    val contractAddress: String = "",
    val cumulativeGasUsed: String = "",
    val from: String = "",
    val gas: String = "",
    val gasPrice: String = "",
    val gasUsed: String = "",
    val hash: String = "",
    val input: String = "",
    val isError: String = "",
    val nonce: String = "",
    val timeStamp: String = "",
    val to: String = "",
    val transactionIndex: String = "",
    val txreceiptStatus: String = "",
    val value: String = "",
    val tokenName: String = "",
    val tokenSymbol: String = "",
    val tokenDecimal: String = "",
    @TypeConverters(TransactionTypeConverter::class)
    val type: TransactionType = TransactionType.SEND,
    val txType: String = "",
    val tokenSource: String = "",
    val sourceAmount: String = "",
    val tokenDest: String = "",
    val destAmount: String = "",
    val transactionStatus: String = ""

) : Parcelable {
    constructor(entity: TransactionEntity, transactionType: TransactionType, txType: String) : this(
        entity.blockHash,
        entity.blockNumber,
        entity.confirmations,
        entity.contractAddress,
        entity.cumulativeGasUsed,
        entity.from,
        entity.gas,
        entity.gasPrice,
        entity.gasUsed,
        entity.hash.toLowerCase(),
        entity.input,
        entity.isError,
        entity.nonce,
        entity.timeStamp,
        entity.to,
        entity.transactionIndex,
        entity.txreceiptStatus,
        entity.value,
        entity.tokenName,
        entity.tokenSymbol,
        entity.tokenDecimal,
        transactionType,
        txType
    )


    constructor(entity: TransactionEntity, address: String, txType: String) : this(
        entity.blockHash,
        entity.blockNumber,
        entity.confirmations,
        entity.contractAddress,
        entity.cumulativeGasUsed,
        entity.from,
        entity.gas,
        entity.gasPrice,
        entity.gasUsed,
        entity.hash.toLowerCase(),
        entity.input,
        entity.isError,
        entity.nonce,
        entity.timeStamp,
        entity.to,
        entity.transactionIndex,
        entity.txreceiptStatus,
        entity.value,
        entity.tokenName,
        entity.tokenSymbol,
        entity.tokenDecimal,
        if (entity.from == address) TransactionType.SEND else TransactionType.RECEIVED,
        txType
    )

    constructor(tx: org.web3j.protocol.core.methods.response.Transaction) : this(
        tx.blockHash,
        if (tx.blockNumberRaw.isNullOrEmpty()) "" else tx.blockNumber.safeToString(),
        "",
        "",
        "",
        tx.from,
        tx.gas.safeToString(),
        tx.gasPrice.safeToString(),
        "",
        tx.hash,
        tx.input,
        "0",
        tx.nonce.safeToString(),
        "0",
        tx.to,
        tx.transactionIndex.toString(),
        "",
        tx.value.toString()

    )

    constructor(tx: TransactionReceipt) : this(
        tx.blockHash,
        tx.blockNumber.toString(),
        "",
        tx.contractAddress ?: "",
        tx.cumulativeGasUsed.toString(),
        tx.from ?: "",
        "",
        "",
        tx.gasUsed.toString(),
        tx.transactionHash ?: "",
        "",
        if (tx.isStatusOK) "0" else "1",
        "",
        "0",
        tx.to ?: "",
        tx.transactionIndex.toString(),
        tx.status ?: ""
    )

    enum class TransactionType {
        SEND, RECEIVED, SWAP
    }

    val isTransactionFail: Boolean
        get() = isError == "1"

    val displayTransaction: String
        get() =
            if (isTransfer) {
                StringBuilder()
                    .append(if (type == TransactionType.SEND) "-" else "+")
                    .append(" ")
                    .append(
                        value.toBigDecimalOrDefaultZero()
                            .divide(
                                BigDecimal.TEN
                                    .pow(
                                        tokenDecimal
                                            .toBigDecimalOrDefaultZero().toInt()
                                    )
                            )
                    ).append(" ")
                    .append(tokenSymbol)
                    .toString()
     else

                StringBuilder()
                    .append(
                        sourceAmount
                    )
                    .append(" ")
                    .append(tokenSource)
                    .append(" ➞ ")
                    .append(
                        destAmount
                    )
                    .append(" ")
                    .append(tokenDest)
                    .toString()

    companion object {
        const val PENDING = 0
        const val MINED = 1
        const val PENDING_TRANSACTION_STATUS = "pending"
        const val SWAP_TRANSACTION = "SWAP"
        const val SEND_TRANSACTION = "SEND"
        const val RECEIVE_TRANSACTION = "RECEIVE"
        val formatterShort = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val formatterFull = SimpleDateFormat("EEEE, dd MMM yyyy'T'HH:mm:ssZZZZZ", Locale.ENGLISH)

    }

    val isPendingTransaction: Boolean
        get() = transactionStatus == PENDING_TRANSACTION_STATUS

    val shortedDateTimeFormat: String
        get() = if (timeStamp.isNotEmpty()) formatterShort.format(Date(timeStamp.toLong() * 1000L)) else "0"

    val longDateTimeFormat: String
        get() = if (timeStamp.isNotEmpty()) formatterFull.format(Date(timeStamp.toLong() * 1000L)) else "0"

    val displayTransactionType: String
        get() = if (isTransfer)
            if (type == TransactionType.SEND) SEND_TRANSACTION else RECEIVE_TRANSACTION
        else SWAP_TRANSACTION


    private val isTransfer: Boolean
        get() = tokenSource.isEmpty() && tokenDest.isEmpty()
    val displayRate: String
        get() =
            if (isTransfer) {
                StringBuilder()
                    .append(if (type == TransactionType.SEND) "To: ${to.displayWalletAddress()}" else "From: ${from.displayWalletAddress()}")
                    .toString()
     else
                StringBuilder().append("1")
                    .append(" ")
                    .append(tokenSource)
                    .append(" = ")
                    .append(rate)
                    .append(" ")
                    .append(tokenDest)
                    .toString()


    val rate: String
        get() = if (sourceAmount.toBigDecimalOrDefaultZero() == BigDecimal.ZERO) "0" else
            (destAmount.toBigDecimalOrDefaultZero()
                .div(sourceAmount.toBigDecimalOrDefaultZero()))
                .toDisplayNumber()

    val fee: String
        get() = Convert.fromWei(
            gasPrice.toBigDecimalOrDefaultZero().multiply(gasUsed.toBigDecimalOrDefaultZero())
            , Convert.Unit.ETHER
        ).toDisplayNumber()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (from != other.from) return false
        if (hash != other.hash) return false
        if (to != other.to) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }
}