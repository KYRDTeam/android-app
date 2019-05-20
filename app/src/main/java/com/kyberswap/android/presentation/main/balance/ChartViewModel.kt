package com.kyberswap.android.presentation.main.balance

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChartViewModel @Inject constructor(
) : ViewModel()

@Parcelize
enum class ChartType : Parcelable {
    DAY, WEEK, MONTH, YEAR, ALL;

    val resolution: String
        get() = when (this) {
            DAY -> "15"
            WEEK, MONTH -> "60"
            YEAR, ALL -> "D"


    fun fromTime(toTime: Long): Long {
        return when (this) {
            DAY -> toTime - 24 * 60 * 60
            WEEK -> toTime - 7 * 24 * 60 * 60
            MONTH -> toTime - 30 * 24 * 60 * 60
            YEAR -> toTime - 365 * 24 * 60 * 60
            ALL -> 1

    }


    val dateFormatter: DateFormat
        get() {
            val pattern = when (this) {
                DAY -> "HH:mm"
                WEEK, MONTH -> "dd/MM HH:MM"
                YEAR, ALL -> "dd/MM"
    
            return SimpleDateFormat(pattern, Locale.US)


    fun label(forTime: Long): String {
        val date = Date(forTime)
        return dateFormatter.format(date)
    }
}

