package com.kyberswap.android.presentation.base

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.jaeger.library.StatusBarUtil
import com.kyberswap.android.R
import com.kyberswap.android.presentation.base.BaseFragment.Companion.SHOW_ALERT
import com.kyberswap.android.presentation.common.AlertActivity
import com.kyberswap.android.presentation.common.AlertWithoutIconActivity
import dagger.android.support.DaggerAppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper


abstract class BaseActivity : DaggerAppCompatActivity() {

    var alertListener: () -> Unit = {}

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLoadingDialog()
    }

    override fun setContentView(layoutResID: Int) {
        setStatusBar()
        super.setContentView(layoutResID)

    }

    protected fun setStatusBar() {
        StatusBarUtil.setTransparent(this)

    }

    fun showAlert(message: String, listener: () -> Unit = {}) {
        this.alertListener = listener
        val intent = AlertActivity.newIntent(this, message)
        startActivityForResult(intent, SHOW_ALERT)
    }

    fun showInsufficientAlert(title: String, message: String, listener: () -> Unit) {
        this.alertListener = listener
        val intent = AlertWithoutIconActivity.newIntent(this, title, message)
        startActivityForResult(intent, SHOW_ALERT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SHOW_ALERT) {
            if (resultCode == Activity.RESULT_OK) {
                alertListener.invoke()
            }
        }
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    var dialog: ProgressDialog? = null

    private fun initLoadingDialog() {
        dialog = ProgressDialog(this)
            .apply {
                setMessage(getString(R.string.message_loading))
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }
    }

    fun showProgress(showProgress: Boolean) {
        if (showProgress) dialog?.show() else dialog?.dismiss()
    }
}
