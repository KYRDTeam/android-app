package com.kyberswap.android.util.ext

import android.view.View
import androidx.constraintlayout.widget.Group

fun Group.setAllOnClickListener(listener: (view: View) -> Unit = {}) {
    referencedIds.forEach { viewId ->
        val viewById = rootView.findViewById<View>(viewId)
        viewById?.setOnClickListener(listener)
    }
}
