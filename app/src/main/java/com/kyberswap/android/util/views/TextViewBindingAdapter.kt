package com.kyberswap.android.util.views

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.KycInfo
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan
import io.github.inflationx.calligraphy3.TypefaceUtils
import java.math.BigDecimal
import java.math.RoundingMode


object TextViewBindingAdapter {
    @BindingAdapter("app:resourceId")
    @JvmStatic
    fun setText(view: TextView, resourceId: Int) {
        view.text = view.context.resources.getString(resourceId)
    }

    @BindingAdapter("app:data", "app:font")
    @JvmStatic
    fun styleText(view: TextView, words: Array<String>, font: String) {

        val spannableString = SpannableString(view.text.toString())
        val typeface = TypefaceUtils.load(
            view.context.assets,
            font
        )

        words.forEach {
            val calligraphyTypeface = CalligraphyTypefaceSpan(
                typeface
            )

            spannableString.setSpan(
                calligraphyTypeface,
                spannableString.indexOf(it),
                spannableString.indexOf(it) + it.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


        view.setText(spannableString, TextView.BufferType.SPANNABLE)
    }


    @BindingAdapter("app:styleBold", "app:font")
    @JvmStatic
    fun styleRadioButtonText(view: RadioButton, bold: String?, font: String) {
        try {
            val spannableString = SpannableString(view.text.toString())
            val typeface = TypefaceUtils.load(
                view.context.assets,
                font
            )

            val calligraphyTypeface = CalligraphyTypefaceSpan(
                typeface
            )

            if (bold.isNullOrEmpty()) return
            spannableString.setSpan(
                calligraphyTypeface,
                spannableString.indexOf(bold),
                spannableString.indexOf(bold) + bold.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            view.setText(spannableString, TextView.BufferType.SPANNABLE)
 catch (ex: Exception) {
            ex.printStackTrace()

    }

    @BindingAdapter("app:ratePercentage", "app:hasSamePair", "app:warning")
    @JvmStatic
    fun setPercentage(view: TextView, percent: String?, samePair: Boolean?, warning: Boolean?) {

        if (samePair != null && samePair) {
            view.visibility = View.GONE
 else {
            val percentageRate = percent.toBigDecimalOrDefaultZero()
            if (percentageRate > (-0.1).toBigDecimal()) {
                view.visibility = View.GONE
                return
    

            if (warning != null && warning) {
                view.visibility = View.VISIBLE
                val drawable = when {
                    percentageRate > BigDecimal.ZERO -> R.drawable.ic_arrow_up
                    percentageRate < BigDecimal.ZERO -> R.drawable.ic_arrow_down
                    else -> 0
        

                val color = when {
                    percentageRate > BigDecimal.ZERO -> R.color.token_change24h_up
                    percentageRate < BigDecimal.ZERO -> R.color.token_change24h_down
                    else -> R.color.token_change24h_same
        

                view.setTextColor(ContextCompat.getColor(view.context, color))

                view.text =
                    String.format(
                        view.context.getString(R.string.percentage_format),
                        percentageRate.abs().toDouble()
                    )
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
     else {
                View.GONE
    

    }


    @BindingAdapter("app:ratePercentage")
    @JvmStatic
    fun ratePercentage(view: TextView, percent: String?) {

        val percentageRate = percent.toBigDecimalOrDefaultZero()
        val drawable = when {
            percentageRate > BigDecimal.ZERO -> R.drawable.ic_arrow_up
            percentageRate < BigDecimal.ZERO -> R.drawable.ic_arrow_down
            else -> 0

        val color = when {
            percentageRate > BigDecimal.ZERO -> R.color.token_change24h_up
            percentageRate < BigDecimal.ZERO -> R.color.token_change24h_down
            else -> R.color.token_change24h_same


        if (percentageRate == BigDecimal.ZERO) {
            view.visibility = View.GONE
 else {
            view.visibility = View.VISIBLE


        view.setTextColor(ContextCompat.getColor(view.context, color))

        view.text =
            String.format(
                view.context.getString(R.string.percentage_format),
                percentageRate.abs().toDouble()
            )
        view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }


    @BindingAdapter("app:rateValue")
    @JvmStatic
    fun rateWarning(view: TextView, percent: String?) {

        val percentageRate = percent.toBigDecimalOrDefaultZero()
        if (percentageRate > (-0.1).toBigDecimal()) {
            view.visibility = View.GONE
            return

        view.visibility = View.VISIBLE
    }

    @BindingAdapter("app:date")
    @JvmStatic
    fun transactionDate(view: TextView, time: Long) {
        view.text = DateTimeHelper.transactionDate(time)
    }


    @BindingAdapter("app:documentType")
    @JvmStatic
    fun documentType(view: TextView, documentType: String?) {
        if (KycInfo.TYPE_PASSPORT == documentType) {
            view.text = view.context.getString(R.string.passport)
 else if (KycInfo.TYPE_NATIONAL_ID == documentType) {
            view.text = view.context.getString(R.string.personal_id)

    }


    @BindingAdapter("app:isAbove", "app:alertPrice")
    @JvmStatic
    fun alertPrice(view: TextView, isAbove: Boolean?, alertPrice: BigDecimal?) {
        val color = if (true == isAbove) {
            R.color.rate_up_text_color
 else R.color.rate_down_text_color

        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder().append(if (true == isAbove) "≥ " else "≤ ")
            .append(alertPrice?.toDisplayNumber())
    }


    @BindingAdapter("app:isAbove", "app:percentChange")
    @JvmStatic
    fun percentChange(
        view: TextView,
        isAbove: Boolean?,
        percentChange: BigDecimal?
    ) {
        val color = if (true == isAbove) {
            R.color.rate_up_text_color
 else R.color.rate_down_text_color

        val drawable = when (isAbove) {
            true -> R.drawable.ic_arrow_up
            else -> R.drawable.ic_arrow_down

        view.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder()
            .append(percentChange?.setScale(2, RoundingMode.UP)?.toDisplayNumber()).append("%")
    }


    @BindingAdapter("app:isAbove", "app:percentChange", "app:isFilled")
    @JvmStatic
    fun fillAlertPercentChange(
        view: TextView,
        isAbove: Boolean?,
        percentChange: BigDecimal?,
        isFilled: Boolean
    ) {
        val color = if (true == isAbove) {
            R.color.rate_up_text_color
 else R.color.rate_down_text_color

        val drawable = when (isAbove) {
            true -> R.drawable.ic_arrow_up
            else -> R.drawable.ic_arrow_down

        if (isFilled) {
            val icon = view.context.getDrawable(drawable)
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)

            val filter = ColorMatrixColorFilter(matrix)

            icon?.colorFilter = filter

 else {
            view.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)

        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder()
            .append(percentChange?.setScale(2, RoundingMode.UP)?.toDisplayNumber()).append("%")
    }

    @BindingAdapter("app:rate")
    @JvmStatic
    fun setPercentage(view: TextView, rate: BigDecimal) {

        val color = when {
            rate >= BigDecimal.ZERO -> R.color.rate_up_text_color
            else -> R.color.rate_down_text_color


        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder().append(rate.abs().toDisplayNumber()).append(" %").toString()

    }

    @BindingAdapter("app:orderStatus")
    @JvmStatic
    fun orderStatus(view: TextView, orderStatus: String) {


        val background: Int
        val textColor: Int
        when (orderStatus) {
            Order.Status.OPEN.value -> {
                background = R.drawable.rounded_corner_order_open_background
                textColor = R.color.text_order_status_open
    
            Order.Status.FILLED.value -> {
                background = R.drawable.rounded_corner_order_filled_background
                textColor = R.color.text_order_status_filled

    
            Order.Status.IN_PROGRESS.value -> {
                background = R.drawable.rounded_corner_order_in_progress_background
                textColor = R.color.text_order_status_in_progress
    
            Order.Status.CANCELLED.value -> {
                background = R.drawable.rounded_corner_order_cancelled_background
                textColor = R.color.text_order_status_cancelled
    
            else -> {
                background = R.drawable.rounded_corner_order_invalidated_background
                textColor = R.color.text_order_status_invalidated
    

        view.text = orderStatus
        view.setTextColor(textColor)
        view.setBackgroundResource(background)

    }

    @BindingAdapter("app:kycStatus")
    @JvmStatic
    fun kycStatus(view: TextView, kycStatus: String?) {
        val background = when (kycStatus) {
            UserInfo.REJECT -> R.drawable.rounded_corner_status_rejected
            UserInfo.BLOCK -> R.drawable.rounded_corner_status_blocked
            UserInfo.PENDING -> R.drawable.rounded_corner_status_pending
            UserInfo.APPROVED -> R.drawable.rounded_corner_status_approved
            else -> R.drawable.rounded_corner_status_unverified


        val stringResource = when (kycStatus) {
            UserInfo.REJECT -> R.string.kyc_status_rejected
            UserInfo.BLOCK -> R.string.kyc_status_blocked
            UserInfo.PENDING -> R.string.kyc_status_pending
            UserInfo.APPROVED -> R.string.kyc_status_approved
            else -> R.string.kyc_status_unverified


        view.text = view.context.getString(stringResource)
        view.setBackgroundResource(background)

    }
}