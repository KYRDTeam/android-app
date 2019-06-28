package com.kyberswap.android.presentation.main.profile.kyc


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentPersonalInfoBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class PersonalInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentPersonalInfoBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val handler by lazy {
        Handler()
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PersonalInfoViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.tvNext.setOnClickListener {
            navigator.navigateToPassport(currentFragment)
        }

    }

    companion object {
        fun newInstance() =
            PersonalInfoFragment()
    }


}
