package com.kyberswap.android.util.ext

import android.view.View

fun View.toggleSelection() {
    this.isSelected = !this.isSelected
}