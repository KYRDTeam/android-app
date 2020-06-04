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

    var displayTime: Int = DEFAULT_DISPLAY_TIME

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
        displayTime = intent.getIntExtra(DISPLAY_TIME_SECONDS, DEFAULT_DISPLAY_TIME)
        binding.title = title
        binding.isVisibleTitle = !title.isNullOrEmpty()
        binding.flContainer.setOnClickListener {
            onBackPressed()
        }
        handler.postDelayed({
            onBackPressed()
        }, displayTime * 1000L)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }


    override fun onBackPressed() {
        handler.removeCallbacksAndMessages(null)
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)

        finish()
        overridePendingTransition(R.anim.from_top, android.R.anim.fade_out)
    }


    companion object {
        private const val ALERT_CONTENT = "alert_content"
        private const val ALERT_TITLE = "alert_title"
        private const val DISPLAY_TIME_SECONDS = "display_time_seconds"
        private const val DEFAULT_DISPLAY_TIME = 10

        fun newIntent(
            context: Context,
            title: String?,
            content: String,
            time: Int = DEFAULT_DISPLAY_TIME
        ) = Intent(context, AlertWithoutIconActivity::class.java).apply {
            putExtra(ALERT_CONTENT, content)
            putExtra(ALERT_TITLE, title)
            putExtra(DISPLAY_TIME_SECONDS, time)
        }
    }
}
