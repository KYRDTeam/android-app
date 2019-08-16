package com.kyberswap.android.presentation.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentImportSeedBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.ImportWalletState
import com.kyberswap.android.presentation.listener.addTextChangeListener
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_import_seed.*
import org.consenlabs.tokencore.wallet.model.Messages
import javax.inject.Inject

class ImportSeedFragment : BaseFragment() {

    private lateinit var binding: FragmentImportSeedBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var fromMain: Boolean = false

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ImportSeedViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromMain = arguments!!.getBoolean(FROM_MAIN_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImportSeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.edtSeed.addTextChangeListener {
            afterTextChanged {
                val count = if (it.toString().isEmpty()) 0
                else it.toString().trim().split(" ").size
                binding.txtWordCounter.text = getString(
                    R.string.text_counter,
                    count
                )
                if (count >= 12) {
                    binding.btnImportWallet.isEnabled = true
                }
            }
        }
        binding.btnImportWallet.setOnClickListener {
            viewModel.importFromSeed(
                edtSeed.text?.trim().toString(),
                if (edtWalletName.text.isNotEmpty()) edtWalletName.text.trim().toString()
                else getString(R.string.default_wallet_name)
            )
        }

        binding.imgQRCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()
        }

        viewModel.importWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.let { state ->
                showProgress(state == ImportWalletState.Loading)
                when (state) {
                    is ImportWalletState.Success -> {
                        showAlert(getString(R.string.import_wallet_success)) {
                            if (fromMain) {
                                activity?.onBackPressed()
                            } else {
                                navigator.navigateToHome()
                            }
                        }
                    }
                    is ImportWalletState.ShowError -> {
                        val message = when (state.message) {
                            Messages.WALLET_EXISTS -> {
                                getString(R.string.wallet_exist)
                            }

                            Messages.PRIVATE_KEY_INVALID -> {
                                getString(R.string.fail_import_private_key)
                            }

                            Messages.MNEMONIC_BAD_WORD -> {
                                getString(R.string.fail_import_mnemonic)
                            }

                            Messages.MAC_UNMATCH -> {
                                getString(R.string.fail_import_json)
                            }
                            else -> {
                                state.message ?: getString(R.string.something_wrong)
                            }

                        }
                        showAlertWithoutIcon(
                            message = message
                        )
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showAlertWithoutIcon(message = getString(R.string.message_cancelled))
            } else {
                binding.edtSeed.setText(result.contents.toString())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val FROM_MAIN_PARAM = "from_main_param"
        fun newInstance(fromMain: Boolean) =
            ImportSeedFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(FROM_MAIN_PARAM, fromMain)
                }
            }
    }
}
