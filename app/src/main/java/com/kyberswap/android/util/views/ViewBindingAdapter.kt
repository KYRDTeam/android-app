package com.kyberswap.android.util.views

import android.view.View
import androidx.databinding.BindingAdapter

object ViewBindingAdapter {
    @BindingAdapter("app:selected")
    @JvmStatic
    fun isSelected(view: View, isSelected: Boolean) {
        view.isSelected = isSelected
    }
}