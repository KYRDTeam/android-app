package com.kyberswap.android.util.ext

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun Calendar.format(format: String): String {
    val simpleDateFormat = SimpleDateFormat(format, Locale.JAPAN)
    return simpleDateFormat.format(this.time)
}