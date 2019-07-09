package com.andrognito.pinlockview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PinLockView : RecyclerView {

    private var mPin = ""
    private var mPinLength: Int = 0
    private var mHorizontalSpacing: Int = 0
    private var mVerticalSpacing: Int = 0
    private var mShowDeleteButton: Boolean = false

    private var mIndicatorDots: IndicatorDots? = null
    private var mAdapter: PinLockAdapter? = null
    private var mPinLockListener: PinLockListener? = null
    private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
    private var mCustomKeySet: IntArray? = null

    private val mOnNumberClickListener = object : PinLockAdapter.OnNumberClickListener {
        override fun onNumberClicked(keyValue: Int) {
            if (mPin.length < pinLength) {
                mPin += keyValue.toString()

                if (isIndicatorDotsAttached) {
                    mIndicatorDots!!.updateDot(mPin.length)
        

                if (mPin.length == 1) {
                    mAdapter!!.pinLength = mPin.length
                    mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
        

                if (mPinLockListener != null) {
                    if (mPin.length == mPinLength) {
                        mPinLockListener!!.onComplete(mPin)
             else {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
            
        
     else {
                if (!isShowDeleteButton) {
                    resetPinLockView()
                    mPin += keyValue.toString()

                    if (isIndicatorDotsAttached) {
                        mIndicatorDots!!.updateDot(mPin.length)
            

                    if (mPinLockListener != null) {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
            

         else {
                    if (mPinLockListener != null) {
                        mPinLockListener!!.onComplete(mPin)
            
        
    


    }

    private val mOnDeleteClickListener = object : PinLockAdapter.OnDeleteClickListener {
        override fun onDeleteClicked() {
            if (mPin.length > 0) {
                mPin = mPin.substring(0, mPin.length - 1)

                if (isIndicatorDotsAttached) {
                    mIndicatorDots!!.updateDot(mPin.length)
        

                if (mPin.length == 0) {
                    mAdapter!!.pinLength = mPin.length
                    mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
        

                if (mPinLockListener != null) {
                    if (mPin.length == 0) {
                        mPinLockListener!!.onEmpty()
                        clearInternalPin()
             else {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
            
        
     else {
                if (mPinLockListener != null) {
                    mPinLockListener!!.onEmpty()
        
    


        override fun onDeleteLongClicked() {
            resetPinLockView()
            if (mPinLockListener != null) {
                mPinLockListener!!.onEmpty()
    

    }

    /**
     * Get the length of the current pin length
     *
     * @return the length of the pin
     */
    /**
     * Sets the pin length dynamically
     *
     * @param pinLength the pin length
     */
    var pinLength: Int
        get() = mPinLength
        set(pinLength) {
            this.mPinLength = pinLength

            if (isIndicatorDotsAttached) {
                mIndicatorDots!!.pinLength = pinLength
    



    /**
     * Is the delete button shown
     *
     * @return returns true if shown, false otherwise
     */
    /**
     * Dynamically set if the delete button should be shown
     *
     * @param showDeleteButton true if the delete button should be shown, false otherwise
     */
    var isShowDeleteButton: Boolean
        get() = mShowDeleteButton
        set(showDeleteButton) {
            this.mShowDeleteButton = showDeleteButton
            mCustomizationOptionsBundle!!.isShowDeleteButton = showDeleteButton
            mAdapter!!.notifyDataSetChanged()



    var customKeySet: IntArray?
        get() = mCustomKeySet
        set(customKeySet) {
            this.mCustomKeySet = customKeySet

            if (mAdapter != null) {
                mAdapter!!.keyValues = customKeySet
    


    /**
     * Returns true if [IndicatorDots] are attached to [PinLockView]
     *
     * @return true if attached, false otherwise
     */
    val isIndicatorDotsAttached: Boolean
        get() = mIndicatorDots != null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attributeSet: AttributeSet?, defStyle: Int) {

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PinLockView)

        try {
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mHorizontalSpacing = typedArray.getDimension(
                R.styleable.PinLockView_keypadHorizontalSpacing,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_horizontal_spacing)
            ).toInt()
            mVerticalSpacing = typedArray.getDimension(
                R.styleable.PinLockView_keypadVerticalSpacing,
                ResourceUtils.getDimensionInPx(context, R.dimen.default_vertical_spacing)
            ).toInt()

            mShowDeleteButton =
                typedArray.getBoolean(R.styleable.PinLockView_keypadShowDeleteButton, true)

 finally {
            typedArray.recycle()


        mCustomizationOptionsBundle = CustomizationOptionsBundle()
        mCustomizationOptionsBundle!!.isShowDeleteButton = mShowDeleteButton

        initView()
    }

    private fun initView() {
        layoutManager = LTRGridLayoutManager(context, 3)

        mAdapter = PinLockAdapter()
        mAdapter?.onItemClickListener = mOnNumberClickListener
        mAdapter?.onDeleteClickListener = mOnDeleteClickListener
        mAdapter?.customizationOptions = mCustomizationOptionsBundle
        adapter = mAdapter

        addItemDecoration(ItemSpaceDecoration(mHorizontalSpacing, mVerticalSpacing, 3, false))
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    /**
     * Sets a [PinLockListener] to the to listen to pin update events
     *
     * @param pinLockListener the listener
     */
    fun setPinLockListener(pinLockListener: PinLockListener) {
        this.mPinLockListener = pinLockListener
    }

    fun enableLayoutShuffling() {
        this.mCustomKeySet = ShuffleArrayUtils.shuffle(DEFAULT_KEY_SET)

        if (mAdapter != null) {
            mAdapter!!.keyValues = mCustomKeySet

    }

    private fun clearInternalPin() {
        mPin = ""
    }

    /**
     * Resets the [PinLockView], clearing the entered pin
     * and resetting the [IndicatorDots] if attached
     */
    fun resetPinLockView() {

        clearInternalPin()

        mAdapter?.pinLength = mPin.length
        mAdapter?.notifyItemChanged(mAdapter!!.itemCount - 1)

        if (mIndicatorDots != null) {
            mIndicatorDots!!.updateDot(mPin.length)

    }

    /**
     * Attaches [IndicatorDots] to [PinLockView]
     *
     * @param mIndicatorDots the view to attach
     */
    fun attachIndicatorDots(mIndicatorDots: IndicatorDots) {
        this.mIndicatorDots = mIndicatorDots
    }

    companion object {

        private const val DEFAULT_PIN_LENGTH = 4
        private val DEFAULT_KEY_SET = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
    }
}
