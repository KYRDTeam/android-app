package com.kyberswap.android.util.ext

import android.app.Activity
import android.view.View

fun Activity.setImmersiveFullScreen() {
    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
}

fun Activity.setFullScreen() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

fun Activity.exitFullScreen(color: Int = 0) {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    if (color != 0) {
        window.statusBarColor = color
    }
}

fun Activity.getStatusBar(): Int {
    return window.statusBarColor
}