package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentOrderConfirmBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.USER_CLICK_CANCEL_SUBMIT_ORDER_V1
import com.kyberswap.android.util.USER_CLICK_SUBMIT_ORDER_CONFIRM_V1
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import javax.inject.Inject

class OrderConfirmFragment : BaseFragment(), LoginState {

    private lateinit var binding: FragmentOrderConfirmBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var limitOrder: LocalLimitOrder? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LimitOrderViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        limitOrder = arguments?.getParcelable(LIMIT_ORDER)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.imgBack.setOnClickListener {
            onBackPress()
        }

        binding.order = limitOrder
        binding.executePendingBindings()

//        viewModel.getLimitOrders(wallet)
//        viewModel.getLocalLimitOrderCallback.observe(viewLifecycleOwner, Observer {
//            it?.getContentIfNotHandled()?.let { state ->
//                when (state) {
//                    is GetLocalLimitOrderState.Success -> {
//                        binding.order = state.order
//                        binding.executePendingBindings()
//                    }
//                    is GetLocalLimitOrderState.ShowError -> {
//
//                    }
//                }
//            }
//        })

        binding.imgInfo.setOnClickListener {
            showAlert(
                getString(R.string.limit_order_confirm_info),
                R.drawable.ic_confirm_info
            )
        }

        binding.tvCancel.setOnClickListener {
            onBackPress()
            analytics.logEvent(
                USER_CLICK_CANCEL_SUBMIT_ORDER_V1,
                Bundle().createEvent(binding.order?.displayTokenPair)
            )
        }

        binding.tvContinue.setOnClickListener {
            viewModel.submitOrder(binding.order, wallet)
            analytics.logEvent(
                USER_CLICK_SUBMIT_ORDER_CONFIRM_V1,
                Bundle().createEvent(binding.order?.displayTokenPair)
            )
        }

        viewModel.submitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SubmitOrderState.Loading)
                when (state) {
                    is SubmitOrderState.Success -> {
                        onSubmitOrderSuccess()
                    }
                    is SubmitOrderState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_confirm_info
                        )
                    }
                }
            }
        })
    }

    private fun onSubmitOrderSuccess() {
        showAlertWithoutIcon(
            title = getString(R.string.title_success),
            message = getString(R.string.order_submitted_message)
        )
        onBackPress(true)
    }

    fun onBackPress(isRefresh: Boolean = false) {
        val fm = currentFragment.childFragmentManager
        for (i in fm.backStackEntryCount - 1 downTo 0) {
            val name = fm.getBackStackEntryAt(i).name
            if (name == LimitOrderFragment::class.java.simpleName) {
                val fragmentByTag = fm.findFragmentByTag(tag)
                if (fragmentByTag is LimitOrderFragment) {
                    if (isRefresh) {
                        fragmentByTag.onRefresh()
                    }
                }
                break
            }
            fm.popBackStack()
        }
    }

    override fun getLoginStatus() {
        viewModel.getLoginStatus()
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

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val LIMIT_ORDER = "limit_order_param"
        fun newInstance(wallet: Wallet?, limitOrder: LocalLimitOrder?) =
            OrderConfirmFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putParcelable(LIMIT_ORDER, limitOrder)
                }
            }
    }
}
