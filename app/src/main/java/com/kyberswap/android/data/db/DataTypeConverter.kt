package com.kyberswap.android.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.WalletBalance
import com.kyberswap.android.domain.model.WcEthSendTransaction
import com.kyberswap.android.domain.model.WcEthSign
import com.kyberswap.android.domain.model.WcSessionRequest
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import java.math.BigDecimal
import java.math.BigInteger


class DataTypeConverter {
    @TypeConverter
    fun stringToDecimal(data: String?): BigDecimal {
        return data.toBigDecimalOrDefaultZero()
    }

    @TypeConverter
    fun bigDecimalToString(decimal: BigDecimal): String {
        return decimal.toPlainString()
    }
}

class BigIntegerDataTypeConverter {
    @TypeConverter
    fun stringToBigInteger(data: String?): BigInteger {
        return data.toBigIntegerOrDefaultZero()
    }

    @TypeConverter
    fun bigIntegerToString(bigInteger: BigInteger): String {
        return bigInteger.toString()
    }
}


class TransactionTypeConverter {
    @TypeConverter
    fun transactionTypeToInt(type: Transaction.TransactionType): Int {
        return type.ordinal
    }

    @TypeConverter
    fun intToTransactionType(type: Int): Transaction.TransactionType {
        return Transaction.TransactionType.values()[type]
    }
}

class TokenPairTypeConverter {
    @TypeConverter
    fun stringToTokenPairType(value: String): List<Pair<String, String>> {
        val listType = object : TypeToken<List<Pair<String, String>>>() {
        }.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun tokenPairTypeToString(list: List<Pair<String, String>>): String {
        return Gson().toJson(list)
    }
}


class PendingBalancesConverter {
    @TypeConverter
    fun stringToPendingBalancesType(value: String): Map<String, BigDecimal> {
        val listType = object : TypeToken<Map<String, BigDecimal>>() {
        }.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun pendingBalancesTypeToString(list: Map<String, BigDecimal>): String {
        return Gson().toJson(list)
    }
}

class ListStringConverter {
    @TypeConverter
    fun stringToTokenPairType(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {
        }.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun tokenPairTypeToString(list: List<String>): String {
        return Gson().toJson(list)
    }
}


class TransactionTypesConverter {
    @TypeConverter
    fun stringToTransactionTypes(value: String): List<Transaction.TransactionType> {
        val listType = object : TypeToken<List<Transaction.TransactionType>>() {
        }.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun transactionsTypeToString(list: List<Transaction.TransactionType>): String {
        return Gson().toJson(list)
    }
}

class WalletBalanceTypeConverter {
    @TypeConverter
    fun stringToWalletBalanceType(value: String): List<WalletBalance> {
        val listType = object : TypeToken<List<WalletBalance>>() {
        }.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun walletBalanceTypeToString(list: List<WalletBalance>): String {
        return Gson().toJson(list)
    }
}

class WalletConnectDataTypeConverter {
    @TypeConverter
    fun stringToWcEthSendTransaction(value: String?): WcEthSendTransaction? {
        if (value.isNullOrBlank()) return null
        val typeToken = object : TypeToken<WcEthSendTransaction>() {
        }.type
        return Gson().fromJson(value, typeToken)
    }

    @TypeConverter
    fun wcEthSendTransactionToString(wcEthSendTransaction: WcEthSendTransaction?): String {
        if (wcEthSendTransaction == null) return ""
        return Gson().toJson(wcEthSendTransaction)
    }

    @TypeConverter
    fun stringToWcSessionRequest(value: String?): WcSessionRequest? {
        if (value.isNullOrBlank()) return null
        val typeToken = object : TypeToken<WcSessionRequest>() {
        }.type
        return Gson().fromJson(value, typeToken)
    }

    @TypeConverter
    fun wcSessionRequestToString(wcSessionRequest: WcSessionRequest?): String {
        if (wcSessionRequest == null) return ""
        return Gson().toJson(wcSessionRequest)
    }

    @TypeConverter
    fun stringToWcEthSign(value: String?): WcEthSign? {
        if (value.isNullOrBlank()) return null
        val typeToken = object : TypeToken<WcEthSign>() {
        }.type
        return Gson().fromJson(value, typeToken)
    }

    @TypeConverter
    fun wcEthSignToString(wcEthSign: WcEthSign?): String {
        if (wcEthSign == null) return ""
        return Gson().toJson(wcEthSign)
    }
}