package com.kyberswap.android.presentation.helper

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.*
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.presentation.main.alert.EligibleTokenAdapter
import com.kyberswap.android.presentation.main.alert.Passport
import com.kyberswap.android.presentation.main.alert.PassportAdapter
import io.reactivex.disposables.CompositeDisposable
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


    fun showBottomSheetBackupPhraseDialog(
        backupKeyStore: () -> Unit,
        backupPrivateKey: () -> Unit,
        backupMnemonic: () -> Unit,
        backupCopyAddress: () -> Unit
    ) {

        val binding = DataBindingUtil.inflate<DialogBackupPhraseBottomSheetBinding>(
            LayoutInflater.from(activity), R.layout.dialog_backup_phrase_bottom_sheet, null, false
        )

        val dialog = BottomSheetDialog(activity)
        dialog.setContentView(binding.root)

        binding.tvBackupKeystore.setOnClickListener {
            backupKeyStore.invoke()
            dialog.dismiss()
        }

        binding.tvBackupPrivateKey.setOnClickListener {
            backupPrivateKey.invoke()
            dialog.dismiss()
        }

        binding.tvBackupMnemonic.setOnClickListener {
            backupMnemonic.invoke()
            dialog.dismiss()
        }

        binding.tvBackupCopyAddress.setOnClickListener {
            backupCopyAddress.invoke()
            dialog.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showBottomSheetManageWalletDialog(
        hideSwitchOption: Boolean = false,
        onClickSwitchWallet: () -> Unit,
        onClickEditWallet: () -> Unit,
        onClickDeleteWallet: () -> Unit
    ) {

        val binding = DataBindingUtil.inflate<DialogManageWalletBottomSheetBinding>(
            LayoutInflater.from(activity), R.layout.dialog_manage_wallet_bottom_sheet, null, false
        )

        val dialog = BottomSheetDialog(activity)
        dialog.setContentView(binding.root)

        binding.tvSwitchWallet.visibility = if (hideSwitchOption) View.GONE else View.VISIBLE
        binding.tvSwitchWallet.setOnClickListener {
            onClickSwitchWallet.invoke()
            dialog.dismiss()
        }

        binding.tvEditWallet.setOnClickListener {
            onClickEditWallet.invoke()
            dialog.dismiss()
        }

        binding.tvDeleteWallet.setOnClickListener {
            onClickDeleteWallet.invoke()
            dialog.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showImagePickerBottomSheetDialog(onCameraSelect: () -> Unit, onGallerySelect: () -> Unit) {

        val binding = DataBindingUtil.inflate<DialogImagePickerBottomSheetBinding>(
            LayoutInflater.from(activity), R.layout.dialog_image_picker_bottom_sheet, null, false
        )

        val dialog = BottomSheetDialog(activity)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {
            onCameraSelect.invoke()
            dialog.dismiss()
        }

        binding.tvPhotoLibrary.setOnClickListener {
            onGallerySelect.invoke()
            dialog.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showBottomSheetPassportDialog(
        appExecutors: AppExecutors
    ) {

        val binding = DataBindingUtil.inflate<DialogPassportBottomSheetBinding>(
            LayoutInflater.from(activity), R.layout.dialog_passport_bottom_sheet, null, false
        )

        val dialog = BottomSheetDialog(activity)
        dialog.setContentView(binding.root)

        val resources = listOf(
            R.drawable.passport_show_corner,
            R.drawable.passport_cover,
            R.drawable.passport_blurry,
            R.drawable.passport_correct
        )

        val contents = listOf(
            R.string.must_show_all_4_corners_of_the_card,
            R.string.must_not_be_covered_in_anyway,
            R.string.must_not_be_blurry,
            R.string.this_is_right
        )

        val passports = resources.zip(contents) { resource, content ->
            Passport(resource, content)
        }

        val passportAdapter = PassportAdapter(appExecutors)

        binding.rvPassport.layoutManager = GridLayoutManager(
            activity,
            2
        )
        binding.rvPassport.adapter = passportAdapter
        passportAdapter.submitList(passports)
        dialog.show()
    }

    fun showBottomSheetHoldPassportDialog(
        appExecutors: AppExecutors
    ) {

        val binding = DataBindingUtil.inflate<DialogHoldingPassportBottomSheetBinding>(
            LayoutInflater.from(activity),
            R.layout.dialog_holding_passport_bottom_sheet,
            null,
            false
        )

        val dialog = BottomSheetDialog(activity)
        dialog.setContentView(binding.root)

        val resources = listOf(
            R.drawable.passport_hold_incorrect,
            R.drawable.passport_hold_correct
        )

        val contents = listOf(
            R.string.passport_hold_incorrect,
            R.string.passport_hold_correct
        )

        val passports = resources.zip(contents) { resource, content ->
            Passport(resource, content)
        }

        val passportAdapter = PassportAdapter(appExecutors)

        binding.rvPassport.layoutManager = GridLayoutManager(
            activity,
            2
        )
        binding.rvPassport.adapter = passportAdapter
        passportAdapter.submitList(passports)
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


    fun showInputPassword(
        compositeDisposable: CompositeDisposable,
        onFinish: (password: String) -> Unit
    ) {
        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogPasswordBackupWalletBinding>(
                LayoutInflater.from(activity), R.layout.dialog_password_backup_wallet, null, false
            )

        dialog.setView(binding.root)
        binding.tvDone.setOnClickListener {
            if (binding.edtPassword.text.isNullOrBlank()) {
                binding.ilPassword.error = activity.getString(R.string.field_required)
                return@setOnClickListener
            }
            if (binding.edtConfirmPassword.text.isNullOrBlank()) {
                binding.ilConfirmPassword.error = activity.getString(R.string.field_required)
                return@setOnClickListener
            }
            if (binding.edtPassword.text.toString() != binding.edtConfirmPassword.text.toString()) {
                binding.ilConfirmPassword.error = activity.getString(R.string.password_mismatch)
                return@setOnClickListener
            }

            onFinish.invoke(binding.edtPassword.text.toString())
            dialog.dismiss()

        }
        compositeDisposable.add(binding.edtPassword.textChanges().skipInitialValue().subscribe {
            binding.ilPassword.error = null
        })

        compositeDisposable.add(binding.edtConfirmPassword.textChanges().skipInitialValue().subscribe {
            binding.ilConfirmPassword.error = null
        })

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

    fun showAlertTriggerDialog(
        alert: Alert,
        positiveListener: () -> Unit = {}
    ) {

        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogAlertTriggerBinding>(
                LayoutInflater.from(activity), R.layout.dialog_alert_trigger, null, false
            )

        binding.tvOk.setOnClickListener {
            positiveListener.invoke()
            dialog.dismiss()
        }

        binding.alert = alert
        binding.executePendingBindings()
        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun showSkipBackupPhraseDialog(
        positiveListener: () -> Unit,
        negativeListener: () -> Unit = {}
    ) {

        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogSkipBackupPhraseBinding>(
                LayoutInflater.from(activity), R.layout.dialog_skip_backup_phrase, null, false
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

    fun showExceedNumberAlertDialog(positiveListener: () -> Unit = {}) {
        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogMaximumAlertBinding>(
                LayoutInflater.from(activity), R.layout.dialog_maximum_alert, null, false
            )

        binding.tvConfirm.setOnClickListener {
            positiveListener.invoke()
            dialog.dismiss()
        }

        dialog.setView(binding.root)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun showOrderFillDialog(
        notification: NotificationLimitOrder,
        positiveListener: (url: String) -> Unit
    ) {

        val dialog = AlertDialog.Builder(activity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        val binding =
            DataBindingUtil.inflate<DialogOrderFilledBinding>(
                LayoutInflater.from(activity), R.layout.dialog_order_filled, null, false
            )

        binding.tvDetail.setOnClickListener {
            positiveListener.invoke(notification.txHash)
            dialog.dismiss()
        }

        dialog.setView(binding.root)

        binding.order = Order(notification)
        binding.executePendingBindings()

        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}
