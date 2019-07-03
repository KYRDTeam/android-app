package com.kyberswap.android.util.ext

import java.math.BigDecimal

inline fun <T> Iterable<T>.sumByBigDecimal(selector: (T) -> BigDecimal): BigDecimal {
    var sum: BigDecimal = BigDecimal.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}


fun List<String>.display(): String {
    val builder = StringBuilder()
    forEachIndexed { index, s ->
        builder.append(s)
        if (index < this.size - 1) {
            builder.append("\n")

    }
    return builder.toString()
}