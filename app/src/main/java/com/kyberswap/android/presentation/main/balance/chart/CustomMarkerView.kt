package com.kyberswap.android.presentation.main.balance.chart

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.kyberswap.android.R
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber


class CustomMarkerView(context: Context, private val xAxisValueFormatter: ValueFormatter) :
    MarkerView(context, R.layout.custom_marker_view) {
    private val tvOpen: TextView = findViewById(R.id.tvOpen)
    private val tvTime: TextView = findViewById(R.id.tvTime)
    private val tvHigh: TextView = findViewById(R.id.tvHigh)
    private val tvLow: TextView = findViewById(R.id.tvLow)
    private val tvClose: TextView = findViewById(R.id.tvClose)
    private val tvChange: TextView = findViewById(R.id.tvChange)
    private val tvChangePercent: TextView = findViewById(R.id.tvChangePercent)

    private val makerTime by lazy {
        resources.getString(R.string.marker_time)
    }

    private val makerOpen by lazy {
        context.getString(R.string.marker_open)
    }

    private val makerHigh by lazy {
        context.getString(R.string.marker_high)
    }

    private val makerLow by lazy {
        context.getString(R.string.marker_low)
    }

    private val makerClose by lazy {
        context.getString(R.string.marker_close)
    }

    private val makerChange by lazy {
        context.getString(R.string.marker_change)
    }

    private val makerChangePercent by lazy {
        context.getString(R.string.marker_change_percent)
    }

    private val decreasingColor by lazy {
        ContextCompat.getColor(context, R.color.desc_color)
    }

    private val increasingColor by lazy {
        ContextCompat.getColor(context, R.color.inc_color)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            if (e is CandleEntry) {
                e.let {
                    tvTime.text =
                        String.format(xAxisValueFormatter.getFormattedValue(e.x), makerTime)
                    tvOpen.text = String.format(
                        makerOpen,
                        e.open.toString().toBigDecimalOrDefaultZero().toDisplayNumber()
                    )
                    tvHigh.text = String.format(
                        makerHigh,
                        e.high.toString().toBigDecimalOrDefaultZero().toDisplayNumber()
                    )
                    tvLow.text = String.format(
                        makerLow,
                        e.low.toString().toBigDecimalOrDefaultZero().toDisplayNumber()
                    )

                    tvClose.text = String.format(
                        makerClose,
                        e.close.toString().toBigDecimalOrDefaultZero().toDisplayNumber()
                    )

                    tvChange.text = String.format(
                        makerChange,
                        (e.close - e.open).toBigDecimal().toDisplayNumber()
                    )

                    tvChangePercent.text = String.format(
                        makerChangePercent,
                        ((e.close - e.open) / e.open * 100)
                    )
                    if (e.close >= e.open) {
                        tvChange.setTextColor(increasingColor)
                        tvChangePercent.setTextColor(increasingColor)
                    } else {
                        tvChange.setTextColor(decreasingColor)
                        tvChangePercent.setTextColor(decreasingColor)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -chartView.height.toFloat())
    }
}
