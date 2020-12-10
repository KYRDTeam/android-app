package com.kyberswap.android.presentation.common

import android.text.style.ClickableSpan
import android.view.View
import com.kyberswap.android.util.ext.openUrl

class ClickableSpan(
    var url: String
) : ClickableSpan() {
    override fun onClick(tv: View) {
        tv.context.openUrl(url)
    }

//    override fun updateDrawState(ds: TextPaint) { // override updateDrawState
//
////        ds.isUnderlineText = false // set to false to remove underline
//    }
}