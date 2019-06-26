package com.kyberswap.android.presentation.main.alert


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentProfileBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class PriceAlertFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PriceAlertViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    companion object {
        fun newInstance() =
            PriceAlertFragment()
    }


}
