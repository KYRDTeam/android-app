package com.kyberswap.android.util.ext

import java.math.BigDecimal

fun BigDecimal.toDisplayNumber(): String {
    val stringNumber = this.toString()
    val list = stringNumber.split(".")
    if (list.size == 1) return stringNumber
    else {
        val s = list[1]
        if (s[0] != '0') {
            return list[0] + '.' + s.substring(0, 4)
        } else {
            var index = 0
            s.forEach { c ->
                if (c == '0') {
                    index++
                } else {
                    return list[0] + '.' + s.substring(0, index) + s.substring(
                        index,
                        if (s.length > index + 4) index + 4 else s.length
                    )
                }
            }
        }
    }

    return stringNumber
}