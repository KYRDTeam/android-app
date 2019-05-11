package com.kyberswap.android.util.ext

import android.view.View
import android.widget.EditText

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