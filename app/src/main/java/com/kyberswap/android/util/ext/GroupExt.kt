package com.kyberswap.android.util.ext

import android.view.View
import androidx.constraintlayout.widget.Group

fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
    referencedIds.forEach { viewId ->
        rootView.findViewById<View>(viewId)?.setOnClickListener(listener)
    }
}
