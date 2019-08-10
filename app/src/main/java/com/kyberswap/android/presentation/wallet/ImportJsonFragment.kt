package com.kyberswap.android.presentation.wallet

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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

    private var fromMain: Boolean = false

    private val defaultName by lazy {
        getString(R.string.import_your_json_file)
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
             else {
                        showAlertWithoutIcon(message = getString(R.string.permission_required))
            
        


        viewModel.importWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.let { state ->
                showProgress(state == ImportWalletState.Loading)
                when (state) {
                    is ImportWalletState.Success -> {

                        showAlert(getString(R.string.import_wallet_success)) {
                            if (fromMain) {
                                activity?.onBackPressed()
                     else {
                                navigator.navigateToHome()
                    

                
            
                    is ImportWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        binding.btnImportWallet.setOnClickListener {
            uri?.let {
                viewModel.importFromJson(
                    it,
                    edtPassword.text?.trim().toString(),
                    if (edtWalletName.text.isNotEmpty()) edtWalletName.text.trim().toString()
                    else getString(R.string.default_wallet_name)
                )
    

    }

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION


        startActivityForResult(
            Intent.createChooser(intent, "Select your backup file"),
            READ_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            READ_REQUEST_CODE -> {
                data?.data?.also { uri ->
                    this.uri = uri
                    binding.button.text = queryName(uri)
                    binding.btnImportWallet.isEnabled = true
        
    

    }

    private fun queryName(uri: Uri): String {
        val returnCursor = context?.contentResolver?.query(
            uri,
            null,
            null,
            null,
            null
        )!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }


    companion object {
        private const val FROM_MAIN_PARAM = "from_main_param"
        private const val READ_REQUEST_CODE = 1
        fun newInstance(fromMain: Boolean) =
            ImportJsonFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(FROM_MAIN_PARAM, fromMain)
        
    
    }
}
