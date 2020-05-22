package com.kyberswap.android.presentation.main.notification

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentNotificationsBinding
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.domain.model.UserStatusChangeEvent
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.limitorder.LimitOrderFragment
import com.kyberswap.android.presentation.main.limitorder.LimitOrderV2Fragment
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.openUrl
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class NotificationFragment : BaseFragment() {

    private lateinit var binding: FragmentNotificationsBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val handler by lazy {
        Handler()
    }

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(NotificationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isLoggedIn: Boolean = false

    private var hasUnReadNotification: Boolean = false

    private var isPriceNotificationEnable: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.let {
            binding.swipeLayout.setColorSchemeColors(
                ContextCompat.getColor(
                    it,
                    R.color.colorAccent
                )
            )
        }
        binding.hasUnReadNotification = hasUnReadNotification
        val notificationAdapter = NotificationAdapter(appExecutors) {
            if (!it.read) {
                if (isLoggedIn) {
                    viewModel.readNotification(it)
                }
            }

            val act = activity
            if (act is MainActivity) {
                when (it.label) {
                    Notification.TYPE_LIMIT_ORDER -> {
                        act.moveToTab(MainPagerAdapter.LIMIT_ORDER)
                        handler.post {
                            val currentFragment = act.getCurrentFragment()
                            if (currentFragment is LimitOrderFragment) {
                                currentFragment.showFillOrder(NotificationLimitOrder(it.data))
                            } else if (currentFragment is LimitOrderV2Fragment) {
                                currentFragment.showFillOrder(NotificationLimitOrder(it.data))
                            }
                        }
                    }
                    Notification.TYPE_ALERT, Notification.TYPE_BIG_SWING, Notification.TYPE_NEW_LISTING -> {
                        act.moveToTab(MainPagerAdapter.SWAP)
                        handler.post {
                            val currentFragment = act.getCurrentFragment()
                            if (currentFragment is SwapFragment) {
                                currentFragment.newSwap(it.data)
                            }
                        }
                    }

                    else -> {
                        dialogHelper.showPromotionDialog(it) {
                            openUrl(it)
                        }
                    }
                }
            }
        }

        viewModel.getLoginStatus()
        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->

                when (state) {
                    is UserInfoState.Success -> {
                        val userId = state.userInfo?.uid ?: 0
                        isPriceNotificationEnable = state.userInfo?.priceNoti == true
                        if (isLoggedIn != (userId > 0)) {
                            isLoggedIn = userId > 0
                            hasUnReadNotification =
                                isLoggedIn && notificationAdapter.getData().any { !it.read }
                            binding.hasUnReadNotification = hasUnReadNotification
                            refresh()
                        }
                    }
                    is UserInfoState.ShowError -> {
                        isLoggedIn = false
                    }
                }
            }
        })

        refresh()

        binding.tvSetting.setOnClickListener {
            if (isLoggedIn) {
                navigator.navigateToNotificationSettingScreen(
                    currentFragment,
                    isPriceNotificationEnable
                )
            } else {
                showAlertWithoutIcon(
                    message = getString(R.string.sign_in_requried_notification_setting)
                )
            }
        }


        binding.rvNotifications.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        binding.rvNotifications.adapter = notificationAdapter

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvReadAll.setOnClickListener {
            viewModel.readAll(notificationAdapter.getData())
        }

        viewModel.getNotificationsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetNotificationsState.Loading)
                when (state) {
                    is GetNotificationsState.Success -> {
                        notificationAdapter.submitList(state.notifications)
                        if (!isLoggedIn) {
                            notificationAdapter.readAll()
                        }
                        hasUnReadNotification = isLoggedIn && state.notifications.any { !it.read }
                        binding.hasUnReadNotification = hasUnReadNotification
                        binding.isNoData = state.notifications.isEmpty()
//                        updateReadAllView(state.notifications.any { !it.read })
                    }
                    is GetNotificationsState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.readNotificationsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is ReadNotificationsState.Success -> {
                        notificationAdapter.updateReadItem(state.notification)
                        if (state.isReadAll) {
                            updateReadAllView(false)
                            updateNotificationIndicator()
                        }
                    }
                    is ReadNotificationsState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        binding.swipeLayout.setOnRefreshListener {
            refresh()
        }
    }

    private fun updateNotificationIndicator() {
        if (activity is MainActivity) {
            (activity as MainActivity).markReadAllNotification()
        }
    }

    private fun updateReadAllView(isShowReadAll: Boolean) {
        if (isShowReadAll) {
            binding.tvReadAll.visibility = View.VISIBLE
            binding.tvHorizontalDevider.visibility = View.VISIBLE
        } else {
            binding.tvReadAll.visibility = View.GONE
            binding.tvHorizontalDevider.visibility = View.GONE
        }
    }

    private fun setUpNotificationSetting(isPriceNotificationEnable: Boolean = false) {
        binding.tvSetting.setOnClickListener {
            if (isLoggedIn) {
                navigator.navigateToNotificationSettingScreen(
                    currentFragment,
                    isPriceNotificationEnable
                )
            } else {
                showAlertWithoutIcon(
                    message = getString(R.string.sign_in_requried_notification_setting)
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UserStatusChangeEvent) {
        isLoggedIn = false
        viewModel.getNotifications()
    }

    override fun showProgress(showProgress: Boolean) {
        binding.swipeLayout.isRefreshing = showProgress
    }

    private fun refresh() {
        viewModel.getNotifications()
    }

    companion object {
        fun newInstance() =
            NotificationFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

