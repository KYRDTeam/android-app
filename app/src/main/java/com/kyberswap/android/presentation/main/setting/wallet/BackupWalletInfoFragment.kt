package com.kyberswap.android.presentation.main.setting.wallet

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentBackupWalletInfoBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class BackupWalletInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentBackupWalletInfoBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var value: String? = null

    private var wallet: Wallet? = null

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(BackupWalletInfoViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        value = arguments?.getString(PARAM_VALUE)
        wallet = arguments?.getParcelable(PARAM_WALLET)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBackupWalletInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.imgAddress.setImageBitmap(generateBarcode())
        binding.info = value

        binding.tvSave.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, value)
                type =
                    MIME_TYPE_TEXT
            }

            startActivity(sendIntent)
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        if (wallet != null) {
            viewModel.save(wallet!!.copy(hasBackup = true))
        }

        viewModel.saveWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveWalletState.Success -> {

                    }
                    is SaveWalletState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private fun generateBarcode(): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(
                value, BarcodeFormat.QR_CODE, resources.getDimensionPixelSize(
                    R.dimen.bar_code_dimen
                ), resources.getDimensionPixelSize(
                    R.dimen.bar_code_dimen
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val MIME_TYPE_TEXT = "text/plain"
        private const val PARAM_VALUE = "param_value"
        private const val PARAM_WALLET = "param_wallet"
        fun newInstance(value: String, wallet: Wallet?) =
            BackupWalletInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_VALUE, value)
                    putParcelable(PARAM_WALLET, wallet)
                }
            }
    }
}
