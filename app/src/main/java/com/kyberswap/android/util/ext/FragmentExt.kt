package com.kyberswap.android.util.ext

import androidx.fragment.app.Fragment
import com.kyberswap.android.presentation.main.MainActivity

fun Fragment.showDrawer(boolean: Boolean) {
    if (activity != null && activity is MainActivity) {
        (activity as MainActivity).showDrawer(boolean)
    }
}