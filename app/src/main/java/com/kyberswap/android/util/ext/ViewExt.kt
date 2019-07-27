package com.kyberswap.android.util.ext

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kyberswap.android.R
import java.math.BigDecimal

fun View.toggleSelection() {
    this.isSelected = !this.isSelected
}

fun View.enable(enable: Boolean) {
    this.isEnabled = enable
}

fun EditText.swap(other: EditText) {
    val temp = this.text
    this.text = other.text
    other.text = temp
}

fun EditText.textToDouble(): Double {
    if (this.text.isNullOrEmpty()) return 0.0
    return try {
        text.toString().toDouble()
    } catch (ex: NumberFormatException) {
        ex.printStackTrace()
        0.0
    }
}

fun EditText.setAmount(amount: String?) {
    if (amount.toDoubleOrDefaultZero() != 0.0) {
        setText(amount)
    } else {
        setText("")
    }
}

fun TextView.setAmount(amount: String?) {
    if (amount.toDoubleOrDefaultZero() != 0.0) {
        text = amount
    } else {
        text = ""
    }
}

fun TextView.getAmountOrDefaultValue(): String {
    return if (text.isNullOrEmpty() ||
        text.toString().toDouble() == 0.0
    ) context.getString(
        R.string.default_source_amount
    ) else text.toString()
}

fun TextView.setTextIfChange(text: CharSequence) {
    if (text != this.text) {
        this.text = text
    }
}

fun TextView.toBigDecimalOrDefaultZero(): BigDecimal {
    return text.toString().toBigDecimalOrDefaultZero()
}

fun TextView.colorRate(rate: BigDecimal) {
    try {


        val color: Int
        val ratePercent =
            String.format(context.getString(R.string.rate_percent), rate.abs().toString())
        val text: String
        when {
            rate > BigDecimal.ZERO -> {

                color = ContextCompat.getColor(context, R.color.token_change24h_up)
                text = String.format(
                    context.getString(R.string.limit_order_rate_higher_market),
                    ratePercent
                )
            }
            rate < BigDecimal.ZERO -> {
                color = ContextCompat.getColor(context, R.color.token_change24h_down)
                text = String.format(
                    context.getString(R.string.limit_order_rate_lower_market),
                    ratePercent
                )
            }
            else -> {
                color = ContextCompat.getColor(context, R.color.token_change24h_same)
                text = ""
            }
        }
        if (text.isEmpty()) {
            setText("")
            return
        }
        val spannableString = SpannableString(text)

        spannableString.setSpan(
            ForegroundColorSpan(color),
            spannableString.indexOf(ratePercent),
            spannableString.indexOf(ratePercent) + ratePercent.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        setText(spannableString, TextView.BufferType.SPANNABLE)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun TextView.colorize(colorString: String, color: Int) {
    val spannableString = SpannableString(text)
    spannableString.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, color)),
        spannableString.indexOf(colorString),
        spannableString.indexOf(colorString) + colorString.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun TextView.underline(underline: String) {
    try {

        val spannableString = SpannableString(text)

        spannableString.setSpan(
            UnderlineSpan(),
            spannableString.indexOf(underline),
            spannableString.indexOf(underline) + underline.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        setText(spannableString, TextView.BufferType.SPANNABLE)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}
