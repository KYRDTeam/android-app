package com.kyberswap.android.util.ext

import android.content.Context
import android.util.DisplayMetrics

fun Int.dpToPx(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    return this * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

fun Int.pxToDp(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    return this / (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}