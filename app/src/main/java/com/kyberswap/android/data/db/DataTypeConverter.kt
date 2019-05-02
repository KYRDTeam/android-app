package com.kyberswap.android.data.db

import androidx.room.TypeConverter
import java.math.BigDecimal

class DataTypeConverter {
    @TypeConverter
    fun stringToDecimal(data: String?): BigDecimal {
        return BigDecimal(data)
    }

    @TypeConverter
    fun bigDecimalTOString(decimal: BigDecimal): String {
        return decimal.toString()
    }
}
