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