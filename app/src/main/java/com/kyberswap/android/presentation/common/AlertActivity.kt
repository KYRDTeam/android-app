package com.kyberswap.android.presentation.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityAlertBinding
import com.kyberswap.android.presentation.base.BaseActivity


class AlertActivity : BaseActivity() {

    private val handler by lazy {
        Handler()
    }
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityAlertBinding>(
            this,
            R.layout.activity_alert
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.content = intent.getStringExtra(ALERT_CONTENT)
        binding.flContainer.setOnClickListener {
            onBackPressed()

        handler.postDelayed({
            onBackPressed()
, 1000)
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
        fun newIntent(context: Context, content: String) =
            Intent(context, AlertActivity::class.java).apply {
                putExtra(ALERT_CONTENT, content)
    
    }
}
