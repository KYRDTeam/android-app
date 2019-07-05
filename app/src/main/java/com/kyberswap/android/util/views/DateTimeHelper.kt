package com.kyberswap.android.util.views

import java.text.SimpleDateFormat
import java.util.*

object DateTimeHelper {
    private val displayFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US)
    private val fullFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    fun displayDate(date: String?): String {
        if (date.isNullOrEmpty()) return ""
        return try {
            displayFormat.format(fullFormat.parse(date))
 catch (ex: Exception) {
            ex.printStackTrace()
            date

    }
}