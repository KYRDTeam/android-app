package com.kyberswap.android.presentation.base

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.jaeger.library.StatusBarUtil
import com.kyberswap.android.R
import dagger.android.support.DaggerAppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper


abstract class BaseActivity : DaggerAppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLoadingDialog()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setStatusBar()
    }

    protected fun setStatusBar() {
        StatusBarUtil.setTransparent(this)
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
