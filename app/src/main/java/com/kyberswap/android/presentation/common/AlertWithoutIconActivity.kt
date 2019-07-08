package com.kyberswap.android.presentation.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityInsufficientAlertBinding
import com.kyberswap.android.presentation.base.BaseActivity


class AlertWithoutIconActivity : BaseActivity() {

    private val handler by lazy {
        Handler()
    }
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityInsufficientAlertBinding>(
            this,
            R.layout.activity_insufficient_alert
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.content = intent.getStringExtra(ALERT_CONTENT)
        val title = intent.getStringExtra(ALERT_TITLE)
        binding.title = title
        binding.isVisibleTitle = !title.isNullOrEmpty()
        binding.flContainer.setOnClickListener {
            onBackPressed()
        }
        handler.postDelayed({
            onBackPressed()
        }, 2000)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }


    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)

        super.onBackPressed()
        overridePendingTransition(R.anim.from_top, R.anim.back_to_top)
    }


    companion object {
        private const val ALERT_CONTENT = "alert_content"
        private const val ALERT_TITLE = "alert_title"

        fun newIntent(
            context: Context,
            title: String?,
            content: String
        ) = Intent(context, AlertWithoutIconActivity::class.java).apply {
            putExtra(ALERT_CONTENT, content)
            putExtra(ALERT_TITLE, title)
        }
    }
}
