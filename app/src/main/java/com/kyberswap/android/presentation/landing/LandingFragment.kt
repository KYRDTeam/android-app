package com.kyberswap.android.presentation.landing


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLandingBinding

private const val ARG_PARAM = "arg_param"


class LandingFragment : Fragment() {

    private lateinit var binding: FragmentLandingBinding

    private var position: Int = 0

    private val landingList by lazy {
        listOf(
                LandingViewModel(R.drawable.ic_security_check, R.string.landing_1_title, R.string.landing_1_content),
                LandingViewModel(R.drawable.swap, R.string.landing_2_title, R.string.landing_2_content),
                LandingViewModel(R.drawable.profile, R.string.landing_3_title, R.string.landing_3_content))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt(ARG_PARAM) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = landingList[position]
    }

    companion object {
        fun newInstance(position: Int) =
                LandingFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM, position)
                    }
                }
    }


}
