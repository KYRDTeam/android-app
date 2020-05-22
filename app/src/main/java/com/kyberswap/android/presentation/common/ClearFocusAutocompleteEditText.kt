package com.kyberswap.android.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

open class ClearFocusAutocompleteEditText : AppCompatAutoCompleteTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus()
        }
        if (keyImeChangeListener != null) {
            keyImeChangeListener?.onKeyIme(keyCode, event)
        }

        return super.onKeyPreIme(keyCode, event)
    }

    private var keyImeChangeListener: KeyImeChange? = null

    fun setKeyImeChangeListener(listener: KeyImeChange) {
        keyImeChangeListener = listener
    }
}
