package com.kyberswap.android.presentation.main.balance.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentOtherBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class OtherFragment : BaseFragment() {

    private lateinit var binding: FragmentOtherBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

//    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(OtherViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtherBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance() = OtherFragment()
    }
}
