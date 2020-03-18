package com.kyberswap.android.presentation.main.notification

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentNotificationSettingBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class NotificationSettingFragment : BaseFragment(), LoginState {

    private lateinit var binding: FragmentNotificationSettingBinding

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

    private var isPriceNotificationEnable: Boolean = false

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(NotificationSettingViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPriceNotificationEnable =
            arguments?.getBoolean(IS_PRICE_NOTIFICATION_ENABLE, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.isPriceNotificationEnable = isPriceNotificationEnable
        viewModel.getSubscriptionTokens()
        binding.rvToken.layoutManager = GridLayoutManager(
            activity,
            4
        )

        val adapter = SubscribedTokenAdapter(appExecutors)
        binding.rvToken.adapter = adapter

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
        viewModel.getSubscribedNotificationsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSubscriptionNotificationState.Success -> {
                        adapter.submitFilterList(state.notifications)
                        if (state.notifications.filter { it.subscribed }.size > state.notifications.size / 2) {
                            binding.tvSelectAll.text =
                                getString(R.string.filter_deselect_all)
                        } else {
                            binding.tvSelectAll.text = getString(R.string.filter_select_all)
                        }
                    }

                    is GetSubscriptionNotificationState.ShowError -> {
                        if (state.message != null) {
                            showError(state.message)
                        }
                    }
                }
            }
        })

        viewModel.togglePriceNotificationsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is TogglePriceNotificationState.Success -> {

                    }

                    is TogglePriceNotificationState.ShowError -> {
                        val currentValue = binding.swOnOff.isChecked
                        binding.swOnOff.isChecked = !currentValue
                        binding.isPriceNotificationEnable = !currentValue
                        if (state.message != null) {
                            showError(state.message)
                        }
                    }
                }
            }
        })


        viewModel.updateSubscribedTokensNotificationsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UpdateSubscribedTokensNotificationState.Success -> {
                        if (activity != null) {
                            activity?.onBackPressed()
                        }
                    }

                    is UpdateSubscribedTokensNotificationState.ShowError -> {
                        if (state.message != null) {
                            showError(state.message)
                        }
                    }
                }
            }
        })

        binding.swOnOff.isChecked = isPriceNotificationEnable

        binding.swOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.togglePriceNoti(isChecked)
            binding.isPriceNotificationEnable = isChecked
        }

        binding.tvSelectAll.setOnClickListener {
            adapter.reset(isSelectAll)
            toggleSelectAll()
        }

        binding.tvReset.setOnClickListener {
            adapter.submitFilterList(adapter.getData().map {
                it.copy(subscribed = true)
            })
            binding.tvSelectAll.text =
                getString(R.string.filter_deselect_all)

        }

        binding.tvApply.setOnClickListener {
            viewModel.updateSubscribedTokens(adapter.getData().filter { it.subscribed }
                .map { it.symbol })
        }

        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        if (!(state.userInfo != null && state.userInfo.uid > 0)) {
                            activity?.onBackPressed()
                        }
                    }
                    is UserInfoState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private val isSelectAll: Boolean
        get() = binding.tvSelectAll.text == getString(R.string.filter_select_all)

    private fun toggleSelectAll() {
        if (isSelectAll) {
            binding.tvSelectAll.text = getString(R.string.filter_deselect_all)
        } else {
            binding.tvSelectAll.text = getString(R.string.filter_select_all)
        }
    }

    companion object {
        private const val IS_PRICE_NOTIFICATION_ENABLE = "is_price_notification_enable"
        fun newInstance(isPriceNotificationEnable: Boolean) =
            NotificationSettingFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(IS_PRICE_NOTIFICATION_ENABLE, isPriceNotificationEnable)
                }
            }
    }

    override fun getLoginStatus() {
        viewModel.getLoginStatus()
    }
}

