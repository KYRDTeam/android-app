package com.kyberswap.android.presentation.main.profile.kyc


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.databinding.FragmentVerificationBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import javax.inject.Inject


class VerificationFragment : BaseFragment() {

    private lateinit var binding: FragmentVerificationBinding

    @Inject
    lateinit var navigator: Navigator

    private val handler by lazy {
        Handler()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }


        handler.postDelayed({
            onBackPress()
        }, 3000)
    }

    private fun onBackPress() {
        val fm = (activity as MainActivity).getCurrentFragment()?.childFragmentManager
        if (fm != null)
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            VerificationFragment()
    }


}
