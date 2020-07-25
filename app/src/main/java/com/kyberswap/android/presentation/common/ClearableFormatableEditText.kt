package com.kyberswap.android.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.kyberswap.android.util.ext.decimalSeparator
import com.kyberswap.android.util.ext.getKSNumberFormat
import com.kyberswap.android.util.ext.ksFormat
import com.kyberswap.android.util.ext.thousandSeparator
import timber.log.Timber


/**
 * To clear icon can be changed via
 *
 * <pre>
 * android:drawable(Right|Left)="@drawable/custom_icon"
</pre> *
 */
class ClearableFormatableEditText : ClearFocusEditText, OnTouchListener, OnFocusChangeListener,
    TextWatcher {

    private var location: Location? = Location.RIGHT

    private var drawable: Drawable? = null
    private var listener: Listener? = null

    private var onTouchListener: OnTouchListener? = null
    private var focusChangeListener: OnFocusChangeListener? = null

    private val displayedDrawable: Drawable?
        get() = if (location != null) compoundDrawables[location!!.position] else null

    private var latestChangeStart: Int = 0
    private var latestInsertionSize: Int = 0

    private var beforeNumber: String? = null
    private var newCursorPosition: Int = 0
    private var formattedNumber: String? = null

    private val nf by lazy {
        getKSNumberFormat()
    }


    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (s.isBlank()) return
        latestChangeStart = start
        latestInsertionSize = after
        beforeNumber = s.toString()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (isFocused) {
            setClearIconVisible(!TextUtils.isEmpty(text))
        }
    }

    override fun afterTextChanged(text: Editable) {
        if (text.isBlank()) return
        if (text.endsWith(nf.decimalSeparator()) || text.endsWith("0")) return
        try {
            removeTextChangedListener(this)
            var inputtedNumber = text.toString()

            var isModify = false
            newCursorPosition = latestChangeStart
            // Deleting
            if (latestChangeStart >= 0 && latestChangeStart < inputtedNumber.length && latestInsertionSize == 0 && !beforeNumber.isNullOrBlank() && inputtedNumber.length != beforeNumber!!.length) {
                isModify = true
            }

            // Adding
            if (latestInsertionSize > 0 && latestChangeStart + 1 < inputtedNumber.length) {

                isModify = true
                newCursorPosition = latestChangeStart + 1
            }

            if (!beforeNumber.isNullOrBlank() && latestChangeStart < inputtedNumber.length &&
                latestChangeStart >= 0 &&
                latestInsertionSize == 0 && beforeNumber!![latestChangeStart] == nf.thousandSeparator()
            ) {
                inputtedNumber = inputtedNumber.removeRange(
                    if (latestChangeStart - 1 >= 0) latestChangeStart - 1 else 0,
                    latestChangeStart
                )
                newCursorPosition = latestChangeStart - 1

                isModify = true
            }
            val formattedValue = nf.ksFormat(inputtedNumber, false)
            setText(formattedValue)

            val count = inputtedNumber.substring(0, latestChangeStart)
                .count { it == nf.thousandSeparator() }
            val formatCount = formattedValue.substring(0, latestChangeStart)
                .count { it == nf.thousandSeparator() }

            newCursorPosition -= (count - formatCount)

//            Timber.e("latestInsertionSize: " + latestInsertionSize)
//            Timber.e("latestChangeStart: " + latestChangeStart)
//            Timber.e("inputtedNumber: " + inputtedNumber)
//
//            Timber.e("newCursorPosition: " + newCursorPosition)
//            Timber.e("formatedvalue: " + formattedValue)

            if (newCursorPosition < 0) newCursorPosition = 0
            if (isModify) {
                setSelection(newCursorPosition)
            } else {
                setSelection(formattedValue.length)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Timber.e("ex: %s", ex.localizedMessage)
        } finally {
            formattedNumber = null
            newCursorPosition = 0
            addTextChangedListener(this)
        }
    }

    enum class Location(internal val position: Int) {
        LEFT(0), RIGHT(2)
    }

    interface Listener {
        fun didClearText()
    }


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }


    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    /**
     * null disables the icon
     */
    fun setIconLocation(loc: Location) {
        this.location = loc
        initIcon()
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        this.onTouchListener = l
    }

    override fun setOnFocusChangeListener(f: OnFocusChangeListener?) {
        this.focusChangeListener = f
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (displayedDrawable != null) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val left =
                if (location == Location.LEFT) 0 else width - paddingRight - drawable!!.intrinsicWidth
            val right =
                if (location == Location.LEFT) paddingLeft + drawable!!.intrinsicWidth else width
            val tappedX = x in left..right && y >= 0 && y <= bottom - top
            if (tappedX) {
                if (event.action == MotionEvent.ACTION_UP) {
                    setText("")
                    beforeNumber = null
                    if (listener != null) {
                        listener!!.didClearText()
                    }
                }
                return true
            }
        }
        return if (onTouchListener != null) {
            onTouchListener!!.onTouch(v, event)
        } else false
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            setClearIconVisible(!TextUtils.isEmpty(text))
        } else {
            setClearIconVisible(false)
            if ((v is AppCompatEditText) &&
                v.text != null && v.text.toString().trim().isNotEmpty()
            ) {
                v.setText(nf.ksFormat(v.text.toString().trim()))
            }
        }
        if (focusChangeListener != null) {
            focusChangeListener!!.onFocusChange(v, hasFocus)
        }
    }


    override fun setCompoundDrawables(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        super.setCompoundDrawables(left, top, right, bottom)
        initIcon()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
        addTextChangedListener(this)
        initIcon()
        setClearIconVisible(false)
    }

    private fun initIcon() {
        drawable = null
        if (location != null) {
            drawable = compoundDrawables[location!!.position]
        }
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, android.R.drawable.presence_offline)
        }
        drawable!!.setBounds(0, 0, drawable!!.intrinsicWidth, drawable!!.intrinsicHeight)
        val min = paddingTop + drawable!!.intrinsicHeight + paddingBottom
        if (suggestedMinimumHeight < min) {
            minimumHeight = min
        }
    }

    protected fun setClearIconVisible(visible: Boolean) {
        val cd = compoundDrawables
        val displayed = displayedDrawable
        val wasVisible = displayed != null
        if (visible != wasVisible) {
            val x = if (visible) drawable else null
            super.setCompoundDrawables(
                if (location == Location.LEFT) x else cd[0],
                cd[1],
                if (location == Location.RIGHT) x else cd[2],
                cd[3]
            )
        }
    }
}