package com.kyberswap.android.presentation.helper

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.*
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.presentation.main.alert.EligibleTokenAdapter
import javax.inject.Inject

class DialogHelper @Inject constructor(private val activity: AppCompatActivity) {

    fun showConfirmation(positiveListener: () -> Unit) {
        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogConfirmationBinding>(
                LayoutInflater.from(activity), R.layout.dialog_confirmation, null, false
            )

        binding.tvConfirm.setOnClickListener {
            positiveListener.invoke()
            dialog.dismiss()
        }

        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }


    fun showBottomSheetDialog(onClickCreateWallet: () -> Unit, onClickImportWallet: () -> Unit) {

        val binding = DataBindingUtil.inflate<DialogBottomSheetBinding>(
            LayoutInflater.from(activity), R.layout.dialog_bottom_sheet, null, false
        )

        val dialog = BottomSheetDialog(activity)
        dialog.setContentView(binding.root)

        binding.tvCreateWallet.setOnClickListener {
            onClickCreateWallet.invoke()
            dialog.dismiss()
        }

        binding.tvImportWallet.setOnClickListener {
            onClickImportWallet.invoke()
            dialog.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    fun showCancelOrder(order: Order, positiveListener: () -> Unit) {
        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogCancelOrderConfirmationBinding>(
                LayoutInflater.from(activity),
                R.layout.dialog_cancel_order_confirmation,
                null,
                false
            )

        binding.tvConfirm.setOnClickListener {
            positiveListener.invoke()
            dialog.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        binding.order = order
        binding.executePendingBindings()

        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }


    fun showResetPassword(positiveListener: (email: String) -> Unit) {
        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogForgotPasswordBinding>(
                LayoutInflater.from(activity), R.layout.dialog_forgot_password, null, false
            )

        binding.tvSend.setOnClickListener {
            positiveListener.invoke(binding.edtEmail.text.toString())
            dialog.dismiss()
        }

        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    fun showWrongBackup(positiveListener: () -> Unit) {

        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogBackupMessageBinding>(
                LayoutInflater.from(activity), R.layout.dialog_backup_message, null, false
            )

        binding.tvTryAgain.setOnClickListener {
            positiveListener.invoke()
            dialog.dismiss()
        }

        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    fun showWrongBackup(
        numberOfTry: Int,
        positiveListener: () -> Unit = {},
        negativeListener: () -> Unit = {}
    ) {
        if (numberOfTry > 0) {
            showWrongBackupAgain(positiveListener, negativeListener)
        } else {
            showWrongBackup(positiveListener)
        }
    }

    fun showWrongBackupAgain(
        positiveListener: () -> Unit,
        negativeListener: () -> Unit = {}
    ) {

        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogBackupMessageAgainBinding>(
                LayoutInflater.from(activity), R.layout.dialog_backup_message_again, null, false
            )

        binding.tvTryAgain.setOnClickListener {
            positiveListener.invoke()
            dialog.dismiss()
        }

        binding.tvRetry.setOnClickListener {
            negativeListener.invoke()
            dialog.dismiss()
        }



        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    fun showConfirmDeleteAlert(
        positiveListener: () -> Unit,
        negativeListener: () -> Unit = {}
    ) {

        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogConfirmDeleteAlertBinding>(
                LayoutInflater.from(activity), R.layout.dialog_confirm_delete_alert, null, false
            )

        binding.tvOk.setOnClickListener {
            positiveListener.invoke()
            dialog.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            negativeListener.invoke()
            dialog.dismiss()
        }



        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setDialogDimens(dialog: AlertDialog, width: Int?, height: Int? = null) {
        val lp = WindowManager.LayoutParams()

        lp.copyFrom(dialog.window?.attributes)
        if (width != null) {
            lp.width = width
        }
        if (height != null) {
            lp.height = height
        }

        dialog.window?.attributes = lp
    }

    fun showEligibleToken(appExecutors: AppExecutors, tokens: List<String>) {
        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogEligibleTokenBinding>(
                LayoutInflater.from(activity), R.layout.dialog_eligible_token, null, false
            )

        binding.rvToken.layoutManager = GridLayoutManager(
            activity,
            4
        )

        val adapter = EligibleTokenAdapter(appExecutors) {

        }
        binding.imgClose.setOnClickListener {
            dialog.dismiss()
        }
        binding.rvToken.adapter = adapter
        adapter.submitList(tokens)


        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }
}
