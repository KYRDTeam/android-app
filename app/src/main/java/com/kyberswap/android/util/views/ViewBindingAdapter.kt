package com.kyberswap.android.util.views

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kyberswap.android.R

object ViewBindingAdapter {
    @BindingAdapter("app:selected")
    @JvmStatic
    fun isSelected(view: View, isSelected: Boolean) {
        view.isSelected = isSelected
        val size = if (isSelected) {
            R.dimen.text_size_title
 else {
            R.dimen.text_size_content

        if (view is TextView) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.context.resources.getDimension(size))

    }

    @BindingAdapter("app:isActive", "app:isReward")
    @JvmStatic
    fun leaderBoardItem(view: View, isActive: Boolean?, isReward: Boolean?) {
        val drawable: Int
        val color: Int
        val circleDrawable: Int
        if (isReward == true) {
            if (isActive == true) {
                drawable = R.drawable.alert_background_reward
                color = R.color.text_alert_reward_color
                circleDrawable = R.drawable.circle_alert_rank_active_bg

     else {
                drawable = R.drawable.alert_background_normal
                color = R.color.text_alert_normal_color
                circleDrawable = R.drawable.circle_alert_rank_reward_bg
    

 else {
            if (isActive == true) {
                drawable = R.drawable.alert_background_active
                color = R.color.text_alert_active_color
                circleDrawable = R.drawable.circle_alert_rank_active_bg
     else {
                drawable = R.drawable.alert_background_normal
                color = R.color.text_alert_normal_color
                circleDrawable = R.drawable.circle_alert_rank_bg
    



        if (view is TextView) {
            view.setTextColor(ContextCompat.getColor(view.context, color))
            view.setBackgroundResource(circleDrawable)
 else {
            view.setBackgroundResource(drawable)

    }
}