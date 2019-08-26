package com.kyberswap.android.presentation.main.alert


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLeaderBoardBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.alert.GetLeaderBoardState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class LeaderBoardFragment : BaseFragment() {

    private lateinit var binding: FragmentLeaderBoardBinding

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

    private val handler by lazy {
        Handler()
    }

    private var userInfo: UserInfo? = null

    private var isCampaignResult: Boolean? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LeaderBoardViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = arguments?.getParcelable(USER_INFO_PARAM)
        isCampaignResult = arguments?.getBoolean(LATEST_CAMPAIGN_RESULT, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        userInfo?.let {
            if (isCampaignResult == true) {
                binding.title = getString(R.string.campaign_result)
                viewModel.getCampaignResult(it)

     else {
                binding.title = getString(R.string.alerts_leader_board)
                viewModel.getLeaderBoard(it)
    


        binding.isCampaignResult = isCampaignResult
        binding.rvLeaderBoard.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )


        val adapter = LeaderBoardAlertAdapter(appExecutors)

        binding.rvLeaderBoard.adapter = adapter

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetLeaderBoardState.Loading)
                when (state) {
                    is GetLeaderBoardState.Success -> {
                        adapter.submitAlerts(state.alerts)
                        binding.campaign = state.campaignInfo
                        binding.isNoData = state.alerts.isEmpty()
                        binding.lastCampaignTitle = state.lastCampaignTitle
                        binding.lnCampaignInfo.visibility =
                            if (state.campaignInfo.id <= 0 && isCampaignResult != true && state.lastCampaignTitle.isEmpty()) View.GONE else View.VISIBLE
                        binding.executePendingBindings()
            
                    is GetLeaderBoardState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        binding.tvWinner.setOnClickListener {
            navigator.navigateToLeaderBoard(
                currentFragment,
                userInfo,
                true
            )


        binding.flToggle.setOnClickListener {
            binding.expandableLayout.toggle()
            binding.imgToggle.isSelected = !binding.imgToggle.isSelected


        binding.tvEligibleToken.setOnClickListener {
            dialogHelper.showEligibleToken(
                appExecutors,
                binding.campaign?.eligibleTokens?.split(",") ?: listOf()
            )


        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()


    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        private const val USER_INFO_PARAM = "user_info"
        private const val LATEST_CAMPAIGN_RESULT = "latest_campaign_result"
        fun newInstance(userInfo: UserInfo?, isCampaignResult: Boolean = false) =
            LeaderBoardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(USER_INFO_PARAM, userInfo)
                    putBoolean(LATEST_CAMPAIGN_RESULT, isCampaignResult)

        
    
    }


}
