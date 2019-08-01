package com.kyberswap.android.presentation.main.limitorder

import android.animation.ObjectAnimator
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.*
import kotlinx.android.synthetic.main.fragment_limit_order.*
import kotlinx.android.synthetic.main.fragment_swap.edtDest
import kotlinx.android.synthetic.main.fragment_swap.edtSource
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


class LimitOrderFragment : BaseFragment(), PendingTransactionNotification, LoginState {

    private lateinit var binding: FragmentLimitOrderBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var pendingBalances: PendingBalances? = null


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LimitOrderViewModel::class.java)
    }

    private val handler by lazy { Handler() }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    var hasUserFocus: Boolean? = false


    private var userInfo: UserInfo? = null

    private var notification: NotificationLimitOrder? = null

    private val srcAmount: String
        get() = binding.edtSource.text.toString()

    private val dstAmount: String
        get() = binding.edtDest.text.toString()

    private val rateText: String
        get() = binding.edtRate.text.toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notification = arguments?.getParcelable(NOTIFICATION_PARAM)
    }

    private val marketRate: String?
        get() = binding.order?.getExpectedDestAmount(BigDecimal.ONE)?.toDisplayNumber()

    private val revertedMarketRate: String?
        get() = if (marketRate.toDoubleOrDefaultZero() == 0.0) "0" else 1.0.div(
            marketRate.toDoubleOrDefaultZero()
        ).toBigDecimal().toDisplayNumber()

    private val tokenSourceSymbol: String?
        get() = binding.order?.tokenSource?.tokenSymbol

    private val tokenDestSymbol: String?
        get() = binding.order?.tokenDest?.tokenSymbol

    private val rate: String?
        get() = binding.order?.combineRate

    private val hasRelatedOrder: Boolean
        get() = viewModel.relatedOrders.any {
            it.src == binding.order?.tokenSource?.symbol && it.dst == binding.order?.tokenDest?.symbol && it.userAddr == wallet?.address
        }
    private val sourceLock = AtomicBoolean()
    private val destLock = AtomicBoolean()

    private var orderAdapter: OrderAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLimitOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getSelectedWallet()
        notification?.let {
            dialogHelper.showOrderFillDialog(it) { url ->
                openUrl(getString(R.string.transaction_etherscan_endpoint_url) + url)
            }
        }
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        binding.walletName = state.wallet.name
                        if (!state.wallet.isSameWallet(wallet)) {
                            wallet = state.wallet
                            viewModel.getLimitOrders(wallet)
                            viewModel.getPendingBalances(wallet)
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getLoginStatus()

        viewModel.getLocalLimitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetLocalLimitOrderState.Success -> {
                        if (!state.order.isSameTokenPairForAddress(binding.order)) {
                            hasUserFocus = false
                            if (state.order.tokenSource.tokenSymbol == state.order.tokenDest.tokenSymbol) {
                                showError(getString(R.string.same_token_alert))
                            }
                            if (state.order.hasSamePair) {
                                edtRate.setText("1")
                            }

                            binding.order = state.order
                            binding.isQuote = state.order.tokenSource.isQuote
                            binding.executePendingBindings()
                            getPendingBalance()
                            viewModel.getFee(
                                binding.order,
                                srcAmount,
                                dstAmount,
                                wallet
                            )

                            edtSource.setAmount(state.order.srcAmount)
                            getRate(state.order)
                            viewModel.getGasPrice()
                            viewModel.getGasLimit(wallet, binding.order)
                        }


                    }
                    is GetLocalLimitOrderState.ShowError -> {

                    }
                }
            }
        })

        listOf(binding.imgTokenSource, binding.tvSource).forEach {
            it.setOnClickListener {
                hideKeyboard()
                navigator.navigateToTokenSearchFromLimitOrder(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet,
                    true
                )
            }
        }

        listOf(binding.imgTokenDest, binding.tvDest).forEach {
            it.setOnClickListener {
                hideKeyboard()
                navigator.navigateToTokenSearchFromLimitOrder(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet,
                    false
                )
            }
        }

        binding.imgMenu.setOnClickListener {
            showDrawer(true)
        }

        binding.tvBalance.setOnClickListener {
            binding.order?.let {
                if (it.tokenSource.isETHWETH) {
                    binding.edtSource.setAmount(
                        it.availableAmountForTransfer(
                            tvBalance.toBigDecimalOrDefaultZero(),
                            it.gasPrice.toBigDecimalOrDefaultZero()
                        ).toDisplayNumber()
                    )
                } else {
                    binding.edtSource.setAmount(tvBalance.text.toString())
                }

            }
        }

        binding.tv25Percent.setOnClickListener {
            binding.edtSource.setAmount(
                tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.25.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv50Percent.setOnClickListener {
            binding.edtSource.setAmount(
                tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.5.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv100Percent.setOnClickListener {

            binding.order?.let {
                if (it.tokenSource.isETHWETH) {
                    binding.edtSource.setAmount(
                        it.availableAmountForTransfer(
                            tvBalance.toBigDecimalOrDefaultZero(),
                            it.gasPrice.toBigDecimalOrDefaultZero()
                        ).toDisplayNumber()
                    )
                } else {
                    binding.edtSource.setAmount(tvBalance.text.toString())
                }

            }
        }

        binding.tvRate.setOnClickListener {
            binding.edtRate.setText(marketRate)
            binding.tvRateWarning.text = ""
        }

        binding.tvCurrentRate.setOnClickListener {
            binding.edtRate.setText(marketRate)
            binding.tvRateWarning.text = ""
        }

        binding.imgSwap.setOnClickListener {
            hasUserFocus = false
            resetAmount()
            val limitOrder = binding.order?.swapToken()
            limitOrder?.let {
                getRate(it)
                viewModel.getFee(
                    it,
                    srcAmount,
                    dstAmount,
                    wallet
                )
                viewModel.saveLimitOrder(it)
                binding.order = limitOrder
                binding.isQuote = limitOrder.tokenSource.isQuote
                binding.executePendingBindings()
                viewModel.getGasPrice()
                viewModel.getGasLimit(wallet, it)
                updateAvailableAmount(pendingBalances)
            }

            hasUserFocus = false

        }

        binding.rvRelatedOrder.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        if (orderAdapter == null) {
            orderAdapter =
                OrderAdapter(
                    appExecutors
                    , {

                        dialogHelper.showCancelOrder(it) {
                            viewModel.cancelOrder(it)
                        }
                    }, {

                    })
        }
        orderAdapter?.mode = Attributes.Mode.Single
        binding.rvRelatedOrder.adapter = orderAdapter

        viewModel.getRelatedOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetRelatedOrdersState.Success -> {
                        if (binding.isWarning != true) {
                            orderAdapter?.submitList(listOf())
                            orderAdapter?.submitList(state.orders)

                            binding.tvRelatedOrder.visibility =
                                if (hasRelatedOrder) View.VISIBLE else View.GONE
                        }
                    }
                    is GetRelatedOrdersState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.imgInfo.setOnClickListener {
            showAlert(
                getString(R.string.token_eth_star_name),
                R.drawable.ic_confirm_info
            )
        }

        viewModel.getGetNonceStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetNonceState.Success -> {
                        val order = binding.order?.copy(nonce = state.nonce)
                        binding.order = order

                    }
                    is GetNonceState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.tvManageOrder.setOnClickListener {
            when {
                userInfo == null || userInfo!!.uid <= 0 -> {
                    moveToLoginTab()
                    showAlertWithoutIcon(
                        title = getString(R.string.sign_in_required_title), message = getString(
                            R.string.sign_in_to_use_limit_order_feature
                        )
                    )
                }

                else -> {
                    hideKeyboard()
                    navigator.navigateToManageOrder(
                        currentFragment,
                        wallet
                    )
                }
            }


        }

        viewModel.compositeDisposable.add(binding.edtRate.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    hasUserFocus = it
                }
            })

        viewModel.getGetMarketRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {

                        val order = binding.order?.copy(
                            marketRate = state.rate
                        )
                        if (order?.isSameTokenPair(binding.order) != true) {

                            binding.order = order
                            binding.executePendingBindings()
                        }

                        binding.tvRate.text = String.format(
                            getString(R.string.limit_order_current_rate),
                            tokenSourceSymbol,
                            "$marketRate $tokenDestSymbol"
                        )
                        binding.tvRateRevert.text = String.format(
                            getString(R.string.limit_order_current_rate),
                            tokenDestSymbol,
                            "$revertedMarketRate $tokenSourceSymbol"
                        )

                        if (hasUserFocus != true) {
                            binding.edtRate.setAmount(order?.displayMarketRate)
                        }

                    }
                    is GetMarketRateState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.compositeDisposable.add(binding.edtSource.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                sourceLock.set(it)
            })

        viewModel.compositeDisposable.add(binding.edtDest.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                destLock.set(it)
            })

        viewModel.compositeDisposable.add(binding.edtDest.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                if (destLock.get()) {
                    if (rateText.toBigDecimalOrDefaultZero() == BigDecimal.ZERO) {
                        binding.edtSource.setText("")
                    } else {

                        binding.edtSource.setAmount(
                            text.toBigDecimalOrDefaultZero()
                                .divide(
                                    rateText.toBigDecimalOrDefaultZero(),
                                    18,
                                    RoundingMode.CEILING
                                ).toDisplayNumber()
                        )
                    }
                }

            })

        viewModel.compositeDisposable.add(binding.edtSource.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->

                if (!sourceLock.get()) return@subscribe

                if (text.isNullOrEmpty()) {
                    binding.edtDest.setText("")
                }

                binding.order?.let { order ->
                    if (rateText.isEmpty()) {
                        edtDest.setAmount(
                            order.getExpectedDestAmount(
                                text.toString()
                                    .toBigDecimalOrDefaultZero()
                            )
                                .toDisplayNumber()
                        )


                        viewModel.getExpectedRate(
                            order,
                            if (text.isNullOrEmpty()) getString(R.string.default_source_amount) else text.toString()
                        )

                    } else {
                        edtDest.setAmount(
                            text.toBigDecimalOrDefaultZero().multiply(
                                rateText.toBigDecimalOrDefaultZero()
                            ).toDisplayNumber()
                        )

                    }


                    viewModel.getFee(
                        binding.order,
                        srcAmount,
                        dstAmount,
                        wallet
                    )

                    wallet?.let { wallet ->

                        val currentOrder = binding.order?.copy(
                            srcAmount = text.toString()
                        )

                        currentOrder?.let {
                            viewModel.getGasLimit(
                                wallet, it
                            )
                        }

                    }
                }
            })

        viewModel.compositeDisposable.add(
            binding.edtRate.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->
                    binding.tvRateWarning.colorRate(text.toString().percentage(rate))
                    if (text.isNullOrEmpty()) {
                        binding.edtDest.setText("")
                    }

                    binding.order?.let { order ->
                        edtDest.setAmount(
                            order.getExpectedDestAmount(
                                text.toString().toBigDecimalOrDefaultZero(),
                                srcAmount.toBigDecimalOrDefaultZero()
                            ).toDisplayNumber()
                        )

                        val bindingOrder = binding.order?.copy(
                            srcAmount = srcAmount,
                            minRate = edtRate.toBigDecimalOrDefaultZero()
                        )

                        bindingOrder?.let {
                            if (binding.order != bindingOrder) {
                                binding.order = bindingOrder
                                binding.executePendingBindings()
                            }
                        }
                    }
                })

        viewModel.getExpectedRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetExpectedRateState.Success -> {
                        val order = binding.order?.copy(
                            expectedRate = state.list[0]
                        )
                        if (order?.isSameTokenPair(binding.order) != true) {
                            binding.order = order
                            binding.executePendingBindings()
                        }

                        binding.tvRate.text = String.format(
                            getString(R.string.limit_order_current_rate),
                            tokenSourceSymbol,
                            "$marketRate $tokenDestSymbol"
                        )

                        binding.tvRateRevert.text = String.format(
                            getString(R.string.limit_order_current_rate),
                            tokenDestSymbol,
                            "$revertedMarketRate $tokenSourceSymbol"
                        )

                        if (rateText.isEmpty()) {
                            binding.edtRate.setAmount(rate)

                        }

                        if (rateText.isEmpty()) {
                            binding.edtDest.setAmount(
                                binding.order?.getExpectedDestAmount(srcAmount.toBigDecimalOrDefaultZero())?.toDisplayNumber()
                            )
                        } else {
                            binding.edtDest.setAmount(
                                binding.order?.getExpectedDestAmount(
                                    rateText.toBigDecimalOrDefaultZero(),
                                    srcAmount.toBigDecimalOrDefaultZero()
                                )?.toDisplayNumber()
                            )
                        }
                    }
                    is GetExpectedRateState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.getGetGasLimitCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasLimitState.Success -> {
                        val order = binding.order?.copy(
                            gasLimit = state.gasLimit
                        )

                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                        }
                    }
                    is GetGasLimitState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val order = binding.order?.copy(
                            gasPrice = state.gas.fast
                        )
                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                        }
                    }
                    is GetGasPriceState.ShowError -> {

                    }
                }
            }
        })

        viewModel.compositeDisposable.add(
            binding.cbUnderstand.checkedChanges().skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.tvSubmitOrderWarning.isEnabled = it
                    binding.tvSubmitOrderWarning.alpha = if (it) 1.0f else 0.2f
                })

        binding.tvChangeRate.setOnClickListener {
            orderAdapter?.submitList(viewModel.toOrderItems(viewModel.relatedOrders))
            playAnimation(false)
            binding.edtRate.requestFocus()
            binding.edtRate.setSelection(binding.edtRate.text.length)
        }

        binding.tvSubmitOrderWarning.setOnClickListener {
            setWarning(false)
            saveLimitOrder()
        }

        viewModel.getFeeCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showLoading(state == GetFeeState.Loading)
                when (state) {
                    is GetFeeState.Success -> {

                        binding.hasDiscount =
                            state.fee.discountPercent > 0 && srcAmount.toBigDecimalOrDefaultZero().times(
                                state.fee.fee.toBigDecimal()
                            ) > BigDecimal.ZERO


                        binding.tvFee.text = String.format(
                            getString(R.string.limit_order_fee),
                            srcAmount.toBigDecimalOrDefaultZero().times(state.fee.fee.toBigDecimal()).toDisplayNumber().exactAmount(),
                            tokenSourceSymbol
                        )

                        binding.tvFeeNoDiscount.text = String.format(
                            getString(R.string.limit_order_fee_no_discount),
                            srcAmount.toBigDecimalOrDefaultZero().times(state.fee.fee.toBigDecimal()).toDisplayNumber().exactAmount(),
                            tokenSourceSymbol
                        )

                        binding.tvOriginalFee.text = String.format(
                            getString(R.string.limit_order_fee),
                            srcAmount.toBigDecimalOrDefaultZero().times(state.fee.nonDiscountedFee.toBigDecimal()).toDisplayNumber().exactAmount(),
                            tokenSourceSymbol
                        )

                        binding.tvOff.text = String.format(
                            getString(R.string.discount_fee), state.fee.discountPercent
                        )
                        val order = binding.order?.copy(fee = state.fee.fee.toBigDecimal())
                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                        }
                    }
                    is GetFeeState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })



        binding.tvSubmitOrder.setOnClickListener {
            val warningOrderList = viewModel.warningOrderList(
                binding.edtRate.toBigDecimalOrDefaultZero(),
                orderAdapter?.orderList ?: listOf()
            )

            when {
                srcAmount.isEmpty() -> {
                    showError(getString(R.string.specify_amount))
                }
                srcAmount.toBigDecimalOrDefaultZero() >
                    viewModel.calAvailableAmount(
                        binding.order?.tokenSource,
                        pendingBalances
                    ).toBigDecimalOrDefaultZero() -> {
                    showError(getString(R.string.exceed_balance))
                }
                binding.order?.hasSamePair == true -> showAlert(getString(R.string.same_token_alert))
                binding.order?.amountTooSmall(srcAmount) == true -> {
                    showError(getString(R.string.swap_amount_small))
                }

                userInfo == null || userInfo!!.uid <= 0 -> {
                    moveToLoginTab()
                    showAlertWithoutIcon(
                        title = getString(R.string.sign_in_required_title), message = getString(
                            R.string.sign_in_to_use_limit_order_feature
                        )
                    )
                }

                warningOrderList.isNotEmpty() -> {
                    orderAdapter?.submitList(
                        viewModel.toOrderItems(
                            warningOrderList
                        )
                    )

                    playAnimation(true)

                }

                else -> binding.order?.let {

                    saveLimitOrder()
                }
            }
        }

        viewModel.saveOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveLimitOrderState.Success -> {
                        when {
                            viewModel.needConvertWETH(
                                binding.order,
                                pendingBalances
                            ) -> {
                                hideKeyboard()
                                navigator.navigateToConvertFragment(
                                    currentFragment,
                                    wallet,
                                    binding.order
                                )
                            }
                            else -> {
                                hideKeyboard()
                                navigator.navigateToOrderConfirmScreen(
                                    currentFragment,
                                    wallet,
                                    binding.order
                                )
                            }
                        }
                    }
                    is SaveLimitOrderState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }

                }
            }
        })

        viewModel.cancelOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CancelOrdersState.Loading)
                when (state) {
                    is CancelOrdersState.Success -> {
                        binding.order?.let { it1 ->
                            wallet?.let { it2 ->
                                viewModel.getNonce(
                                    it1,
                                    it2
                                )

                                viewModel.getRelatedOrders(it1, it2)
                            }
                        }
                        viewModel.getPendingBalances(wallet)


                    }
                    is CancelOrdersState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        userInfo = state.userInfo
                        when {
                            !(state.userInfo != null && state.userInfo.uid > 0) -> {
                                orderAdapter?.submitList(listOf())
                                pendingBalances = null
                                updateAvailableAmount(pendingBalances)
                                binding.tvRelatedOrder.visibility = View.GONE
                            }
                            else -> {
                                binding.order?.let { it1 ->
                                    wallet?.let { it2 ->
                                        viewModel.getRelatedOrders(
                                            it1,
                                            it2
                                        )
                                    }
                                }
                                getPendingBalance()
                                getNonce()
                            }
                        }
                    }
                    is UserInfoState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.getPendingBalancesCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPendingBalancesState.Success -> {

                        this.pendingBalances = state.pendingBalances

                        updateAvailableAmount(state.pendingBalances)
                    }
                    is GetPendingBalancesState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.tvOriginalFee.paintFlags =
            binding.tvOriginalFee.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        binding.tvLearnMore.paintFlags =
            binding.tvLearnMore.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.tvMore.underline(getString(R.string.learn_more))

        binding.tvMore.setOnClickListener {
            openUrl(getString(R.string.order_fee_url))
        }

        binding.tvCancelWhy.setOnClickListener {
            openUrl(getString(R.string.same_token_pair_url))
        }


        binding.tvLearnMore.setOnClickListener {
            openUrl(getString(R.string.order_fee_url))
        }


        viewModel.cancelRelatedOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is CancelOrdersState.Success -> {
                        setWarning(false)
                        saveLimitOrder()

                    }
                    is CancelOrdersState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

    }


    fun getPendingBalance() {
        viewModel.getPendingBalances(wallet)
    }

    private fun saveLimitOrder() {
        binding.order?.let {

            val order = it.copy(
                srcAmount = srcAmount,
                minRate = edtRate.toBigDecimalOrDefaultZero()
            )

            if (binding.order != order) {
                binding.order = order
            }
            viewModel.saveLimitOrder(
                order, true
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun playAnimation(isWarning: Boolean) {
        val animator = ObjectAnimator.ofInt(
            binding.scView,
            "scrollY",
            0
        )

        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()

        setWarning(isWarning)
    }

    private fun setWarning(isWarning: Boolean) {
        binding.vRate.isSelected = isWarning
        binding.isWarning = isWarning
        binding.edtRate.isEnabled = !isWarning
        orderAdapter?.setWarning(isWarning)
    }

    private fun updateAvailableAmount(pendingBalances: PendingBalances?) {
        val calAvailableAmount = binding.order?.let { order ->
            viewModel.calAvailableAmount(
                order.tokenSource,
                pendingBalances
            )
        }
        if (binding.availableAmount != calAvailableAmount) {
            binding.availableAmount = calAvailableAmount
            binding.executePendingBindings()
        }
    }

    private fun moveToLoginTab() {
        (activity as? MainActivity)?.moveToTab(MainPagerAdapter.PROFILE, true)
    }

    override fun getLoginStatus() {
        viewModel.getLoginStatus()
    }

    private fun resetAmount() {
        edtSource.setText("")
        edtDest.setText("")
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        viewModel.compositeDisposable.clear()
        super.onDestroyView()
    }

    private fun getRate(order: LocalLimitOrder) {
        viewModel.getMarketRate(order)
        viewModel.getExpectedRate(
            order,
            edtSource.getAmountOrDefaultValue()
        )
    }

    fun getNonce() {
        binding.order?.let { wallet?.let { it1 -> viewModel.getNonce(it, it1) } }
    }

    fun getRelatedOrders() {
        binding.order?.let { wallet?.let { it1 -> viewModel.getRelatedOrders(it, it1) } }
    }

    override fun showNotification(showNotification: Boolean) {
        binding.vNotification.visibility = if (showNotification) View.VISIBLE else View.GONE
    }

    companion object {
        private const val NOTIFICATION_PARAM = "notification_param"
        fun newInstance(notification: NotificationLimitOrder? = null) = LimitOrderFragment().apply {
            arguments = Bundle().apply {
                putParcelable(NOTIFICATION_PARAM, notification)
            }
        }
    }
}
