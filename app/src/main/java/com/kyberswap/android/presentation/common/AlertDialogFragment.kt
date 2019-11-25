package com.kyberswap.android.presentation.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentAlertDialogBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.util.OPEN_TX_FROM_ALERT_EVENT
import com.kyberswap.android.util.OPEN_TX_MINED_ACTION
import com.kyberswap.android.util.OPEN_TX_PENDING_ACTION
import com.kyberswap.android.util.TRANSACTION_DETAIL_ACTION
import com.kyberswap.android.util.TX_FAILED_ACTION
import com.kyberswap.android.util.TX_FAILED_EVENT
import com.kyberswap.android.util.TX_MINED_EVENT
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.openUrl


/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
class AlertDialogFragment : DialogFragment() {

    private var dialogType: Int = CustomAlertActivity.DIALOG_TYPE_BROADCASTED
    private var transaction: Transaction? = null

    private var isCounterStop: Boolean = false

    lateinit var analytics: FirebaseAnalytics

    private val handler by lazy {
        Handler()
    }

    private val isDone: Boolean
        get() = DIALOG_TYPE_DONE == dialogType

    private val isBroadcasted: Boolean
        get() = DIALOG_TYPE_BROADCASTED == dialogType

    private val binding by lazy {
        DataBindingUtil.inflate<FragmentAlertDialogBinding>(
            LayoutInflater.from(activity), R.layout.fragment_alert_dialog, null, false
        )
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        analytics = FirebaseAnalytics.getInstance(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NO_TITLE, R.style.AlertDialogStyle)
        dialogType = arguments?.getInt(DIALOG_TYPE_PARAM) ?: DIALOG_TYPE_BROADCASTED
        transaction = arguments?.getParcelable(TRANSACTION_PARAM)
    }

    private fun executeBinding() {
        binding.isDone = isDone
        binding.transaction = transaction
        binding.executePendingBindings()
        if (isDone) {
            transaction?.let { transaction ->
                val message = when (transaction.transactionType) {
                    Transaction.TransactionType.SEND -> {
                        if (transaction.isTransactionFail) {
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
                            activity?.let {
                                ContextCompat.getDrawable(
                                    it,
                                    R.drawable.ic_open_in_new_icon
                                )
                            }
                        if (drawableIcon != null) {
                            drawableIcon.setBounds(
                                0,
                                0,
                                drawableIcon.intrinsicWidth,
                                drawableIcon.intrinsicHeight
                            )
                            val spanImage = BetterImageSpan(
                                drawableIcon,
                                BetterImageSpan.ALIGN_CENTER
                            )


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
    }

    private lateinit var callback: Callback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)

        val p = dialog?.window?.attributes
        p?.width = ViewGroup.LayoutParams.MATCH_PARENT
        p?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE

        dialog?.window?.attributes = p

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (dialog == null) showsDialog = false
        super.onActivityCreated(savedInstanceState)
//        binding.flContainer.setOnClickListener {
//            activity?.onBackPressed()
//        }

        executeBinding()

        if (isDone) {
            analytics.logEvent(
                TX_MINED_EVENT,
                Bundle().createEvent(TRANSACTION_DETAIL_ACTION, transaction?.displayTransaction)
            )
            if (transaction?.isTransactionFail == true) {
                analytics.logEvent(
                    TX_FAILED_EVENT,
                    Bundle().createEvent(
                        TX_FAILED_ACTION,
                        transaction?.gas + "|" + transaction?.gasUsed
                    )
                )
            }
        }

        binding.imgViewPendingTx.setOnClickListener {
            analytics.logEvent(
                OPEN_TX_FROM_ALERT_EVENT, Bundle().createEvent(
                    if (isDone) OPEN_TX_MINED_ACTION else OPEN_TX_PENDING_ACTION,
                    transaction?.displayTransaction
                )
            )

            transaction?.let {
                (context?.applicationContext as KyberSwapApplication).stopCounter()
                isCounterStop = true
                openUrl(getString(R.string.transaction_etherscan_endpoint_url) + it.hash)
            }
        }

        binding.tvDetail.setOnClickListener {
            analytics.logEvent(
                OPEN_TX_FROM_ALERT_EVENT, Bundle().createEvent(
                    if (isDone) OPEN_TX_MINED_ACTION else OPEN_TX_PENDING_ACTION,
                    transaction?.displayTransaction
                )
            )

            transaction?.let {
                (context?.applicationContext as KyberSwapApplication).stopCounter()
                isCounterStop = true
                openUrl(getString(R.string.transaction_etherscan_endpoint_url) + it.hash)
            }
        }

        binding.tvSwap.setOnClickListener {
            dismiss()
            callback.onSwap()
        }

        binding.tvTransfer.setOnClickListener {
            dismiss()
            callback.onTransfer()
        }
    }

    companion object {
        const val DIALOG_TYPE_BROADCASTED = 0
        const val DIALOG_TYPE_DONE = 1
        private const val DIALOG_TYPE_PARAM = "dialog_type_param"
        private const val TRANSACTION_PARAM = "transaction_param"

        fun newInstance(
            dialogType: Int,
            transaction: Transaction?
        ) = AlertDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(DIALOG_TYPE_PARAM, dialogType)
                putParcelable(TRANSACTION_PARAM, transaction)
            }
        }
    }


    interface Callback {
        fun onSwap()
        fun onTransfer()
    }
}
