package com.kyberswap.android.util.views

import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan
import io.github.inflationx.calligraphy3.TypefaceUtils
import java.math.BigDecimal

object TextViewBindingAdapter {
    @BindingAdapter("app:resourceId")
    @JvmStatic
    fun setText(view: TextView, resourceId: Int) {
        view.text = view.context.resources.getString(resourceId)
    }

    @BindingAdapter("app:data", "app:font")
    @JvmStatic
    fun styleText(view: TextView, words: Array<String>, font: String) {

        val spannableString = SpannableString(view.text.toString())
        val typeface = TypefaceUtils.load(
            view.context.assets,
            font
        )

        words.forEach {
            val calligraphyTypeface = CalligraphyTypefaceSpan(
                typeface
            )

            spannableString.setSpan(
                calligraphyTypeface,
                spannableString.indexOf(it),
                spannableString.indexOf(it) + it.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        view.setText(spannableString, TextView.BufferType.SPANNABLE)
    }


    @BindingAdapter("app:styleBold", "app:font")
    @JvmStatic
    fun styleRadioButtonText(view: RadioButton, bold: String?, font: String) {
        try {
            val spannableString = SpannableString(view.text.toString())
            val typeface = TypefaceUtils.load(
                view.context.assets,
                font
            )

            val calligraphyTypeface = CalligraphyTypefaceSpan(
                typeface
            )

            if (bold.isNullOrEmpty()) return
            spannableString.setSpan(
                calligraphyTypeface,
                spannableString.indexOf(bold),
                spannableString.indexOf(bold) + bold.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            view.setText(spannableString, TextView.BufferType.SPANNABLE)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @BindingAdapter("app:ratePercentage", "app:hasSamePair", "app:warning")
    @JvmStatic
    fun setPercentage(view: TextView, percent: String?, samePair: Boolean?, warning: Boolean?) {

        if (samePair != null && samePair) {
            view.visibility = View.GONE
        } else {
            val percentageRate = percent.toBigDecimalOrDefaultZero()
            if (percentageRate > (-0.1).toBigDecimal()) {
                view.visibility = View.GONE
                return
            }

            if (warning != null && warning) {
                view.visibility = View.VISIBLE
                val drawable = when {
                    percentageRate > BigDecimal.ZERO -> R.drawable.ic_arrow_up
                    percentageRate < BigDecimal.ZERO -> R.drawable.ic_arrow_down
                    else -> 0
                }

                val color = when {
                    percentageRate > BigDecimal.ZERO -> R.color.token_change24h_up
                    percentageRate < BigDecimal.ZERO -> R.color.token_change24h_down
                    else -> R.color.token_change24h_same
                }

                view.setTextColor(ContextCompat.getColor(view.context, color))

                view.text =
                    String.format(
                        view.context.getString(R.string.percentage_format),
                        percentageRate.abs().toDouble()
                    )
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
            } else {
                View.GONE
            }
        }
    }


    @BindingAdapter("app:rateValue")
    @JvmStatic
    fun rateWarning(view: TextView, percent: String?) {

        val percentageRate = percent.toBigDecimalOrDefaultZero()
        if (percentageRate > (-0.1).toBigDecimal()) {
            view.visibility = View.GONE
            return
        }
        view.visibility = View.VISIBLE
    }


    @BindingAdapter("app:isAbove", "app:alertPrice")
    @JvmStatic
    fun alert(view: TextView, isAbove: Boolean?, alertPrice: BigDecimal?) {
        val color = if (true == isAbove) {
            R.color.rate_up_text_color
        } else R.color.rate_down_text_color

        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder().append(if (true == isAbove) "≥ " else "≤ ")
            .append(alertPrice?.toDisplayNumber())
    }

    @BindingAdapter("app:rate")
    @JvmStatic
    fun setPercentage(view: TextView, rate: BigDecimal) {

        val color = when {
            rate >= BigDecimal.ZERO -> R.color.rate_up_text_color
            else -> R.color.rate_down_text_color
        }

        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder().append(rate.abs().toDisplayNumber()).append(" %").toString()

    }

    @BindingAdapter("app:orderStatus")
    @JvmStatic
    fun setPercentage(view: TextView, orderStatus: String) {
        val background = when (orderStatus) {
            Order.Status.OPEN.value -> R.drawable.rounded_corner_order_open_background
            Order.Status.FILLED.value -> R.drawable.rounded_corner_order_filled_background
            Order.Status.IN_PROGRESS.value -> R.drawable.rounded_corner_order_in_progress_background
            Order.Status.CANCELLED.value -> R.drawable.rounded_corner_order_cancelled_background
            else -> R.drawable.rounded_corner_order_invalidated_background
        }
        view.text = orderStatus
        view.setBackgroundResource(background)

    }
}