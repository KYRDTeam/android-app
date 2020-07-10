package com.kyberswap.android.util.views

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
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
            if (spannableString.indexOf(it) >= 0 && (spannableString.indexOf(it) + it.length <= spannableString.length)) {
                val calligraphyTypeface = CalligraphyTypefaceSpan(
                    typeface
                )
                spannableString.setSpan(
                    calligraphyTypeface,
                    spannableString.indexOf(it),
                    spannableString.indexOf(it) + it.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        view.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    @BindingAdapter("app:time", "app:font")
    @JvmStatic
    fun styleTime(view: TextView, time: String?, font: String) {
        if (time.isNullOrEmpty()) return
        try {
            val deta = (System.currentTimeMillis() - DateTimeHelper.toLong(time)) / 1000L
            val minus = deta / 60.0
            val hours = minus / 60
            val days = hours / 24
            val context = view.context
            val displayText = if (deta / 60.0 < 2) {
                context.getString(R.string.just_now)
            } else if (minus >= 2 && minus < 60) {
                String.format(context.getString(R.string.mins_ago), minus.toInt().toString())
            } else if (hours < 2.0) {
                context.getString(R.string.one_hour_ago)
            } else if (hours >= 2 && hours < 24) {
                String.format(context.getString(R.string.hours_ago), hours.toInt().toString())
            } else if (days < 2.0) {
                context.getString(R.string.one_day_ago)
            } else if (days <= 3.0) {
                String.format(context.getString(R.string.days_ago), days.toInt().toString())
            } else {
                DateTimeHelper.displayAlertDate(time)
            }

            val spannableString = SpannableString(view.text.toString() + " " + displayText)
            val typeface = TypefaceUtils.load(
                view.context.assets,
                font
            )

            spannableString.setSpan(
                RelativeSizeSpan(0.75f),
                spannableString.indexOf(displayText),
                spannableString.indexOf(displayText) + displayText.length,
                0
            )


            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        view.context,
                        R.color.color_trigger_alert_price
                    )
                ),
                spannableString.indexOf(displayText),
                spannableString.indexOf(displayText) + displayText.length,
                0
            ) //

            val calligraphyTypeface = CalligraphyTypefaceSpan(
                typeface
            )

            spannableString.setSpan(
                calligraphyTypeface,
                spannableString.indexOf(displayText),
                spannableString.indexOf(displayText) + displayText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            view.setText(spannableString, TextView.BufferType.SPANNABLE)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @BindingAdapter("app:styleBold", "app:font", "app:data")
    @JvmStatic
    fun styleRadioButtonText(view: RadioButton, bold: String?, font: String?, data: String?) {
        try {
            if (font.isNullOrBlank() || data.isNullOrBlank() || bold.isNullOrBlank()) return
            val spannableString = SpannableStringBuilder().append(data)
            val typeface = TypefaceUtils.load(
                view.context.assets,
                font
            )
            val calligraphyTypeface = CalligraphyTypefaceSpan(
                typeface
            )
            if (bold.isNullOrEmpty()) return
            if (spannableString.indexOf(bold) < 0 ||
                (spannableString.indexOf(bold) + bold.length) >= spannableString.length
            ) return


            spannableString.setSpan(
                calligraphyTypeface,
                spannableString.indexOf(bold),
                spannableString.indexOf(bold) + bold.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            view.text = spannableString
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @BindingAdapter("app:isPending")
    @JvmStatic
    fun transactionStatus(view: TextView, isPending: Boolean?) {

        val background: Int
        val textColor: Int
        when (isPending) {
            true -> {
                background = R.drawable.rounded_corner_pending_transaction_background
                textColor = R.color.pending_transaction_text
            }
            else -> {
                background = R.drawable.rounded_corner_mined_transaction_background
                textColor = R.color.mined_transaction_text
            }
        }
        view.setTextColor(ContextCompat.getColor(view.context, textColor))
        view.setBackgroundResource(background)
    }

    @BindingAdapter("app:ratePercentage", "app:hasSamePair", "app:warning")
    @JvmStatic
    fun setPercentage(view: TextView, percent: String?, samePair: Boolean?, warning: Boolean?) {

        if (samePair != null && samePair) {
            view.visibility = View.GONE
        } else {
            val percentageRate = percent.toBigDecimalOrDefaultZero()
            if (percentageRate >= (-5).toBigDecimal()) {
                view.visibility = View.GONE
                return
            }

            if (warning != null && warning) {
                view.visibility = View.VISIBLE
                val drawable = when {
                    percentageRate > BigDecimal.ZERO -> R.drawable.ic_arrow_up
                    percentageRate < BigDecimal.ZERO -> R.drawable.ic_arrow_down
                    else -> 0
                }

                val color = when {
                    percentageRate > BigDecimal.ZERO -> R.color.token_change24h_up
                    percentageRate < BigDecimal.ZERO -> R.color.token_change24h_down
                    else -> R.color.token_change24h_same
                }

                view.setTextColor(ContextCompat.getColor(view.context, color))

                view.text =
                    String.format(
                        view.context.getString(R.string.percentage_format),
                        percentageRate.abs().toDouble()
                    )
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
            } else {
                View.GONE
            }
        }
    }

    @BindingAdapter("app:ratePercentage")
    @JvmStatic
    fun ratePercentage(view: TextView, percent: String?) {

        val percentageRate = percent.toBigDecimalOrDefaultZero()
        val drawable = when {
            percentageRate > BigDecimal.ZERO -> R.drawable.ic_arrow_up
            percentageRate < BigDecimal.ZERO -> R.drawable.ic_arrow_down
            else -> 0
        }
        val color = when {
            percentageRate > BigDecimal.ZERO -> R.color.token_change24h_up
            percentageRate < BigDecimal.ZERO -> R.color.token_change24h_down
            else -> R.color.token_change24h_same
        }

        if (percentageRate == BigDecimal.ZERO) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }

        view.setTextColor(ContextCompat.getColor(view.context, color))

        view.text =
            String.format(
                view.context.getString(R.string.percentage_format),
                percentageRate.abs().toDouble()
            )
        view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }

    @BindingAdapter("app:market24h")
    @JvmStatic
    fun market24h(view: TextView, percent: String?) {

        val percentageRate = percent.toBigDecimalOrDefaultZero()
        val drawable = when {
            percentageRate > BigDecimal.ZERO -> R.drawable.ic_arrow_up
            percentageRate < BigDecimal.ZERO -> R.drawable.ic_arrow_down
            else -> 0
        }
        val color = when {
            percentageRate > BigDecimal.ZERO -> R.color.token_change24h_up
            percentageRate < BigDecimal.ZERO -> R.color.token_change24h_down
            else -> R.color.token_change24h_same
        }


        view.setTextColor(ContextCompat.getColor(view.context, color))

        if (percentageRate == BigDecimal.ZERO) {
            view.text = "--"
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        } else {
            view.text =
                String.format(
                    view.context.getString(R.string.percentage_format),
                    percentageRate.abs().toDouble()
                )
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
        }
    }

    @BindingAdapter("app:rateValue")
    @JvmStatic
    fun rateWarning(view: TextView, percent: String?) {

        val percentageRate = percent.toBigDecimalOrDefaultZero()
        if (percentageRate >= (-5).toBigDecimal()) {
            view.visibility = View.GONE
            return
        }
        view.visibility = View.VISIBLE
    }

    @BindingAdapter("app:date")
    @JvmStatic
    fun transactionDate(view: TextView, time: Long) {
        view.text = DateTimeHelper.transactionDate(time)
    }

    @BindingAdapter("app:time")
    @JvmStatic
    fun notificationTime(view: TextView, time: String) {
        val deta = (System.currentTimeMillis() - DateTimeHelper.toLong(time)) / 1000L
        val minus = deta / 60.0
        val hours = minus / 60
        val days = hours / 24
        val context = view.context
        val displayText = if (deta / 60.0 < 2) {
            context.getString(R.string.just_now)
        } else if (minus >= 2 && minus < 60) {
            String.format(context.getString(R.string.mins_ago), minus.toInt().toString())
        } else if (hours < 2.0) {
            context.getString(R.string.one_hour_ago)
        } else if (hours >= 2 && hours < 24) {
            String.format(context.getString(R.string.hours_ago), hours.toInt().toString())
        } else if (days < 2.0) {
            context.getString(R.string.one_day_ago)
        } else if (days <= 3.0) {
            String.format(context.getString(R.string.days_ago), days.toInt().toString())
        } else {
            DateTimeHelper.displayAlertDate(time)
        }

        view.text = displayText
    }

    @BindingAdapter("app:documentType")
    @JvmStatic
    fun documentType(view: TextView, documentType: String?) {
        if (KycInfo.TYPE_PASSPORT == documentType) {
            view.text = view.context.getString(R.string.passport)
        } else if (KycInfo.TYPE_NATIONAL_ID == documentType) {
            view.text = view.context.getString(R.string.personal_id)
        }
    }

    @BindingAdapter("app:isAbove", "app:alertPrice")
    @JvmStatic
    fun alertPrice(view: TextView, isAbove: Boolean?, alertPrice: BigDecimal?) {
        val color = if (true == isAbove) {
            R.color.rate_up_text_color
        } else R.color.rate_down_text_color

        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder().append(if (true == isAbove) "≥ " else "≤ ")
            .append(alertPrice?.toDisplayNumber())
    }

    @BindingAdapter("app:isAbove", "app:alertPrice", "app:isFilled")
    @JvmStatic
    fun filledAlertPrice(
        view: TextView,
        isAbove: Boolean?,
        alertPrice: BigDecimal?,
        isFilled: Boolean?
    ) {
        val color = when {
            isFilled == true -> R.color.color_trigger_alert_price
            true == isAbove -> {
                R.color.rate_up_text_color
            }
            else -> R.color.rate_down_text_color
        }

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
        } else R.color.rate_down_text_color

        val drawable = when (isAbove) {
            true -> R.drawable.ic_arrow_up
            else -> R.drawable.ic_arrow_down
        }
        view.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder()
            .append(percentChange?.setScale(2, RoundingMode.UP)?.toDisplayNumber()).append("%")
    }

    @BindingAdapter("app:isAbove", "app:percentChange", "app:isFilled")
    @JvmStatic
    fun filledAlertPercentChange(
        view: TextView,
        isAbove: Boolean?,
        percentChange: BigDecimal?,
        isFilled: Boolean?
    ) {
        val color = when {
            isFilled == true -> R.color.color_trigger_alert_price
            true == isAbove -> {
                R.color.rate_up_text_color
            }
            else -> R.color.rate_down_text_color
        }

        val drawable = when (isAbove) {
            true -> R.drawable.ic_arrow_up
            else -> R.drawable.ic_arrow_down
        }
        if (isFilled == true) {
            val icon = view.context.getDrawable(drawable)
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)

            val filter = ColorMatrixColorFilter(matrix)

            icon?.colorFilter = filter
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
        }
        view.setTextColor(ContextCompat.getColor(view.context, color))
        view.text = StringBuilder()
            .append(percentChange?.setScale(2, RoundingMode.UP)?.toDisplayNumber()).append("%")
    }

    @BindingAdapter("app:rate")
    @JvmStatic
    fun setPercentage(view: TextView, rate: BigDecimal?) {
        if (rate == null) return

        val color = when {
            rate >= BigDecimal.ZERO -> R.color.rate_up_text_color
            else -> R.color.rate_down_text_color
        }

        view.setTextColor(ContextCompat.getColor(view.context, color))

        if (rate.abs() == BigDecimal.ZERO) {
            view.text = ""
        } else {
            view.text =
                StringBuilder().append(String.format("%.2f", rate.abs())).append(" %").toString()
        }
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
            }
            Order.Status.FILLED.value -> {
                background = R.drawable.rounded_corner_order_filled_background
                textColor = R.color.text_order_status_filled
            }
            Order.Status.IN_PROGRESS.value -> {
                background = R.drawable.rounded_corner_order_in_progress_background
                textColor = R.color.text_order_status_in_progress
            }
            Order.Status.CANCELLED.value -> {
                background = R.drawable.rounded_corner_order_cancelled_background
                textColor = R.color.text_order_status_cancelled
            }
            else -> {
                background = R.drawable.rounded_corner_order_invalidated_background
                textColor = R.color.text_order_status_invalidated
            }
        }
        view.text = orderStatus
        view.setTextColor(ContextCompat.getColor(view.context, textColor))
        view.setBackgroundResource(background)
    }

    @BindingAdapter("app:kycStatus")
    @JvmStatic
    fun kycStatus(view: TextView, kycStatus: String?) {
        val background = when (kycStatus) {
            UserInfo.REJECT -> R.drawable.rounded_corner_status_rejected
            UserInfo.BLOCK -> R.drawable.rounded_corner_status_blocked
            UserInfo.BLOCKED -> R.drawable.rounded_corner_status_blocked
            UserInfo.PENDING -> R.drawable.rounded_corner_status_pending
            UserInfo.APPROVED -> R.drawable.rounded_corner_status_approved
            else -> R.drawable.rounded_corner_status_unverified
        }

        val stringResource = when (kycStatus) {
            UserInfo.REJECT -> R.string.kyc_status_rejected
            UserInfo.BLOCK -> R.string.kyc_status_blocked
            UserInfo.BLOCKED -> R.string.kyc_status_blocked
            UserInfo.PENDING -> R.string.kyc_status_pending
            UserInfo.APPROVED -> R.string.kyc_status_approved
            else -> R.string.kyc_status_unverified
        }

        view.text = view.context.getString(stringResource)
        view.setBackgroundResource(background)
    }
}