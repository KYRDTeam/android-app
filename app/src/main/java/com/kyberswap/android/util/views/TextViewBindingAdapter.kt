package com.kyberswap.android.util.views

import android.databinding.BindingAdapter
import android.widget.TextView

object TextViewBindingAdapter {
    @BindingAdapter("app:resourceId")
    @JvmStatic
    fun setText(view: TextView, resourceId: Int) {
        view.text = view.context.resources.getString(resourceId)
    }

}