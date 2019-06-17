package com.kyberswap.android.util.ext

import java.math.BigInteger


fun BigInteger?.safeToString(): String {
    return this?.toString() ?: BigInteger.ZERO.toString()
}