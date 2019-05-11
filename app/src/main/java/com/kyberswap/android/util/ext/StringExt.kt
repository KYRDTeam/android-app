package com.kyberswap.android.util.ext

import kotlin.math.pow

fun String.toWalletAddress(): String {
    return if (this.startsWith("0x")) {
        this
    } else {
        "0x$this"
    }
}

fun String?.percentage(other: String?): Double {
    if (other.isNullOrEmpty() || this.isNullOrEmpty()) return 0.0
    if (other.toDouble() == 0.0) return 0.0
    return try {
        ((this.toDouble() - other.toDouble()) / other.toDouble() * 100)
    } catch (ex: Exception) {
        ex.printStackTrace()
        0.0
    }
}

fun String?.toDoubleOrDefaultZero(): Double {
    if (this.isNullOrEmpty()) return 0.0
    return try {
        this.toDouble()
    } catch (ex: Exception) {
        ex.printStackTrace()
        0.0
    }
}

fun String.updatePrecision(): String {
    return (this.toDouble() / 10.0.pow(18)).toString()
}