package com.kyberswap.android.util.ext

import android.view.View
import android.widget.EditText
import android.widget.TextView
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
