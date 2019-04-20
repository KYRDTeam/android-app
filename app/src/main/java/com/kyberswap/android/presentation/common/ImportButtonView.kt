package com.kyberswap.android.presentation.common

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ImportButtonViewBinding


class ImportButtonView : LinearLayout {

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        var drawable: Drawable? = null
        var text: String? = null
        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.ImportButtonView, 0, 0
            )
            drawable = a.getDrawable(R.styleable.ImportButtonView_iconImage)

            text = a.getString(R.styleable.ImportButtonView_text)
            a.recycle()


        val inflater = LayoutInflater.from(context)
        val binding =
            DataBindingUtil.inflate<ImportButtonViewBinding>(
                inflater,
                R.layout.import_button_view,
                this,
                true
            )


        drawable?.apply {
            binding.icon.background = this





        text?.apply {
            binding.text.text = this


        binding.test = "abc"

    }
}

