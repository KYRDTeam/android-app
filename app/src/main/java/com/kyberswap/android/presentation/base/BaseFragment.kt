package com.kyberswap.android.presentation.base

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.kyberswap.android.R
import com.kyberswap.android.presentation.common.AlertActivity
import com.kyberswap.android.presentation.main.MainActivity
import dagger.android.support.DaggerFragment

abstract class BaseFragment : DaggerFragment() {
    var dialog: ProgressDialog? = null

    var alertListener: () -> Unit = {}

    private fun initLoadingDialog() {
        dialog = ProgressDialog(this.context)
            .apply {
                setMessage(getString(R.string.message_loading))
                setCanceledOnTouchOutside(true)
                setCancelable(true)
    
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

    fun showAlert(message: String, resourceIcon: Int, listener: () -> Unit = {}) {
        if (context != null) {
            this.alertListener = listener
            val intent = AlertActivity.newIntent(context!!, message, resourceIcon)
            startActivityForResult(intent, SHOW_ALERT)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SHOW_ALERT) {
            if (resultCode == Activity.RESULT_OK) {
                alertListener.invoke()
    

    }

    fun showMessageLong(message: String) {
        if (view != null) {
            Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()

    }

    val currentFragment: Fragment
        get() = (activity as MainActivity).getCurrentFragment() ?: this

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

    companion object {
        const val SHOW_ALERT = 0
    }
}
