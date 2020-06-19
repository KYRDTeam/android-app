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
import com.kyberswap.android.databinding.FragmentOrderConfirmV2Binding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.CURRENT_RATE
import com.kyberswap.android.util.DES_AMOUNT
import com.kyberswap.android.util.ERROR
import com.kyberswap.android.util.FEE
import com.kyberswap.android.util.LOCONFIRM_CANCEL
import com.kyberswap.android.util.LOCONFIRM_ERROR
import com.kyberswap.android.util.LOCONFIRM_ORDER_FAILED
import com.kyberswap.android.util.LOCONFIRM_ORDER_SUCCESS
import com.kyberswap.android.util.SRC_AMOUNT
import com.kyberswap.android.util.TARGET_RATE
import com.kyberswap.android.util.TOKEN_PAIR
import com.kyberswap.android.util.USER_CLICK_SUBMIT_ORDER_CONFIRM
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import javax.inject.Inject

class OrderConfirmV2Fragment : BaseFragment(), LoginState {

    private lateinit var binding: FragmentOrderConfirmV2Binding

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
        binding = FragmentOrderConfirmV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
            analytics.logEvent(
                LOCONFIRM_CANCEL,
                Bundle().createEvent(binding.order?.displayTokenPair)
            )
        }

        binding.order = limitOrder
        binding.executePendingBindings()

        binding.tvCancel.setOnClickListener {
            onBackPress()
            analytics.logEvent(
                LOCONFIRM_CANCEL,
                Bundle().createEvent(binding.order?.displayTokenPair)
            )
        }

        binding.tvContinue.setOnClickListener {
            viewModel.submitOrder(binding.order, wallet)
            analytics.logEvent(
                USER_CLICK_SUBMIT_ORDER_CONFIRM,
                Bundle().createEvent(binding.order?.displayTokenPair)
            )
        }

        viewModel.submitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SubmitOrderState.Loading)
                when (state) {
                    is SubmitOrderState.Success -> {
                        onSubmitOrderSuccess()
                        analytics.logEvent(
                            LOCONFIRM_ORDER_SUCCESS,
                            Bundle().createEvent(
                                listOf(
                                    TOKEN_PAIR,
                                    CURRENT_RATE,
                                    TARGET_RATE,
                                    SRC_AMOUNT,
                                    DES_AMOUNT,
                                    FEE
                                ),
                                listOf(
                                    binding.order?.pair,
                                    binding.order?.marketRate,
                                    binding.order?.price,
                                    binding.order?.displayAmount,
                                    binding.order?.displayTotal,
                                    binding.order?.displayedFeeV2
                                )
                            )
                        )
                    }
                    is SubmitOrderState.ShowError -> {
                        val error = state.message ?: getString(R.string.something_wrong)
                        showAlert(
                            error,
                            R.drawable.ic_confirm_info
                        )
                        analytics.logEvent(
                            LOCONFIRM_ORDER_FAILED,
                            Bundle().createEvent(
                                listOf(
                                    TOKEN_PAIR,
                                    CURRENT_RATE,
                                    TARGET_RATE,
                                    SRC_AMOUNT,
                                    DES_AMOUNT,
                                    FEE,
                                    ERROR
                                ),
                                listOf(
                                    binding.order?.pair,
                                    binding.order?.marketRate,
                                    binding.order?.price,
                                    binding.order?.displayAmount,
                                    binding.order?.displayTotal,
                                    binding.order?.displayedFeeV2,
                                    error
                                )
                            )
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
        val fm = currentFragment.childFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
        if (currentFragment is LimitOrderV2Fragment) {
            (currentFragment as LimitOrderV2Fragment).apply {
                resetUI()
                refresh()
            }
        }
    }

    fun onBackPress() {
        val fm = currentFragment.childFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
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
                        analytics.logEvent(LOCONFIRM_ERROR, Bundle().createEvent())
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
            OrderConfirmV2Fragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putParcelable(LIMIT_ORDER, limitOrder)
                }
            }
    }
}
