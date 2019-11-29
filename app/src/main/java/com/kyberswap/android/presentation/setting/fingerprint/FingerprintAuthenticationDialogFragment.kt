package com.kyberswap.android.presentation.setting.fingerprint

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.kyberswap.android.R
import com.kyberswap.android.databinding.DialogFingerprintBinding

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
class FingerprintAuthenticationDialogFragment : DialogFragment(),

    FingerprintUiHelper.Callback {

    private lateinit var callback: Callback
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var fingerprintUiHelper: FingerprintUiHelper
    private lateinit var inputMethodManager: InputMethodManager

    private val binding by lazy {
        DataBindingUtil.inflate<DialogFingerprintBinding>(
            LayoutInflater.from(activity), R.layout.dialog_fingerprint, null, false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (dialog == null) showsDialog = false
        super.onActivityCreated(savedInstanceState)
        dialog?.setTitle(getString(R.string.app_name))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_corner_dialog_background)
        binding.cancelButton.setOnClickListener { dismissAllowingStateLoss() }
        activity?.let {
            fingerprintUiHelper = FingerprintUiHelper(
                it.getSystemService(FingerprintManager::class.java),
                binding.fingerprintIcon,
                binding.fingerprintStatus,
                this
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        if (::fingerprintUiHelper.isInitialized && ::cryptoObject.isInitialized) {
            fingerprintUiHelper.startListening(cryptoObject)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPause() {
        super.onPause()
        if (::fingerprintUiHelper.isInitialized) {
            fingerprintUiHelper.stopListening()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        inputMethodManager = context.getSystemService(InputMethodManager::class.java)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setCryptoObject(cryptoObject: FingerprintManager.CryptoObject) {
        this.cryptoObject = cryptoObject
    }

    override fun onAuthenticated() {
        callback.onAuthSuccess()
        dismissAllowingStateLoss()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onError() {
        if (::fingerprintUiHelper.isInitialized) {
            fingerprintUiHelper.stopListening()
        }
    }

    interface Callback {
        fun onAuthSuccess()
        fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean = true)
    }
}
