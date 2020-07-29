package com.kyberswap.android.presentation.main.setting.wallet


import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentEditWalletBinding
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.setting.PassCodeLockActivity
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.hideKeyboard
import java.io.OutputStream
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

    private var jsonContent: String? = null

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(EditWalletViewModel::class.java)
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

            showPassCodeLock(EDIT_WALLET)
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvDeleteWallet.setOnClickListener {
            showPassCodeLock(DELETE_WALLET)
        }

        binding.imgDone.setOnClickListener {
            hideKeyboard()
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private fun showPassCodeLock(requestCode: Int) {
        if (activity != null) {
            startActivityForResult(
                PassCodeLockActivity.newIntent(
                    activity!!,
                    PassCodeLockActivity.PASS_CODE_LOCK_TYPE_MANAGE_WALLET
                ), requestCode
            )
        }
    }

    private fun onSaveComplete() {
        activity?.onBackPressed()
    }

    private fun copyWalletAddress() {
        if (context != null) {
            val clipboard =
                context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("Copy", wallet?.address)
            if (clipboard != null && clip != null) {
                clipboard.setPrimaryClip(clip)
                showAlert(getString(R.string.address_copy))
            }
        }
    }

    private fun onExportWalletComplete(content: String) {
        val fileName = getString(R.string.wallet_file_prefix) + wallet?.address + ".json"
        this.jsonContent = content
        createFile("text/json", fileName)
    }

    private fun createFile(mimeType: String, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        startActivityForResult(intent, WRITE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_WALLET) {
            if (resultCode == Activity.RESULT_OK) {
                dialogHelper.showBottomSheetBackupPhraseDialog(
                    wallet?.mnemonicAvailable == true,
                    {
                        dialogHelper.showConfirmation(
                            "",
                            getString(R.string.warning_backup_keystore),
                            {
                                dialogHelper.showInputPassword(viewModel.compositeDisposable) {
                                    wallet?.let { it1 -> viewModel.backupKeyStore(it, it1) }

                                }
                            })

                    },
                    {

                        dialogHelper.showConfirmation(
                            "",
                            getString(R.string.warning_backup_private_key),
                            {
                                wallet?.let { it1 -> viewModel.backupPrivateKey(it1) }
                            })

                    },
                    {
                        dialogHelper.showConfirmation(
                            "",
                            getString(R.string.warning_backup_mnemonic),
                            {
                                wallet?.let { it1 -> viewModel.backupMnemonic(it1) }
                            })

                    }, {
                        copyWalletAddress()
                    }
                )
            }
        } else if (requestCode == DELETE_WALLET) {
            if (resultCode == Activity.RESULT_OK) {
                dialogHelper.showConfirmation(
                    getString(R.string.title_delete),
                    getString(R.string.delete_wallet_confirmation),
                    {
                        wallet?.let { it1 -> viewModel.deleteWallet(it1) }
                    })
            }
        } else if (requestCode == WRITE_REQUEST_CODE) {
            val cr = context?.contentResolver
            val uri = data?.data
            uri?.let {
                try {
                    cr?.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

                var os: OutputStream? = null
                try {
                    os = cr?.openOutputStream(uri)
                    os?.write(jsonContent?.toByteArray())
                } catch (e: Exception) {
                } finally {
                    closeQuietly(os)
                }
            }
        }
    }

    private fun closeQuietly(closeable: AutoCloseable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
            }
        }
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
        private const val WRITE_REQUEST_CODE = 1000
        fun newInstance(wallet: Wallet) =
            EditWalletFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
