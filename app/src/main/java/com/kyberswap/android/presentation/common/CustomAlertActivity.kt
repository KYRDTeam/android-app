package com.kyberswap.android.presentation.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityCustomAlertBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.common.BetterImageSpan.ALIGN_CENTER
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.openUrl


class CustomAlertActivity : BaseActivity() {

    private var dialogType: Int = DIALOG_TYPE_BROADCASTED
    private var transaction: Transaction? = null
    private var action: Int = -1

    private var isCounterStop: Boolean = false

    private val handler by lazy {
        Handler()
    }
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityCustomAlertBinding>(
            this,
            R.layout.activity_custom_alert
        )
    }

    private val isDone: Boolean
        get() = DIALOG_TYPE_DONE == dialogType


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        action = -1
        val context = applicationContext
        if (context is KyberSwapApplication) {
            context.setCurrentActivity(this)
        }

        dialogType = intent.getIntExtra(DIALOG_TYPE_PARAM, DIALOG_TYPE_BROADCASTED)
        transaction = intent.getParcelableExtra(TRANSACTION_PARAM)
        binding.isDone = isDone
        if (isDone) {
            transaction?.let { transaction ->
                val message = when (transaction.transactionType) {
                    Transaction.TransactionType.SEND -> {
                        if (transaction.isTransactionFail) {
                            title = getString(R.string.title_fail)
                            String.format(
                                getString(R.string.notification_fail_sent),
                                transaction.displayValue,
                                transaction.to.displayWalletAddress()
                            )
                        } else {
                            String.format(
                                getString(R.string.notification_success_sent),
                                transaction.displayValue,
                                transaction.to.displayWalletAddress()
                            )
                        }
                    }
                    Transaction.TransactionType.SWAP -> {
                        if (transaction.isTransactionFail) {
                            String.format(
                                getString(R.string.notification_fail_swap),
                                transaction.displaySource, transaction.displayDest
                            )
                        } else {
                            String.format(
                                getString(R.string.notification_success_swap),
                                transaction.displaySource, transaction.displayDest
                            )
                        }
                    }
                    else -> {
                        ""
                    }
                }
                if (message.isNotEmpty()) {
                    handler.post {
                        // dummy string is the place holder for image
                        val dummyText = "dummy"
                        val spannableString = SpannableString("$message $dummyText")
                        val drawableIcon =
                            ContextCompat.getDrawable(this, R.drawable.ic_open_in_new_icon)
                        if (drawableIcon != null) {
                            drawableIcon.setBounds(
                                0,
                                0,
                                drawableIcon.intrinsicWidth,
                                drawableIcon.intrinsicHeight
                            )
                            val spanImage = BetterImageSpan(drawableIcon, ALIGN_CENTER)


                            try {
                                spannableString.setSpan(
                                    spanImage,
                                    spannableString.indexOf(dummyText),
                                    spannableString.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            }
                        }
                        binding.tvDetail.text = spannableString
                        binding.isFailed = transaction.isTransactionFail
                    }
                }
            }
        }
        binding.transaction = transaction
        binding.executePendingBindings()


        binding.flContainer.setOnClickListener {
            onBackPressed()
        }
//        handler.postDelayed({
//            onBackPressed()
//        }, 15 * 60 * 1000L)

        binding.imgViewPendingTx.setOnClickListener {
            transaction?.let {
                (applicationContext as KyberSwapApplication).stopCounter()
                isCounterStop = true
                openUrl(getString(R.string.transaction_etherscan_endpoint_url) + it.hash)
            }
        }

        binding.tvDetail.setOnClickListener {
            transaction?.let {
                (applicationContext as KyberSwapApplication).stopCounter()
                isCounterStop = true
                openUrl(getString(R.string.transaction_etherscan_endpoint_url) + it.hash)
            }
        }

        binding.tvSwap.setOnClickListener {
            action = ACTION_NEW_SWAP
            onBackPressed()
        }

        binding.tvTransfer.setOnClickListener {
            action = ACTION_TRANSFER
            onBackPressed()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onResume() {
        if (isCounterStop) {
            isCounterStop = false
            (applicationContext as KyberSwapApplication).startCounter()
        }
        super.onResume()
    }


    override fun onBackPressed() {
        val returnIntent = Intent()
        returnIntent.putExtra(DIALOG_ACTION_PARAM, action)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
        overridePendingTransition(R.anim.from_top, android.R.anim.fade_out)
        action = -1
        handler.removeCallbacksAndMessages(null)
    }


    companion object {
        const val DIALOG_TYPE_BROADCASTED = 0
        const val DIALOG_TYPE_DONE = 1
        const val ACTION_NEW_SWAP = 0
        const val ACTION_TRANSFER = 1
        private const val DIALOG_TYPE_PARAM = "dialog_type_param"
        private const val TRANSACTION_PARAM = "transaction_param"
        const val DIALOG_ACTION_PARAM = "dialog_action_param"

        fun newIntent(
            context: Context,
            dialogType: Int,
            transaction: Transaction?

        ) = Intent(context, CustomAlertActivity::class.java).apply {
            putExtra(DIALOG_TYPE_PARAM, dialogType)
            putExtra(TRANSACTION_PARAM, transaction)
        }
    }
}
