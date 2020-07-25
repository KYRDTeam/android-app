package com.kyberswap.android.util.ext

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun NumberFormat.setKSFractionDigits(): NumberFormat {
    this.maximumFractionDigits = 18
    return this
}

fun getKSNumberFormat(): NumberFormat {
    return DecimalFormat.getInstance(Locale.US).setKSFractionDigits()
}

fun NumberFormat.decimalSeparator(): Char {
    return if (this is DecimalFormat) {
        this.decimalFormatSymbols.decimalSeparator
    } else '.'
}

fun NumberFormat.thousandSeparator(): Char {
    return if (this is DecimalFormat) {
        this.decimalFormatSymbols.groupingSeparator
    } else ' '
}

fun NumberFormat.ksFormat(inputtedNumber: String, isNumberFormat: Boolean = true): String {
    val skipTrailingZeros =
        if (inputtedNumber.indexOf(decimalSeparator()) < 0) inputtedNumber else inputtedNumber.replace(
            "0*$".toRegex(),
            ""
        ).replace("\\.$".toRegex(), "")

    val value = skipTrailingZeros.replace(thousandSeparator().toString(), "")
    return if (isNumberFormat) {
        format(parse(value))
    } else {
        ksTextFormat(value, thousandSeparator().toString(), decimalSeparator().toString())
    }
}

private fun ksTextFormat(
    inputtedString: String,
    thousandSeparator: String,
    decimalSeparator: String
): String {
    val dotPoint = inputtedString.indexOf(decimalSeparator)
    val precision = if (dotPoint >= 0) inputtedString.substring(dotPoint) else ""
    val value = if (dotPoint >= 0) inputtedString.substring(0, dotPoint) else inputtedString
    val nums = value.toMutableList()
    var s = ""
    var count = 0
    for (i in nums.size - 1 downTo 0) {
        count++
        s = nums[i] + s
        if (i > 0 &&
            count % 3 == 0 && nums.size > 3
        ) {
            s = thousandSeparator + s
        }
    }
    return s + precision
}

