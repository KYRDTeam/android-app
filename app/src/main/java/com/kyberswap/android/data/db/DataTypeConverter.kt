package com.kyberswap.android.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.WalletBalance
import java.math.BigDecimal
import java.math.BigInteger


class DataTypeConverter {
    @TypeConverter
    fun stringToDecimal(data: String?): BigDecimal {
        return BigDecimal(data)
    }

    @TypeConverter
    fun bigDecimalToString(decimal: BigDecimal): String {
        return decimal.toPlainString()
    }
}

class BigIntegerDataTypeConverter {
    @TypeConverter
    fun stringToBigInteger(data: String?): BigInteger {
        return BigInteger(data)
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
.type
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
.type
        return Gson().fromJson(value, listType)
    }


    @TypeConverter
    fun pendingBalancesTypeToString(list: Map<String, BigDecimal>): String {
        return Gson().toJson(list)
    }
}

class ListTypeConverter {
    @TypeConverter
    fun stringToTokenPairType(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {
.type
        return Gson().fromJson(value, listType)
    }


    @TypeConverter
    fun tokenPairTypeToString(list: List<String>): String {
        return Gson().toJson(list)
    }
}

class WalletBalanceTypeConverter {
    @TypeConverter
    fun stringToWalletBalanceType(value: String): List<WalletBalance> {
        val listType = object : TypeToken<List<WalletBalance>>() {
.type
        return Gson().fromJson(value, listType)
    }


    @TypeConverter
    fun walletBalanceTypeToString(list: List<WalletBalance>): String {
        return Gson().toJson(list)
    }
}