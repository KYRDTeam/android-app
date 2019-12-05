package com.kyberswap.android.presentation.base

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.presentation.common.AlertActivity
import com.kyberswap.android.presentation.common.AlertWithoutIconActivity
import com.kyberswap.android.presentation.main.MainActivity
import dagger.android.support.DaggerFragment

abstract class BaseFragment : DaggerFragment() {
    var dialog: ProgressDialog? = null
    private val handler by lazy {
        Handler()
    }

    var alertListener: () -> Unit = {}

    private fun initLoadingDialog() {
        dialog = ProgressDialog(this.context)
            .apply {
                setMessage(getString(R.string.message_loading))
                setCanceledOnTouchOutside(true)
                setCancelable(true)
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLoadingDialog()
    }

    open fun showProgress(showProgress: Boolean) {
        if (showProgress) dialog?.show() else dialog?.dismiss()
    }

    fun showAlert(message: String, listener: () -> Unit = {}) {
        if (context != null) {
            this.alertListener = listener
            val intent = AlertActivity.newIntent(context!!, message)
            startActivityForResult(intent, SHOW_ALERT)
        }
    }

    fun showError(
        message: String,
        listener: () -> Unit = {},
        time: Int = DEFAULT_ALERT_TIME_SECONDS
    ) {
        showAlertWithoutIcon(
            getString(R.string.title_error),
            message,
            time,
            listener
        )
    }

    fun showErrorIcon(message: String, listener: () -> Unit = {}) {
        showAlert(message, R.drawable.ic_info_error, listener)
    }

    fun showErrorMessage(message: String) {
        showAlertWithoutIcon(title = getString(R.string.title_error), message = message)
    }

    fun showAlertWithoutIcon(
        title: String? = null,
        message: String,
        timeInSecond: Int = 3,
        listener: () -> Unit = {}
    ) {
        if (context != null) {
            this.alertListener = listener
            val intent = AlertWithoutIconActivity.newIntent(context!!, title, message, timeInSecond)
            startActivityForResult(intent, SHOW_ALERT)
        }
    }

    fun showAlert(message: String, resourceIcon: Int, listener: () -> Unit = {}) {
        if (context != null) {
            this.alertListener = listener
            val intent = AlertActivity.newIntent(context!!, message, resourceIcon)
            startActivityForResult(intent, SHOW_ALERT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SHOW_ALERT) {
            if (resultCode == Activity.RESULT_OK) {
                alertListener.invoke()
            }
        }
    }

    fun showNetworkUnAvailable() {
        showAlertWithoutIcon(
            title = getString(R.string.title_error),
            message = getString(R.string.no_internet_connect)
        )
    }


    fun showMessageLong(message: String) {
        if (view != null) {
            Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
        }
    }

    val currentFragment: Fragment
        get() = (activity as MainActivity).getCurrentFragment() ?: this

    val profileFragment: Fragment?
        get() = (activity as MainActivity).profileFragment ?: this

    fun displaySnackBarWithBottomMargin(snackbar: Snackbar, marginBottom: Int = 0) {
        val snackBarView = snackbar.view
        val params = snackBarView.layoutParams as CoordinatorLayout.LayoutParams

        params.setMargins(
            params.leftMargin,
            params.topMargin,
            params.rightMargin,
            params.bottomMargin + marginBottom
        )

        snackBarView.layoutParams = params
        snackbar.show()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        const val HASH_PARAM = "hash_param"
        const val SHOW_ALERT = 0
        const val DEFAULT_ALERT_TIME_SECONDS = 3
        const val SHOW_BROADCAST = 1
        const val SWAP_CONFIRM = 1010
        const val SEND_CONFIRM = 1011
    }

    fun stopCounter() {
        val context = activity?.applicationContext
        if (context is KyberSwapApplication) {
            context.stopCounter()
        }
    }

    fun startCounter() {
        val context = activity?.applicationContext
        if (context is KyberSwapApplication) {
            context.startCounter()
        }
    }
}
