package com.kyberswap.android.presentation.landing


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.kyberswap.android.databinding.FragmentLandingBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import timber.log.Timber
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"


class LandingFragment : BaseFragment() {

    private lateinit var binding: FragmentLandingBinding

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var position: Int = 0


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LandingViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt(ARG_PARAM) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel.landingList[position]
        binding.tvCreateWallet.setOnClickListener {
            dialogHelper.showConfirmation {
                viewModel.createWallet()
    

        viewModel.createWalletCallback.observe(this, Observer {
            it?.let { state ->
                showProgress(state == CreateWalletState.Loading)
                when (state) {
                    is CreateWalletState.Success -> {
                        Timber.e(state.wallet.address)
                        navigator.navigateToLandingPage()
            
                    is CreateWalletState.ShowError -> {
                        Toast.makeText(
                            this.context,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)
    }

    companion object {
        fun newInstance(position: Int) =
            LandingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM, position)
        
    
    }


}
