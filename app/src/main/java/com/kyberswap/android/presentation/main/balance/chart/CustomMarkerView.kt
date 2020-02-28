package com.kyberswap.android.presentation.main.balance.chart

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.kyberswap.android.R
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber


class CustomMarkerView(context: Context, private val xAxisValueFormatter: ValueFormatter) :
    MarkerView(context, R.layout.custom_marker_view) {
    private val tvPrice: TextView = findViewById(R.id.tvPrice)
    private val tvTime: TextView = findViewById(R.id.tvTime)

    private val offset by lazy {
        resources.getDimension(R.dimen.marker_height)
    }

    private val makerTime by lazy {
        resources.getString(R.string.maker_time)
    }

    private val makerPrice by lazy {
        resources.getString(R.string.maker_price)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            e?.let {
                tvTime.text = String.format(xAxisValueFormatter.getFormattedValue(e.x), makerTime)
                tvPrice.text = String.format(
                    makerPrice,
                    e.y.toString().toBigDecimalOrDefaultZero().toDisplayNumber()
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height + offset))
    }
}
