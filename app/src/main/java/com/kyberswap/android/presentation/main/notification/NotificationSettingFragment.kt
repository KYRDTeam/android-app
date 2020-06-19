package com.kyberswap.android.presentation.main.notification

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentNotificationSettingBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.ENABLE_TOKEN
import com.kyberswap.android.util.PRICETRENDING_APPLY_TAPPED
import com.kyberswap.android.util.PRICETRENDING_NOTI_DISABLE
import com.kyberswap.android.util.PRICETRENDING_NOTI_ENABLE
import com.kyberswap.android.util.PRICETRENDING_RESET_TAPPED
import com.kyberswap.android.util.PRICETRENDING_TOKEN
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import io.reactivex.disposables.CompositeDisposable
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

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private var isPriceNotificationEnable: Boolean = false

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(NotificationSettingViewModel::class.java)
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
                        isPriceNotificationEnable = state.priceNoti
                        binding.swOnOff.isChecked = isPriceNotificationEnable
                        binding.isPriceNotificationEnable = isPriceNotificationEnable
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
                showProgress(state == TogglePriceNotificationState.Loading)
                when (state) {
                    is TogglePriceNotificationState.Success -> {
                        isPriceNotificationEnable = state.state
                        binding.isPriceNotificationEnable = isPriceNotificationEnable
                        binding.executePendingBindings()
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
                showProgress(state == UpdateSubscribedTokensNotificationState.Loading)
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

        compositeDisposable.add(binding.swOnOff.checkedChanges().skipInitialValue()
            .subscribe {
                if (it != isPriceNotificationEnable) {
                    viewModel.togglePriceNoti(it)
                    binding.isPriceNotificationEnable = it
                    analytics.logEvent(
                        if (it) PRICETRENDING_NOTI_ENABLE else PRICETRENDING_NOTI_DISABLE,
                        Bundle().createEvent()
                    )
                }
            })

//        binding.swOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
//            viewModel.togglePriceNoti(isChecked)
//            binding.isPriceNotificationEnable = isChecked
//        }

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

            analytics.logEvent(PRICETRENDING_RESET_TAPPED, Bundle().createEvent())

        }

        binding.tvApply.setOnClickListener {
            val subscribedTokens = adapter.getData().filter { it.subscribed }
                .map { it.symbol }
            viewModel.updateSubscribedTokens(subscribedTokens)
            analytics.logEvent(PRICETRENDING_APPLY_TAPPED, Bundle().createEvent())
            subscribedTokens.forEach {
                analytics.logEvent(PRICETRENDING_TOKEN, Bundle().createEvent(ENABLE_TOKEN, it))
            }

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

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
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

