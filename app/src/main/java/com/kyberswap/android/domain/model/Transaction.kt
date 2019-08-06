package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.transaction.TransactionEntity
import com.kyberswap.android.data.db.TransactionTypeConverter
import com.kyberswap.android.util.ext.*
import kotlinx.android.parcel.Parcelize
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["hash", "transactionStatus", "walletAddress"])]
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
    @PrimaryKey
    val hash: String = "",
    val input: String = "",
    val isError: String = "",
    val nonce: String = "",
    val timeStamp: Long = 0,
    val to: String = "",
    val transactionIndex: String = "",
    val txreceiptStatus: String = "",
    val value: String = "",
    val tokenName: String = "",
    val tokenSymbol: String = "",
    val tokenDecimal: String = "",
    @TypeConverters(TransactionTypeConverter::class)
    val type: TransactionType = TransactionType.SWAP,
    val txType: String = "",
    val tokenSource: String = "",
    val sourceAmount: String = "",
    val tokenDest: String = "",
    val destAmount: String = "",
    val transactionStatus: String = "",
    val walletAddress: String = ""

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
        entity.timeStamp.toLongSafe(),
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
        entity.timeStamp.toLongSafe(),
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
        0,
        tx.to,
        tx.transactionIndex.toString(),
        "",
        tx.value.toString(),
        transactionStatus = PENDING_TRANSACTION_STATUS

    )

    fun with(tx: org.web3j.protocol.core.methods.response.Transaction): Transaction {
        return this.copy(
            blockHash = tx.blockHash ?: "",
            blockNumber = if (tx.blockNumberRaw.isNullOrEmpty()) "" else tx.blockNumber.safeToString(),
            from = tx.from ?: "",
            gasUsed = tx.gas.safeToString(),
            gasPrice = tx.gasPrice.safeToString(),
            hash = tx.hash ?: "",
            input = tx.input ?: "",
            isError = "0",
            nonce = tx.nonce.safeToString(),
            to = tx.to ?: "",
            transactionIndex = tx.transactionIndex.toString(),
            value = tx.value.toString()

        )
    }

    constructor(tx: TransactionReceipt) : this(
        tx.blockHash ?: "",
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
        System.currentTimeMillis() / 1000,
        tx.to ?: "",
        tx.transactionIndex.toString(),
        tx.status ?: ""
    )

    fun with(tx: TransactionReceipt): Transaction {
        return this.copy(
            blockHash = tx.blockHash ?: "",
            blockNumber = tx.blockNumber.toString(),
            contractAddress = tx.contractAddress ?: "",
            cumulativeGasUsed = tx.cumulativeGasUsed.toString(),
            from = tx.from ?: "",
            gasUsed = tx.gasUsed.toString(),
            hash = tx.transactionHash ?: "",
            isError = if (tx.isStatusOK) "0" else "1",
            to = tx.to ?: "",
            transactionIndex = tx.transactionIndex.toString(),
            txreceiptStatus = tx.status ?: ""
        )
    }

    enum class TransactionType {
        SEND, RECEIVED, SWAP;

        val value: String
            get() = when (this) {
                SWAP -> "SWAP"
                SEND -> "SEND"
                RECEIVED -> "RECEIVED"
    
    }

    val isTransactionFail: Boolean
        get() = isError == "1"

    val displayValue: String
        get() =
            StringBuilder()
                .append(
                    value.toBigDecimalOrDefaultZero()
                        .divide(
                            BigDecimal.TEN
                                .pow(
                                    tokenDecimal
                                        .toBigDecimalOrDefaultZero().toInt()
                                ), 18, RoundingMode.HALF_EVEN
                        ).toDisplayNumber()
                )
                .append(" ")
                .append(tokenSymbol)
                .toString()


    val displayTransaction: String
        get() =
            if (isTransfer) {
                StringBuilder()
                    .append(if (type == TransactionType.SEND) "-" else "+")
                    .append(" ")
                    .append(displayValue)
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
        val formatterShort = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val formatterFull = SimpleDateFormat("EEEE, dd MMM yyyy'T'HH:mm:ssZZZZZ", Locale.US)
    }

    fun sameDisplay(other: Transaction): Boolean {
        return this.displayTransaction == other.displayTransaction &&
            this.displayRate == other.displayRate &&
            this.displayTransactionType == other.displayTransactionType &&
            this.isTransactionFail == other.isTransactionFail &&
            this.timeStamp == other.timeStamp
    }


    val isPendingTransaction: Boolean
        get() = transactionStatus == PENDING_TRANSACTION_STATUS

    val shortedDateTimeFormat: String
        get() = formatterShort.format(Date(timeStamp * 1000L))

    val longDateTimeFormat: String
        get() = formatterFull.format(Date(timeStamp * 1000L))

    val displayTransactionType: String
        get() = type.value


    val isTransfer: Boolean
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