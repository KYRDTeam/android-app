package com.kyberswap.android.util.ext

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toDisplayNumber(): String {
    return toDisplayNumberInternal().toBigDecimalOrDefaultZero().stripTrailingZeros()
        .toPlainString()
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
            return scaleString.toBigDecimal().setScale(scaleString.length, RoundingMode.UP)
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
                .toBigDecimal()
                .stripTrailingZeros()
                .toPlainString()
        }
    }
}