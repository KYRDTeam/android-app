package com.kyberswap.android.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var enabledSwipe: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.enabledSwipe) {
            super.onTouchEvent(event)
        } else false

    }


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.enabledSwipe) {
            super.onInterceptTouchEvent(event)
        } else false

    }

    fun setPagingEnabled(enabled: Boolean) {
        this.enabledSwipe = enabled
    }
}