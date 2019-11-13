package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.transaction.TransactionEntity
import com.kyberswap.android.data.db.TransactionTypeConverter
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.safeToString
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toLongSafe
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Entity(
    tableName = "transactions",
    primaryKeys = ["hash", "from", "to"],
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
    @IgnoredOnParcel
    @Ignore
    var currentAddress: String = ""

    constructor(entity: TransactionEntity, transactionType: TransactionType, txType: String) : this(
        entity.blockHash ?: "",
        entity.blockNumber ?: "",
        entity.confirmations ?: "",
        entity.contractAddress ?: "",
        entity.cumulativeGasUsed ?: "",
        entity.from ?: "",
        entity.gas ?: "",
        entity.gasPrice ?: "",
        entity.gasUsed ?: "",
        entity.hash?.toLowerCase(Locale.getDefault()) ?: "",
        entity.input ?: "",
        entity.isError ?: "",
        entity.nonce ?: "",
        entity.timeStamp?.toLongSafe() ?: 0,
        entity.to ?: "",
        entity.transactionIndex ?: "",
        entity.txreceiptStatus ?: "",
        entity.value ?: "",
        entity.tokenName ?: "",
        entity.tokenSymbol ?: "",
        entity.tokenDecimal ?: "",
        transactionType,
        txType
    )


    constructor(entity: TransactionEntity, address: String, txType: String) : this(
        entity.blockHash ?: "",
        entity.blockNumber ?: "",
        entity.confirmations ?: "",
        entity.contractAddress ?: "",
        entity.cumulativeGasUsed ?: "",
        entity.from ?: "",
        entity.gas ?: "",
        entity.gasPrice ?: "",
        entity.gasUsed ?: "",
        entity.hash?.toLowerCase(Locale.getDefault()) ?: "",
        entity.input ?: "",
        entity.isError ?: "",
        entity.nonce ?: "",
        entity.timeStamp?.toLongSafe() ?: 0,
        entity.to ?: "",
        entity.transactionIndex ?: "",
        entity.txreceiptStatus ?: "",
        entity.value ?: "",
        entity.tokenName ?: "",
        entity.tokenSymbol ?: "",
        entity.tokenDecimal ?: "",
        if (entity.from?.toLowerCase(Locale.getDefault()) == address.toLowerCase(Locale.getDefault())) TransactionType.SEND else TransactionType.RECEIVED,
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
        tx.nonce.safeToString(), +
        0,
        tx.to,
        if (tx.transactionIndexRaw.isNullOrEmpty()) "" else tx.transactionIndex.safeToString(),
        "",
        tx.value.toString(),
        transactionStatus = PENDING_TRANSACTION_STATUS

    )

    fun with(tx: org.web3j.protocol.core.methods.response.Transaction): Transaction {
        return this.copy(
            blockHash = tx.blockHash ?: "",
            blockNumber = if (tx.blockNumberRaw.isNullOrEmpty()) "" else tx.blockNumber.safeToString(),
            gasUsed = tx.gas.safeToString(),
            gasPrice = tx.gasPrice.safeToString(),
            hash = tx.hash ?: "",
            input = tx.input ?: "",
            isError = "0",
            nonce = tx.nonce.safeToString(),
            transactionIndex = if (tx.transactionIndexRaw.isNullOrEmpty()) "" else tx.transactionIndex.safeToString(),
            value = tx.value.toString()

        )
    }

    constructor(tx: TransactionReceipt) : this(
        tx.blockHash ?: "",
        if (tx.blockNumberRaw.isNullOrEmpty()) "" else tx.blockNumber.safeToString(),
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
        if (tx.transactionIndexRaw.isNullOrEmpty()) "" else tx.transactionIndex.safeToString(),
        tx.status ?: ""
    )

    fun with(tx: TransactionReceipt): Transaction {
        return this.copy(
            blockHash = tx.blockHash ?: "",
            blockNumber = if (tx.blockNumberRaw.isNullOrEmpty()) "" else tx.blockNumber.safeToString(),
            contractAddress = tx.contractAddress ?: "",
            cumulativeGasUsed = tx.cumulativeGasUsed.toString(),
            gasUsed = tx.gasUsed.toString(),
            hash = tx.transactionHash ?: "",
            isError = if (tx.isStatusOK) "0" else "1",
            transactionIndex = if (tx.transactionIndexRaw.isNullOrEmpty()) "" else tx.transactionIndex.safeToString(),
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
                                ), 18, RoundingMode.UP
                        ).toDisplayNumber()
                )
                .append(" ")
                .append(tokenSymbol)
                .toString()

    val displaySource: String
        get() = StringBuilder()
            .append(sourceAmount.toBigDecimalOrDefaultZero().toDisplayNumber())
            .append(" ")
            .append(tokenSource)
            .toString()

    val displayDest: String
        get() = StringBuilder()
            .append(destAmount.toBigDecimalOrDefaultZero().toDisplayNumber())
            .append(" ")
            .append(tokenDest)
            .toString()

    val displayTransaction: String
        get() =
            if (isTransfer) {
                StringBuilder()
                    .append(if (isSend) "-" else "+")
                    .append(" ")
                    .append(displayValue)
                    .toString()
            } else

                StringBuilder()
                    .append(
                        displaySource
                    )
                    .append(" ➞ ")
                    .append(
                        displayDest
                    )
                    .toString()

    companion object {
        const val PENDING = 0
        const val MINED = 1
        const val PENDING_TRANSACTION_STATUS = "pending"
        var formatterShort = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        var formatterFull = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss", Locale.getDefault())
        var formatterFilter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        const val DEFAULT_DROPPED_BLOCK_NUMBER = 1L
    }

    fun sameDisplay(other: Transaction): Boolean {

        return this.displayTransaction == other.displayTransaction &&
            this.displayRate == other.displayRate &&
            this.transactionType == other.transactionType &&
            this.isTransactionFail == other.isTransactionFail &&
            this.timeStamp == other.timeStamp
    }

    val transactionType: TransactionType
        get() = when {
            this.isTransfer && this.from == currentAddress -> TransactionType.SEND
            this.isTransfer && this.to == currentAddress -> TransactionType.RECEIVED
            else -> this.type
        }

    val isSwap: Boolean
        get() = type == TransactionType.SWAP

    val isTxSend: Boolean
        get() = this.isTransfer && this.from.isNotEmpty()

    fun sameKey(other: Transaction): Boolean {
        return this.hash == other.hash &&
            this.from == other.from &&
            this.to == other.to
    }


    val isPendingTransaction: Boolean
        get() = transactionStatus == PENDING_TRANSACTION_STATUS

    val shortedDateTimeFormat: String
        get() = formatterShort.format(Date(timeStamp * 1000L))

    val filterDateTimeFormat: String
        get() = formatterFilter.format(Date(timeStamp * 1000L))

    val longDateTimeFormat: String
        get() {
            val timeZone = TimeZone.getDefault()
            formatterFull.timeZone = timeZone
            return formatterFull.format(Date(timeStamp * 1000L)) + " " + timeZone.getDisplayName(
                false,
                TimeZone.SHORT
            )
        }

    val isTransfer: Boolean
        get() = tokenSource.isEmpty() && tokenDest.isEmpty()

    private val isSend: Boolean
        get() = isTransfer && from == currentAddress

    fun isReceived(walletAddress: String): Boolean {
        return isTransfer && to == walletAddress
    }

    val displayRate: String
        get() =
            if (isTransfer) {
                StringBuilder()
                    .append(if (isSend) "To: ${to.displayWalletAddress()}" else "From: ${from.displayWalletAddress()}")
                    .toString()
            } else
                StringBuilder().append("1")
                    .append(" ")
                    .append(tokenSource)
                    .append(" = ")
                    .append(rate)
                    .append(" ")
                    .append(tokenDest)
                    .toString()

    val rate: String
        get() = if (sourceAmount.toDouble() == 0.0) "0" else
            (destAmount.toBigDecimalOrDefaultZero()
                .divide(sourceAmount.toBigDecimalOrDefaultZero(), 18, RoundingMode.CEILING))
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