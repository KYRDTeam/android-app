package com.kyberswap.android.presentation.listener

import android.support.v4.widget.DrawerLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView

inline fun EditText.addTextChangeListener(func: AddTextChangeListener.() -> Unit) {
    val listener = AddTextChangeListener()
    listener.func()
    addTextChangedListener(listener)
}

inline fun TextView.addTextChangeListener(func: AddTextChangeListener.() -> Unit) {
    val listener = AddTextChangeListener()
    listener.func()
    addTextChangedListener(listener)
}

open class AddTextChangeListener : TextWatcher {
    private var _afterTextChanged: ((s: Editable) -> Unit)? = null
    private var _beforeTextChanged: ((s: CharSequence, start: Int, count: Int, after: Int) -> Unit)? =
        null
    private var _onTextChanged: ((s: CharSequence, start: Int, before: Int, count: Int) -> Unit)? =
        null

    override fun afterTextChanged(s: Editable) {
        _afterTextChanged?.invoke(s)
    }

    fun afterTextChanged(func: (s: Editable?) -> Unit) {
        _afterTextChanged = func
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        _beforeTextChanged?.invoke(s, start, count, after)
    }

    fun beforeTextChanged(func: (s: CharSequence, start: Int, count: Int, after: Int) -> Unit) {
        _beforeTextChanged = func
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        _onTextChanged?.invoke(s, start, before, count)
    }

    fun onTextChanged(func: (s: CharSequence, start: Int, before: Int, count: Int) -> Unit) {
        _onTextChanged = func
    }
}

inline fun DrawerLayout.addDrawerListener(func: DrawerListener.() -> Unit) {
    val listener = DrawerListener()
    listener.func()
    addDrawerListener(listener)
}

class DrawerListener : DrawerLayout.DrawerListener {
    private var _onDrawerStateChanged: ((newState: Int) -> Unit)? = null
    private var _onDrawerSlide: ((drawerView: View, slideOffset: Float) -> Unit)? = null
    private var _onDrawerClosed: ((drawerView: View) -> Unit)? = null
    private var _onDrawerOpened: ((drawerView: View) -> Unit)? = null

    override fun onDrawerStateChanged(newState: Int) {
        _onDrawerStateChanged?.invoke(newState)
    }

    fun onDrawerStateChanged(func: (newState: Int) -> Unit) {
        _onDrawerStateChanged = func
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        _onDrawerSlide?.invoke(drawerView, slideOffset)
    }

    fun onDrawerSlide(func: (drawerView: View, slideOffset: Float) -> Unit) {
        _onDrawerSlide = func
    }

    override fun onDrawerClosed(drawerView: View) {
        _onDrawerClosed?.invoke(drawerView)
    }

    fun onDrawerClosed(func: (drawerView: View) -> Unit) {
        _onDrawerClosed = func
    }

    override fun onDrawerOpened(drawerView: View) {
        _onDrawerOpened?.invoke(drawerView)
    }

    fun onDrawerOpened(func: (drawerView: View) -> Unit) {
        _onDrawerOpened = func
    }
}