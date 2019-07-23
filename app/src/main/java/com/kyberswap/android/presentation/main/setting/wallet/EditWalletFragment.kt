package com.kyberswap.android.presentation.main.setting.wallet


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentEditWalletBinding
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class EditWalletFragment : BaseFragment() {

    private lateinit var binding: FragmentEditWalletBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper


    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var wallet: Wallet? = null


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(EditWalletViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.wallet = wallet
        binding.tvShowBackupPhrase.setOnClickListener {
            dialogHelper.showBottomSheetBackupPhraseDialog(
                {
                    dialogHelper.showInputPassword(viewModel.compositeDisposable) {
                        wallet?.let { it1 -> viewModel.backupKeyStore(it, it1) }

                    }
                },
                {
                    wallet?.let { it1 -> viewModel.backupPrivateKey(it1) }
                },
                {
                    wallet?.let { it1 -> viewModel.backupMnemonic(it1) }

                }, {
                    copyWalletAddress()
                }
            )
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvDeleteWallet.setOnClickListener {

            dialogHelper.showConfirmation(
                getString(R.string.title_delete),
                getString(R.string.delete_wallet_confirmation),
                {
                    wallet?.let { it1 -> viewModel.deleteWallet(it1) }
                })
        }

        binding.imgDone.setOnClickListener {
            val name = if (binding.edtWalletName.text.isNullOrBlank()) {
                getString(R.string.default_wallet_name)
            } else {
                binding.edtWalletName.text.toString()
            }
            wallet?.copy(name = name)?.let { wl ->
                viewModel.save(wl)
            }
        }

        viewModel.deleteWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DeleteWalletState.Loading)
                when (state) {
                    is DeleteWalletState.Success -> {
                        showAlert(getString(R.string.delete_wallet_success))
                        onDeleteWalletSuccess(state.verifyStatus)
                    }
                    is DeleteWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.exportKeystoreWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == ExportWalletState.Loading)
                when (state) {
                    is ExportWalletState.Success -> {
                        onExportWalletComplete(state.value)
                    }
                    is ExportWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.exportPrivateKeyWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == ExportWalletState.Loading)
                when (state) {
                    is ExportWalletState.Success -> {
                        navigator.navigateToBackupWalletInfo(currentFragment, state.value)
                    }
                    is ExportWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })


        viewModel.exportMnemonicCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == ExportWalletState.Loading)
                when (state) {
                    is ExportWalletState.Success -> {
                        navigator.navigateToBackupWalletInfo(currentFragment, state.value)
                    }
                    is ExportWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.saveWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveWalletState.Success -> {
                        onSaveComplete()
                    }
                    is SaveWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })
    }

    private fun onSaveComplete() {
        activity?.onBackPressed()
    }

    private fun copyWalletAddress() {
        val clipboard =
            context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("Copy", wallet?.address)
        clipboard!!.primaryClip = clip
        showAlert(getString(R.string.address_copy))
    }

    private fun onExportWalletComplete(content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        this.startActivity(sendIntent)
    }

    private fun onDeleteWalletSuccess(verifyStatus: VerifyStatus) {
        if (verifyStatus.isEmptyWallet) {
            navigator.navigateToLandingPage()
            activity?.finishAffinity()
        } else {
            activity?.onBackPressed()
        }

    }


    companion object {
        private const val WALLET_PARAM = "param_wallet"
        fun newInstance(wallet: Wallet) =
            EditWalletFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }


}
