package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.transaction.TransactionEntity
import com.kyberswap.android.data.db.TransactionTypeConverter
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "transactions",
    primaryKeys = ["hash", "from", "to"]
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
    val destAmount: String = ""

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
//        formatter.format(Date(entity.timeStamp.toLong() * 1000L)),
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
//        formatter.format(Date(entity.timeStamp.toLong() * 1000L)),
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

    enum class TransactionType {
        SEND, RECEIVED
    }

    val displayTransaction: String
        get() =
            if (isTransfer) {
                StringBuilder()
                    .append(if (type == TransactionType.SEND) "-" else "+")
                    .append(" ")
                    .append(
                        value.toBigDecimalOrDefaultZero()
                            .divide(
                                10.toBigDecimal()
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
        val formatterShort = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val formatterFull = SimpleDateFormat("EEEE, dd MMM yyyy'T'HH:mm:ssZZ", Locale.US)
    }

    val shortedDateTimeFormat: String
        get() = formatterShort.format(Date(timeStamp.toLong() * 1000L))

    val longDateTimeFormat: String
        get() = formatterFull.format(Date(timeStamp.toLong() * 1000L))

    val displayTransactionType: String
        get() = if (isTransfer)
            if (type == TransactionType.SEND) "SEND" else "RECEIVE"
        else "SWAP"


    private val isTransfer: Boolean
        get() = tokenSource.isEmpty() && tokenDest.isEmpty()
    val displayRate: String
        get() =
            if (isTransfer) {
                StringBuilder()
                    .append(if (type == TransactionType.SEND) "To: $to" else "From: $from")
                    .toString()
     else
                StringBuilder().append("1")
                    .append(" ")
                    .append(tokenSource)
                    .append(" = ")
                    .append(
                        if (sourceAmount.toBigDecimalOrDefaultZero() == BigDecimal.ZERO) "0" else
                            (destAmount.toBigDecimalOrDefaultZero()
                                .div(sourceAmount.toBigDecimalOrDefaultZero()))
                                .toDisplayNumber()
                    )
                    .append(" ")
                    .append(tokenDest)
                    .toString()


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