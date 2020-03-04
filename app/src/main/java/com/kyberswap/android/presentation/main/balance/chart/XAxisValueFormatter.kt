package com.kyberswap.android.presentation.main.balance.chart

import com.github.mikephil.charting.formatter.ValueFormatter
import com.kyberswap.android.domain.model.Chart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Created by philipp on 02/06/16.
 */
class XAxisValueFormatter(private val chart: Chart) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        if (chart.t.isEmpty()) return ""
        val time = chart.t[value.toInt()]
        val cal = Calendar.getInstance()
        cal.timeInMillis = time.toLong() * 1000L
        return SimpleDateFormat("HH:mm dd MMM yyyy", Locale.getDefault()).format(cal.time)

    }

}
