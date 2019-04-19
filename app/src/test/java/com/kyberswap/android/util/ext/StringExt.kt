package com.kyberswap.android.util.ext

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.*

fun String.isEmailValid(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.toAge(pattern: String = "yyyy-MM-dd", suffix: String = "歳"): String {
    val format = SimpleDateFormat(pattern, Locale.JAPAN)
    try {
        val date = format.parse(this)
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob.time = date
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--


        return age.toString() + suffix
    } catch (e: Exception) {
        e.printStackTrace()
        return suffix
    }
}

fun String.toDate(
    dateFormat: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.JAPAN)
): Date {
    return dateFormat.parse(this)
}
