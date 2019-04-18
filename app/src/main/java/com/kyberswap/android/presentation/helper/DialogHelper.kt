package com.kyberswap.android.presentation.helper

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.kyberswap.android.R
import javax.inject.Inject

class DialogHelper @Inject constructor(private val activity: AppCompatActivity) {

    fun showConfirmation(okListener: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.title_confirmation))
            .setMessage(activity.getString(R.string.message_close_app_confirmation))
            .setPositiveButton(activity.getString(R.string.confirm)) { _, _ ->
                okListener.invoke()
    
            .create()
            .show()
    }
}
