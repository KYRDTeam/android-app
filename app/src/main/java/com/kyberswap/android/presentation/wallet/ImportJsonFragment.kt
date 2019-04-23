package com.kyberswap.android.presentation.wallet


import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentImportJsonBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.ImportWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_import_json.*
import javax.inject.Inject


class ImportJsonFragment : BaseFragment() {

    private lateinit var binding: FragmentImportJsonBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var uri: Uri? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ImportJsonViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImportJsonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val rxPermissions = RxPermissions(this)
        binding.button.setOnClickListener {
            rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) {
                        performFileSearch()
                    } else {
                        showMessage(getString(R.string.permission_required))
                    }
                }
        }

        viewModel.importWalletCallback.observe(this, Observer {
            it?.let { state ->
                showProgress(state == ImportWalletState.Loading)
                when (state) {
                    is ImportWalletState.Success -> {
                        showMessageLong(state.wallet.address)
                    }
                    is ImportWalletState.ShowError -> {
                        showMessage(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        binding.btnImportWallet.setOnClickListener {
            uri?.let {
                viewModel.importFromJson(
                    it,
                    edtPassword.text?.trim().toString(),
                    edtWalletName.text?.trim().toString()
                )
            }
        }
    }

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivityForResult(
            Intent.createChooser(intent, "Select your backup file"),
            READ_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when (requestCode) {
            READ_REQUEST_CODE -> {
                resultData?.data?.also { uri ->
                    this.uri = uri
                    binding.btnImportWallet.isEnabled = true
                }
            }

        }
    }


    companion object {
        private const val READ_REQUEST_CODE = 1
        fun newInstance() =
            ImportJsonFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }


}
