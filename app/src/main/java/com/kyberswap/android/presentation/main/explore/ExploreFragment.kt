package com.kyberswap.android.presentation.main.explore

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentExploreBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.UserStatusChangeEvent
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.EXPLORE_ALERT_TAPPED
import com.kyberswap.android.util.EXPLORE_DEFAULT_TAPPED
import com.kyberswap.android.util.EXPLORE_NOTIFICATION_TAPPED
import com.kyberswap.android.util.EXPLORE_PROFILE_TAPPED
import com.kyberswap.android.util.EXPLORE_TRANSACTION_TAPPED
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.openUrl
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class ExploreFragment : BaseFragment() {

    lateinit var binding: FragmentExploreBinding

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

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val handler by lazy { Handler() }

    private var hasUserInfo: Boolean = false

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ExploreViewModel::class.java)
    }

    private var wallet: Wallet? = null

    private val fromLimitOrder: Boolean
        get() = if (activity is MainActivity) (activity as MainActivity).fromLimitOrder else false

    @Inject
    lateinit var analytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getCampaign()
        viewModel.getCampaignStateCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetCampaignsState.Success -> {
                        if (state.campaigns.isEmpty()) {
                            binding.vpCampaign.visibility = View.GONE
                            binding.imgDefault.visibility = View.VISIBLE
                        } else {
                            binding.vpCampaign.visibility = View.VISIBLE
                            binding.imgDefault.visibility = View.GONE
                            binding.vpCampaign.adapter =
                                CampaignPagerAdapter(childFragmentManager, state.campaigns)
                            binding.indicator.setViewPager(binding.vpCampaign)
                        }
                    }
                    is GetCampaignsState.ShowError -> {
                        binding.vpCampaign.visibility = View.GONE
                        binding.imgDefault.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        this.wallet = state.wallet
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        hasUserInfo = state.userInfo != null && state.userInfo.uid > 0
                    }
                    is UserInfoState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        binding.lnAlert.setOnClickListener {
            analytics.logEvent(EXPLORE_ALERT_TAPPED, Bundle().createEvent())
            if (hasUserInfo) {
                navigator.navigateToManageAlert(
                    currentFragment
                )
            } else {
                showAlertWithoutIcon(
                    message = getString(R.string.sign_in_required)
                )
            }
        }

        binding.lnTransaction.setOnClickListener {
            analytics.logEvent(EXPLORE_TRANSACTION_TAPPED, Bundle().createEvent())
            if (wallet != null) {
                navigator.navigateToTransactionScreen(currentFragment, wallet)
            }
        }

        binding.lnNotification.setOnClickListener {
            analytics.logEvent(EXPLORE_NOTIFICATION_TAPPED, Bundle().createEvent())
            navigator.navigateToNotificationScreen(currentFragment)
        }

        binding.lnSignIn.setOnClickListener {
            analytics.logEvent(EXPLORE_PROFILE_TAPPED, Bundle().createEvent())
            if (hasUserInfo) {
                navigator.navigateToProfileDetail(currentFragment, true)
            } else {
                navigator.navigateToProfile(currentFragment)
            }

        }

        binding.imgDefault.setOnClickListener {
            analytics.logEvent(EXPLORE_DEFAULT_TAPPED, Bundle().createEvent())
            openUrl(getString(R.string.kyber_swap_url))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UserStatusChangeEvent) {
        Timber.e("user status change event")
        getLoginStatus()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun getLoginStatus() {
        viewModel.getLoginStatus()
    }

    fun updateView() {
        if (fromLimitOrder) {
            navigator.navigateToProfile(currentFragment)
//            if (activity is MainActivity) (activity as MainActivity).fromLimitOrder = false
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            ExploreFragment()
    }
}
