package com.kyberswap.android.util.views

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeHelper {

    fun displayDate(
        date: String?,
        displayFormat: SimpleDateFormat = SimpleDateFormat(
            "HH:mm dd/MM/yyyy",
            Locale.US
        ),
        fullFormat: SimpleDateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            this.timeZone = TimeZone.getTimeZone("UTC")
        }
    ): String {
        if (date.isNullOrEmpty()) return ""
        return try {
            displayFormat.format(fullFormat.parse(date))
        } catch (ex: Exception) {
            ex.printStackTrace()
            date
        }
    }

    fun displayAlertDate(
        date: String?,
        displayFormat: SimpleDateFormat = SimpleDateFormat(
            "dd MMM yyyy",
            Locale.US
        ),
        fullFormat: SimpleDateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            this.timeZone = TimeZone.getTimeZone("UTC")
        }
    ): String {
        if (date.isNullOrEmpty()) return ""
        return try {
            displayFormat.format(fullFormat.parse(date))
        } catch (ex: Exception) {
            ex.printStackTrace()
            date
        }
    }

    fun toLong(date: String): Long {
        val fullFormat: SimpleDateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.US
        ).apply {
            this.timeZone = TimeZone.getTimeZone("UTC")
        }

        if (date.isBlank()) return 0
        return fullFormat.parse(date).time
    }

    fun transactionDate(
        time: Long, displayFormat: SimpleDateFormat = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.UK
        )
    ): String {
        return try {
            if (time > 0) {
                displayFormat.format(Date(time * 1000))
            } else ""
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ""
        }
    }
}