package com.kyberswap.android.util.ext

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.kyberswap.android.presentation.main.MainActivity


fun Fragment.showDrawer(boolean: Boolean) {
    if (activity != null && activity is MainActivity) {
        (activity as MainActivity).showDrawer(boolean)
    }
}

fun Fragment.openUrl(url: String?) {
    if (url.isNullOrEmpty()) return
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)

    val packageManager = activity?.packageManager
    if (packageManager != null && intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }

}