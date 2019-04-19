package com.kyberswap.android.presentation.base

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatDelegate
import com.kyberswap.android.R
import dagger.android.support.DaggerAppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper


abstract class BaseActivity : DaggerAppCompatActivity() {


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        initLoadingDialog()
    }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

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
