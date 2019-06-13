package com.kyberswap.android.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kyberswap.android.domain.model.Transaction
import java.math.BigDecimal


class DataTypeConverter {
    @TypeConverter
    fun stringToDecimal(data: String?): BigDecimal {
        return BigDecimal(data)
    }

    @TypeConverter
    fun bigDecimalToString(decimal: BigDecimal): String {
        return decimal.toString()
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

class ListTypeConverter {
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
