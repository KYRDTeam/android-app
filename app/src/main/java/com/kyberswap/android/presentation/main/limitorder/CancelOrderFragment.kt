package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentCancelOrderBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletChangeEvent
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.USER_CLICK_CANCEL_RELATED_ORDERS
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.openUrl
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_convert.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class CancelOrderFragment : BaseFragment() {

    private lateinit var binding: FragmentCancelOrderBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private var wallet: Wallet? = null

    private var currentOrder: LocalLimitOrder? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LimitOrderV2ViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var dialogHelper: DialogHelper

    var orders = listOf<Order>()

    private var needConvertEthWeth: Boolean = false

    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        currentOrder = arguments?.getParcelable(LIMIT_ORDER_PARAM)
        orders = arguments?.getParcelableArrayList(CANCEL_LIMIT_ORDER_PARAM) ?: listOf()
        needConvertEthWeth = arguments?.getBoolean(NEED_CONVERT_ETH_WETH) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCancelOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.order = currentOrder
        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {

                        if (!state.wallet.isSameWallet(wallet)) {
                            onBackPressed()
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        imgBack.setOnClickListener {
            onBackPressed()
        }

        compositeDisposable.add(
            binding.cbUnderstand.checkedChanges().skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.tvSubmitOrderWarning.isEnabled = it
                    binding.tvSubmitOrderWarning.alpha = if (it) 1.0f else 0.2f
                })

        binding.tvChangeRate.setOnClickListener {
            onBackPressed()
        }

        binding.tvSubmitOrderWarning.setOnClickListener {
            if (needConvertEthWeth) {
                navigator.navigateToConvertFragment(
                    currentFragment,
                    wallet,
                    binding.order
                )
            } else if (binding.order?.type == LocalLimitOrder.TYPE_LIMIT_ORDER_V1) {
                navigator.navigateToOrderConfirmScreen(
                    currentFragment,
                    wallet,
                    binding.order
                )
            } else {
                navigator.navigateToOrderConfirmV2Screen(
                    currentFragment,
                    wallet,
                    binding.order
                )
            }
            analytics.logEvent(
                USER_CLICK_CANCEL_RELATED_ORDERS,
                Bundle().createEvent()
            )

        }

        binding.rvRelatedOrder.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val orderAdapter =
            OrderAdapter(
                appExecutors
                , {

                }, {

                }, {

                }, {
                    dialogHelper.showInvalidatedDialog(it)
                })
        orderAdapter.mode = Attributes.Mode.Single
        orderAdapter.setWarning(true)
        binding.rvRelatedOrder.adapter = orderAdapter
        orderAdapter.submitOrdersList(viewModel.toOrderItems(orders))

        binding.tvWhy.setOnClickListener {
            openUrl(getString(R.string.same_token_pair_url))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: WalletChangeEvent) {
        onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun onBackPressed() {
        activity?.onBackPressed()
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val CANCEL_LIMIT_ORDER_PARAM = "cancel_limit_order_param"
        private const val LIMIT_ORDER_PARAM = "limit_order_param"
        private const val NEED_CONVERT_ETH_WETH = "need_convert_eth_weth"
        fun newInstance(
            wallet: Wallet?,
            orders: ArrayList<Order>,
            currentOrder: LocalLimitOrder?,
            needConvertEthWeth: Boolean
        ) =
            CancelOrderFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putParcelable(LIMIT_ORDER_PARAM, currentOrder)
                    putParcelableArrayList(CANCEL_LIMIT_ORDER_PARAM, orders)
                    putBoolean(NEED_CONVERT_ETH_WETH, needConvertEthWeth)
                }
            }
    }
}
