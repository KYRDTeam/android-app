package com.kyberswap.android.util.ext

import com.kyberswap.android.presentation.common.MIN_SUPPORT_AMOUNT
import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toDisplayNumber(): String {

    val internalValue = toDisplayNumberInternal().toBigDecimalOrDefaultZero().stripTrailingZeros()
    return if (internalValue.abs() > MIN_SUPPORT_AMOUNT) internalValue.toPlainString() else "0"
}

fun BigDecimal.formatDisplayNumber(): String {
    val internalValue = toDisplayNumberInternal().toBigDecimalOrDefaultZero().stripTrailingZeros()
    return if (internalValue.abs() > MIN_SUPPORT_AMOUNT) {
        getKSNumberFormat().format(internalValue)
    } else "0"
}

fun BigDecimal.toDisplayNumber(length: Int): String {
    return toDisplayNumberInternal(length)
}

fun BigDecimal.toDisplayNumberInternal(length: Int = 4): String {
    val stringNumber = this.stripTrailingZeros().toPlainString()
    val list = stringNumber.split(".")
    if (list.size == 1) return stringNumber
    else {
        val s = list[1]
        if (s[0] != '0') {
            val scaleString =
                list[0] + '.' + s.substring(0, if (s.length > length) length else s.length)
            return scaleString.toBigDecimalOrDefaultZero()
                .setScale(scaleString.length, RoundingMode.UP)
                .stripTrailingZeros().toPlainString()
        } else {
            var index = 0
            s.forEach { c ->
                if (c == '0') {
                    index++
                } else {
                    return list[0] + '.' + s.substring(0, index) + s.substring(
                        index,
                        if (s.length > index + length) index + length else s.length
                    )
                }
            }
            val scaleString =
                list[0] + '.' + s.substring(0, if (s.length > length) length else s.length)
            return scaleString
                .toBigDecimalOrDefaultZero()
                .stripTrailingZeros()
                .toPlainString()
        }
    }
}

fun BigDecimal.rounding(): BigDecimal {
    return if (this - this.toBigInteger()
            .toBigDecimal() > BigDecimal(1E-6)
    ) this else this.toBigInteger().toBigDecimal()
}