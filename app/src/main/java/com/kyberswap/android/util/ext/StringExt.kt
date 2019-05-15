package com.kyberswap.android.util.ext

import com.kyberswap.android.domain.model.CustomBytes32
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

fun String.toWalletAddress(): String {
    return if (this.startsWith("0x")) {
        this
    } else {
        "0x$this"
    }
}

fun String?.percentage(other: String?): BigDecimal {
    if (other.isNullOrEmpty() || this.isNullOrEmpty()) return BigDecimal.ZERO
    if (other.toDouble() == 0.0) return BigDecimal.ZERO
    return try {
        (this.toBigDecimal() - other.toBigDecimal())
            .div(other.toBigDecimal())
            .multiply(100.toBigDecimal())
            .setScale(2, RoundingMode.UP)
    } catch (ex: Exception) {
        ex.printStackTrace()
        BigDecimal.ZERO
    }
}

fun String?.toBigDecimalOrDefaultZero(): BigDecimal {
    if (this.isNullOrEmpty()) return BigDecimal.ZERO
    return try {
        BigDecimal(this)
    } catch (ex: Exception) {
        ex.printStackTrace()
        BigDecimal.ZERO
    }
}

fun String.updatePrecision(): String {
    return (this.toBigDecimal() / 10.0.pow(18).toBigDecimal()).toPlainString()
}

fun String.toBytes32(): CustomBytes32 {
    val byteValue = toByteArray()
    val byteValueLen32 = ByteArray(32)
    System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.size)
    return CustomBytes32(byteValueLen32)
}