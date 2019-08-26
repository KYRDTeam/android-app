package com.kyberswap.android.presentation.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.databinding.FragmentLandingBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel.landingList[position]
    }

    companion object {
        fun newInstance(position: Int) =
            LandingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM, position)
        
    
    }
}
