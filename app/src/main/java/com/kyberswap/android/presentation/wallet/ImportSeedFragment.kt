package com.kyberswap.android.presentation.wallet


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentImportSeedBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.ImportWalletState
import com.kyberswap.android.presentation.listener.addTextChangeListener
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_import_seed.*
import javax.inject.Inject


class ImportSeedFragment : BaseFragment() {

    private lateinit var binding: FragmentImportSeedBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ImportSeedViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImportSeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.edtSeed.addTextChangeListener {
            onTextChanged { s, _, _, _ ->
                val count = s.toString().trim().split(" ").size
                binding.txtWordCounter.text = getString(
                    R.string.text_counter,
                    count
                )
                if (count >= 12) {
                    binding.btnImportWallet.isEnabled = true
        
    

        binding.btnImportWallet.setOnClickListener {
            viewModel.importFromSeed(
                edtSeed.text?.trim().toString(),
                edtWalletName.text?.trim().toString()
            )


        viewModel.importWalletCallback.observe(this, Observer {
            it?.let { state ->
                showProgress(state == ImportWalletState.Loading)
                when (state) {
                    is ImportWalletState.Success -> {
                        showMessageLong(state.wallet.address)
            
                    is ImportWalletState.ShowError -> {
                        showMessage(state.message ?: getString(R.string.something_wrong))
            
        
    
)

    }


    companion object {
        fun newInstance() =
            ImportSeedFragment().apply {
                arguments = Bundle().apply {
        
    
    }


}
