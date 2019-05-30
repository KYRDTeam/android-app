package com.kyberswap.android.data.db

import androidx.room.TypeConverter
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
