package com.kyberswap.android.util.ext

import android.text.SpannableString
import android.text.Spanned
import com.kyberswap.android.domain.model.CustomBytes32
import com.kyberswap.android.presentation.common.ClickableSpan
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.pow

fun String.toWalletAddress(): String {
    return if (this.startsWith("0x")) {
        this
    } else {
        "0x$this"
    }
}

fun String.hexWithPrefix(): String {
    return if (this.startsWith("0x")) {
        this
    } else {
        "0x$this"
    }
}

fun String?.toNumberFormat(): String {
    return toBigDecimalOrDefaultZero().formatDisplayNumber()
}


fun String.clickableSpan(text: String, link: String): SpannableString {
    val spannableString = SpannableString(this)
    val clickableSpan = ClickableSpan(link)

    try {
        spannableString.setSpan(
            clickableSpan,
            spannableString.indexOf(text),
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
    return spannableString
}

fun String.withoutSeparator(): String {
    return replace(getKSNumberFormat().thousandSeparator().toString(), "")
}

fun String.validPassword(): Boolean {
    return Pattern.compile(PATTERN_REGEX_PASSWORD).matcher(this).matches()
}


const val PATTERN_REGEX_PASSWORD =
    "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+\$).{8,}\$"

fun String.toLongSafe(): Long {
    if (isNullOrEmpty()) return 0
    return try {
        toLong()
    } catch (ex: NumberFormatException) {
        ex.printStackTrace()
        0L
    }
}

fun String.toLongSafe(radix: Int): Long {
    if (isNullOrEmpty()) return 0
    return try {
        this.removePrefix("0x").toLong(radix = radix)
    } catch (ex: NumberFormatException) {
        ex.printStackTrace()
        0L
    }
}


fun String?.toBigIntSafe(): BigInteger {
    if (isNullOrEmpty()) return BigInteger.ZERO
    try {
        return BigInteger(this.removePrefix("0x"), 16)
    } catch (ex: java.lang.NumberFormatException) {
        ex.printStackTrace()
        return BigInteger.ZERO
    }
}

fun String.toDoubleSafe(): Double {
    if (isNullOrEmpty()) return 0.0
    return try {
        toDoubleOrDefaultZero()
    } catch (ex: NumberFormatException) {
        ex.printStackTrace()
        0.0
    }
}

fun CharSequence?.isENSAddress(): Boolean {
    return this?.contains(".") == true
}

fun CharSequence?.isUDAddress(): Boolean {
    return (this?.endsWith(".crypto", true) == true) || (this?.endsWith(".zil", true) == true)
}

fun String.onlyAddress(): String {
    val index = this.indexOf("0x")
    return if (index >= 0) {
        val prefix = this.substring(0, this.indexOf("0x"))
        this.removePrefix(prefix).trim()
    } else {
        this
    }
}

fun CharSequence.ensAddress(): String? {
    return if (this.indexOf("0x") > 0) {
        val index = this.indexOf(".")
        val lastIndexOf = this.lastIndexOf("-")
        if (lastIndexOf > index) {
            return this.take(lastIndexOf).trim().toString().toLowerCase(Locale.getDefault())
        } else {
            null
        }
    } else {
        this.toString().toLowerCase(Locale.getDefault())
    }
}


fun String?.percentage(other: String?): BigDecimal {
    if (other.isNullOrEmpty() || this.isNullOrEmpty()) return BigDecimal.ZERO
    if (other.toBigDecimalOrDefaultZero() == BigDecimal.ZERO || other.toDoubleOrDefaultZero() == 0.0) return BigDecimal.ZERO
    return try {
        (this.toDoubleOrDefaultZero() - other.toDoubleOrDefaultZero()).div(other.toDoubleOrDefaultZero())
            .times(100f)
            .toBigDecimal()
            .setScale(2, RoundingMode.UP)
    } catch (ex: Exception) {
        ex.printStackTrace()
        BigDecimal.ZERO
    }
}

fun String.toDate(): Date {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.parse(this)
    } catch (ex: Exception) {
        ex.printStackTrace()
        Date()
    }
}

fun String?.toBigDecimalOrDefaultZero(): BigDecimal {
    if (this.isNullOrEmpty()) return BigDecimal.ZERO
    return try {
        val format = getKSNumberFormat().parse(this).toString()
        BigDecimal(format)
    } catch (ex: Exception) {
        ex.printStackTrace()
        BigDecimal.ZERO
    }
}

fun CharSequence?.toBigDecimalOrDefaultZero(): BigDecimal {
    if (this.isNullOrEmpty()) return BigDecimal.ZERO
    return try {
        toString().toBigDecimalOrDefaultZero()
    } catch (ex: Exception) {
        ex.printStackTrace()
        BigDecimal.ZERO
    }
}

fun String.shortenValue(): String {
    return StringBuilder()
        .append(substring(0, if (length > 5) 5 else length))
        .append("...")
        .append(
            substring(
                if (length > 6) {
                    length - 6
                } else length
            )
        ).toString()
}


fun String?.toBigIntegerOrDefaultZero(): BigInteger {
    if (this.isNullOrEmpty()) return BigInteger.ZERO
    return try {
        val format = getKSNumberFormat().parse(this).toString()
        BigInteger(format)
    } catch (ex: Exception) {
        ex.printStackTrace()
        BigInteger.ZERO
    }
}

fun String.exactAmount(): String {
    return if (this.toDoubleOrDefaultZero() != 0.0) {
        this
    } else {
        "0"
    }
}

fun String?.toDoubleOrDefaultZero(): Double {
    if (this.isNullOrEmpty() || this.trim() == "--") return 0.0
    return try {
        getKSNumberFormat().parse(this).toDouble()
    } catch (ex: Exception) {
        ex.printStackTrace()
        0.0
    }
}

fun String.updatePrecision(): String {
    return (this.toDoubleOrDefaultZero() / 10.0.pow(18)).toBigDecimal().toPlainString()
}

fun String.toBytes32(): CustomBytes32 {
    val byteValue = toByteArray()
    val byteValueLen32 = ByteArray(32)
    System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.size)
    return CustomBytes32(byteValueLen32)
}

fun String.isContact(): Boolean {
    return (startsWith("0x") && length == 42)
}