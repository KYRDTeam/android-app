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
import com.kyberswap.android.databinding.FragmentImportPrivateKeyBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.ImportWalletState
import com.kyberswap.android.presentation.listener.addTextChangeListener
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class ImportPrivateKeyFragment : BaseFragment() {

    private lateinit var binding: FragmentImportPrivateKeyBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ImportPrivateKeyViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImportPrivateKeyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.edtPrivateKey.addTextChangeListener {
            onTextChanged { s, _, _, _ ->
                val count = s.toString().trim().split(" ").size
                binding.btnImportWallet.isEnabled = count > 0
    


        binding.btnImportWallet.setOnClickListener {
            viewModel.importFromPrivateKey(
                binding.edtPrivateKey.text?.trim().toString(),
                if (binding.edtWalletName.text.isNotEmpty()) binding.edtWalletName.text.toString()
                else getString(R.string.default_wallet_name)
            )


        viewModel.importWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.let { state ->
                showProgress(state == ImportWalletState.Loading)
                when (state) {
                    is ImportWalletState.Success -> {
                        navigator.navigateToHome(state.wallet)
            
                    is ImportWalletState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.imgQR.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showAlert(getString(R.string.message_cancelled))
     else {
                binding.edtPrivateKey.setText(result.contents.toString())
    
 else {
            super.onActivityResult(requestCode, resultCode, data)

    }

    companion object {
        fun newInstance() =
            ImportPrivateKeyFragment().apply {
                arguments = Bundle().apply {
        
    
    }
}
