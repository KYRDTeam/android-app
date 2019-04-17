package com.kyberswap.android.presentation.landing


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.R

private const val ARG_PARAM = "arg_param"


class LandingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    companion object {
        fun newInstance() =
                LandingFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }


}
