package com.kyberswap.android.presentation.main.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.kyberswap.android.databinding.FragmentCampaignBinding
import com.kyberswap.android.domain.model.Campaign
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"

class CampaignFragment : BaseFragment() {

    private lateinit var binding: FragmentCampaignBinding

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var campaign: Campaign? = null

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(CampaignViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        campaign = arguments?.getParcelable(ARG_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCampaignBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.campaign = campaign
        binding.executePendingBindings()
    }

    companion object {
        fun newInstance(campaign: Campaign) =
            CampaignFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM, campaign)
                }
            }
    }
}
