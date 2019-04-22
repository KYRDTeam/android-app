package com.kyberswap.android.presentation.helper

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.kyberswap.android.R
import javax.inject.Inject

class DialogHelper @Inject constructor(private val activity: AppCompatActivity) {

    fun showConfirmation(positiveListener: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.title_confirmation))
            .setMessage(activity.getString(R.string.message_close_app_confirmation))
            .setPositiveButton(activity.getString(R.string.confirm)) { _, _ ->
                positiveListener.invoke()
            }
            .create()
            .show()
    }

    private fun showWrongBackup(positiveListener: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.title_wrong_backup))
            .setMessage(activity.getString(R.string.message_wrong_backup))
            .setPositiveButton(activity.getString(R.string.try_again)) { _, _ ->
                positiveListener.invoke()
            }
            .create()
            .show()
    }

    fun showWrongBackup(
        numberOfTry: Int,
        positiveListener: () -> Unit = {},
        negativeListener: () -> Unit = {}
    ) {
        if (numberOfTry > 0) {
            showWrongBackupAgain(positiveListener, negativeListener)
        } else {
            showWrongBackup(positiveListener)
        }
    }

    private fun showWrongBackupAgain(
        positiveListener: () -> Unit,
        negativeListener: () -> Unit = {}
    ) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.title_wrong_backup))
            .setMessage(activity.getString(R.string.message_wrong_backup))
            .setNegativeButton(activity.getString(R.string.retry)) { _, _ ->
                negativeListener.invoke()
            }
            .setPositiveButton(activity.getString(R.string.try_again)) { _, _ ->
                positiveListener.invoke()
            }
            .create()
            .show()
    }

}
