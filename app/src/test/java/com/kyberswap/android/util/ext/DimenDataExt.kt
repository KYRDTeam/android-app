package com.kyberswap.android.util.ext

import android.content.Context
import android.util.DisplayMetrics

fun Int.dpToPx(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    return Math.round(this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun Int.pxToDp(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    return Math.round(this / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}