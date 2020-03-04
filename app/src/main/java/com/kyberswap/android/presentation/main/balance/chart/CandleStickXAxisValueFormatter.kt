package com.kyberswap.android.presentation.main.balance.chart

import com.github.mikephil.charting.formatter.ValueFormatter
import com.kyberswap.android.domain.model.Chart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Created by philipp on 02/06/16.
 */
class CandleStickXAxisValueFormatter(private val chart: Chart, private val type: ChartType?) :
    ValueFormatter() {

    override fun getFormattedValue(value: Float): String {

        if (chart.t.isEmpty()) return ""
        if (value.toInt().toFloat() == value && (value.toInt() % 8 == 0)) {
            val time = chart.t[0].toFloat() + value * 60.0 * 15.0
            val cal = Calendar.getInstance()
            cal.timeInMillis = time.toLong() * 1000L
            return SimpleDateFormat(
                if (type == ChartType.DAY) "HH:mm dd/MM" else "HH:mm dd/MM/yyyy",
                Locale.getDefault()
            ).format(cal.time)
        } else {
            return ""
        }
    }
}
