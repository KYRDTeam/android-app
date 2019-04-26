package com.kyberswap.android.presentation.base

import android.app.ProgressDialog
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.kyberswap.android.R
import dagger.android.support.DaggerFragment

abstract class BaseFragment : DaggerFragment() {
    var dialog: ProgressDialog? = null

    private fun initLoadingDialog() {
        dialog = ProgressDialog(this.context)
            .apply {
                setMessage(getString(R.string.message_loading))
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLoadingDialog()
    }

    fun showProgress(showProgress: Boolean) {
        if (showProgress) dialog?.show() else dialog?.dismiss()
    }

    fun showMessage(message: String) {
        if (view != null) {
            Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun showMessageLong(message: String) {
        if (view != null) {
            Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
