package com.kyberswap.android.presentation.base

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import com.jaeger.library.StatusBarUtil
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.presentation.base.BaseFragment.Companion.SHOW_ALERT
import com.kyberswap.android.presentation.common.AlertActivity
import com.kyberswap.android.presentation.common.AlertWithoutIconActivity
import com.kyberswap.android.presentation.common.CustomAlertActivity
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.balance.send.SendFragment
import dagger.android.support.DaggerAppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper


abstract class BaseActivity : DaggerAppCompatActivity() {

    var alertListener: () -> Unit = {}

    private val handler by lazy {
        Handler()
    }

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

    fun showBroadcastAlert(
        type: Int = CustomAlertActivity.DIALOG_TYPE_BROADCASTED,
        transaction: Transaction? = null,
        listener: () -> Unit = {}
    ) {
        this.alertListener = listener
        val intent = CustomAlertActivity.newIntent(this, type, transaction)
        val context = applicationContext
        if (context is KyberSwapApplication) {
            val currentActivity = context.currentActivity
            if (currentActivity != null && currentActivity is CustomAlertActivity) {
                currentActivity.finish()
            }
        }
        startActivityForResult(intent, BaseFragment.SHOW_BROADCAST)
    }

    fun showAlertWithoutIcon(title: String? = null, message: String, listener: () -> Unit = {}) {
        this.alertListener = listener
        val intent = AlertWithoutIconActivity.newIntent(this, title, message)
        startActivityForResult(intent, SHOW_ALERT)
    }

    fun showAlert(message: String, listener: () -> Unit = {}) {
        this.alertListener = listener
        val intent = AlertActivity.newIntent(this, message)
        startActivityForResult(intent, SHOW_ALERT)
    }

    fun showAlert(
        message: String,
        resourceIcon: Int,
        listener: () -> Unit = {},
        displayTime: Int = 3
    ) {
        this.alertListener = listener
        val intent = AlertActivity.newIntent(this, message, resourceIcon, displayTime)
        startActivityForResult(intent, SHOW_ALERT)
    }

    fun showError(message: String, listener: () -> Unit = {}) {
        showAlert(message, R.drawable.ic_info_error, listener)
    }

    fun showErrorWithTime(message: String, time: Int, listener: () -> Unit = {}) {
        showAlert(message, R.drawable.ic_info_error, listener, time)
    }

    fun showNetworkUnAvailable() {
        showAlertWithoutIcon(
            title = getString(R.string.title_error),
            message = getString(R.string.no_internet_connect)
        )
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
        } else if (requestCode == BaseFragment.SHOW_BROADCAST) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (this is MainActivity) {
                        val action = data.getIntExtra(CustomAlertActivity.DIALOG_ACTION_PARAM, -1)
                        if (action == CustomAlertActivity.ACTION_TRANSFER) {

                            val lastAddedFragment =
                                getCurrentFragment()?.childFragmentManager?.fragments?.lastOrNull()
                            if (lastAddedFragment !is SendFragment) {
                                this.navigateToSendScreen()
                            }
                        } else if (action == CustomAlertActivity.ACTION_NEW_SWAP) {
                            this.moveToTab(MainPagerAdapter.SWAP)
                        }
                    }
                }
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
