package com.kyberswap.android.util.views

import java.text.SimpleDateFormat
import java.util.*

object DateTimeHelper {

    fun displayDate(
        date: String?,
        displayFormat: SimpleDateFormat = SimpleDateFormat(
            "HH:mm dd/MM/yyyy",
            Locale.UK
        ),
        fullFormat: SimpleDateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.UK
        ).apply {
            this.timeZone = TimeZone.getTimeZone("UTC")

    ): String {
        if (date.isNullOrEmpty()) return ""
        return try {
            displayFormat.format(fullFormat.parse(date))
 catch (ex: Exception) {
            ex.printStackTrace()
            date

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
     else ""
 catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ""

    }
}