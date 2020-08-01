package com.kyberswap.android.presentation.main.limitorder

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.databinding.FragmentLimitOrderV2Binding
import com.kyberswap.android.databinding.LayoutLoFeeTargetBinding
import com.kyberswap.android.databinding.LayoutLoManageOrderTargetBinding
import com.kyberswap.android.databinding.LayoutLoPairTargetBinding
import com.kyberswap.android.databinding.LayoutLoPriceTargetBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.EligibleAddress
import com.kyberswap.android.domain.model.EligibleWalletStatus
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletChangeEvent
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.KeyImeChange
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.common.TutorialView
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.DES_AMOUNT
import com.kyberswap.android.util.LO_BUY_TAPPED
import com.kyberswap.android.util.LO_CHART
import com.kyberswap.android.util.LO_LEARNMORE
import com.kyberswap.android.util.LO_MANAGE_ORDERS_TAPPED
import com.kyberswap.android.util.LO_PAIR_CHANGE
import com.kyberswap.android.util.LO_PRICE
import com.kyberswap.android.util.LO_SELL_TAPPED
import com.kyberswap.android.util.LO_USER_CLICK_COPY_WALLET_ADDRESS
import com.kyberswap.android.util.SRC_AMOUNT
import com.kyberswap.android.util.TOKEN_PAIR
import com.kyberswap.android.util.USER_CLICK_100_PERCENT
import com.kyberswap.android.util.USER_CLICK_25_PERCENT
import com.kyberswap.android.util.USER_CLICK_50_PERCENT
import com.kyberswap.android.util.USER_CLICK_BUY_SELL_ERROR
import com.kyberswap.android.util.USER_CLICK_CANCEL_ORDER
import com.kyberswap.android.util.USER_CLICK_MARKET_PRICE_TEXT
import com.kyberswap.android.util.USER_CLICK_PRICE_TEXT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.colorRateV2
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.openUrl
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.setViewEnable
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.textToDouble
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleOrDefaultZero
import com.kyberswap.android.util.ext.toDoubleSafe
import com.kyberswap.android.util.ext.toNumberFormat
import com.kyberswap.android.util.ext.underline
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class LimitOrderV2Fragment : BaseFragment(), PendingTransactionNotification, LoginState,
    TutorialView {

    private lateinit var binding: FragmentLimitOrderV2Binding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private var notification: NotificationLimitOrder? = null

    private var type: Int = LocalLimitOrder.TYPE_BUY

    private val isSell
        get() = type == LocalLimitOrder.TYPE_SELL

    private val scrollHeight by lazy {
        binding.edtAmount.top
    }

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LimitOrderV2ViewModel::class.java)
    }

    val baseToken: Token?
        get() = if (isSell) binding.order?.tokenSource else binding.order?.tokenDest

    val quoteToken: Token?
        get() = if (isSell) binding.order?.tokenDest else binding.order?.tokenSource

    val rateUsdQuote: BigDecimal
        get() = quoteToken?.rateUsdNow ?: BigDecimal.ZERO

    val priceUsdQuote: String
        get() {
            if (marketPrice.equals("--")) return "--"
            val priceUsd = rateUsdQuote.multiply(marketPrice.toBigDecimalOrDefaultZero())
            return if (priceUsd == BigDecimal.ZERO) "--" else priceUsd.toDisplayNumber()
        }

    val displayPriceUsdQuote: String
        get() = priceUsdQuote.toNumberFormat()

    private var pendingBalances: PendingBalances? = null

    private val totalAmount: String
        get() = binding.edtTotal.text.toString()
    private val amount: String
        get() = binding.edtAmount.text.toString()

    private val srcAmount: String
        get() = if (isSell) amount else totalAmount

    private val dstAmount: String
        get() = if (isSell) totalAmount else amount

    private val tokenSourceSymbol: String?
        get() = binding.order?.tokenSource?.tokenSymbol

    private val tokenDestSymbol: String?
        get() = binding.order?.tokenDest?.tokenSymbol

    val compositeDisposable = CompositeDisposable()

    var hasUserFocus: Boolean? = false

    private var spotlight: Spotlight? = null

    private val handler by lazy {
        Handler()
    }

    @Volatile
    private var hasFee: Boolean = false

    private val totalLock = AtomicBoolean()
    private val amountLock = AtomicBoolean()

    private var currentFocus: EditText? = null

    private val isAmountFocus: Boolean
        get() = currentFocus == binding.edtAmount

    private val isTotalFocus: Boolean
        get() = currentFocus == binding.edtTotal

    private val priceText: String
        get() = binding.edtPrice.text.toString()

    private val minRate: BigDecimal
        get() = when {
            isSell -> {
                priceText.toBigDecimalOrDefaultZero()
            }
            else ->
                when {
                    priceText.toDoubleSafe() == 0.0 -> BigDecimal.ZERO
                    else -> BigDecimal.ONE.divide(
                        priceText.toBigDecimalOrDefaultZero(),
                        18,
                        RoundingMode.CEILING
                    )
                }
        }

    private val marketRate: BigDecimal
        get() = when {
            isSell -> {
                marketPrice.toBigDecimalOrDefaultZero()
            }
            else ->
                when {
                    marketPrice?.toDoubleSafe() == 0.0 -> BigDecimal.ZERO
                    else -> BigDecimal.ONE.divide(
                        marketPrice.toBigDecimalOrDefaultZero(),
                        18,
                        RoundingMode.CEILING
                    )
                }
        }

    private val balanceText: String
        get() = binding.tvBalance.text.toBigDecimalOrDefaultZero().toDisplayNumber().split(" ")
            .first()

    private val marketPrice: String?
        get() {
            return if (type == LocalLimitOrder.TYPE_SELL) binding.market?.sellPriceValue
            else binding.market?.buyPriceValue
        }

    private val displayMarketPrice: String?
        get() = marketPrice.toNumberFormat()

    private val calcAmount: String
        get() = calcTotalAmount(priceText, totalAmount)

    private val calcTotalAmount: String
        get() = calcAmount(priceText, amount)

    private var eleigibleAddress: EligibleAddress? = null

    private val hasRelatedOrder: Boolean
        get() = viewModel.relatedOrders.any {
            it.src == binding.order?.tokenSource?.symbol &&
                it.dst == binding.order?.tokenDest?.symbol &&
                it.userAddr == wallet?.address
        }

    private val viewByType: EditText
        get() = if (isSell) binding.edtAmount else binding.edtTotal

    private var orderAdapter: OrderAdapter? = null

    private var userInfo: UserInfo? = null

    private val hasUserInfo: Boolean
        get() = userInfo != null && userInfo!!.uid > 0

    private var eligibleWalletStatus: EligibleWalletStatus? = null

    private val order: LocalLimitOrder?
        get() = binding.order

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var mediator: StorageMediator

    private val currentActivity by lazy {
        activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notification = arguments?.getParcelable(NOTIFICATION_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLimitOrderV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getMarkets()
        binding.tvSubmitOrder.setViewEnable(true)
        notification?.let {
            dialogHelper.showOrderFillDialog(it) { url ->
                openUrl(getString(R.string.transaction_etherscan_endpoint_url) + url)
            }
        }

        binding.imgMenu.setOnClickListener {
            showDrawer(true)
        }

        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        binding.walletName = state.wallet.display()
                        if (!state.wallet.isSameWallet(wallet)) {
                            wallet = state.wallet
                            viewModel.getSelectedMarket(wallet)
                            viewModel.getLimitOrder(wallet, type)
                            viewModel.getLoginStatus()
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getSelectedMarketCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSelectedMarketState.Success -> {
                        if (state.market != binding.market) {
                            binding.market = state.market
                            binding.executePendingBindings()
                            binding.tvPrice.text =
                                if (priceUsdQuote != "--") {
                                    "$displayMarketPrice ~ $$displayPriceUsdQuote"
                                } else {
                                    displayMarketPrice
                                }

                            binding.tlHeader.getTabAt(0)?.text = String.format(
                                getString(R.string.buy_token),
                                state.market.baseSymbol
                            )
                            binding.tlHeader.getTabAt(1)?.text = String.format(
                                getString(R.string.sell_token),
                                state.market.baseSymbol
                            )
                            if (hasUserFocus != true) {
                                binding.edtPrice.setAmount(marketPrice)
                            }

                            if (isAmountFocus) {
                                binding.edtTotal.setAmount(calcTotalAmount)
                            } else {
                                if (binding.edtPrice.text?.isNotEmpty() == true) {
                                    binding.edtAmount.setAmount(
                                        calcAmount
                                    )
                                }
                            }
                        }
                    }
                    is GetSelectedMarketState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getLocalLimitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetLocalLimitOrderState.Success -> {
                        if (!state.order.isSameTokenPairForAddress(binding.order)) {
                            binding.order = state.order
                            binding.executePendingBindings()
                            resetUI()
//                            getFee()
                            viewModel.getPendingBalances(wallet)
                            viewModel.getGasPrice()
                            binding.tvPrice.text = if (priceUsdQuote != "--") {
                                "$displayMarketPrice ~ $$displayPriceUsdQuote"
                            } else {
                                displayMarketPrice
                            }
                            refresh()
                        }
                    }
                    is GetLocalLimitOrderState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getFeeCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showLoading(state == GetFeeState.Loading && binding.tvOriginalFee.visibility == View.VISIBLE)
                when (state) {
                    is GetFeeState.Success -> {
                        binding.tvFee.text = String.format(
                            getString(R.string.limit_order_fee),
                            srcAmount.toBigDecimalOrDefaultZero()
                                .times(state.fee.totalFee.toBigDecimal()).toDisplayNumber()
                                .exactAmount(),
                            tokenSourceSymbol
                        )


                        if (amount.toBigDecimalOrDefaultZero() != BigDecimal.ZERO &&
                            state.fee.discountPercent > 0.0
                        ) {
                            showDiscount(true)
                            binding.tvOriginalFee.text = String.format(
                                getString(R.string.limit_order_fee),
                                srcAmount.toBigDecimalOrDefaultZero()
                                    .times(state.fee.totalNonDiscountedFee.toBigDecimal())
                                    .toDisplayNumber().exactAmount(),
                                tokenSourceSymbol
                            )

                            binding.tvOff.text =

                                if (state.fee.discountPercent % 1 == 0.0) {
                                    String.format(
                                        getString(R.string.discount_fee_long_type),
                                        state.fee.discountPercent.toLong()
                                    )
                                } else {
                                    String.format(
                                        getString(R.string.discount_fee), state.fee.discountPercent
                                    )
                                }
                        } else {
                            showDiscount(false)
                        }

                        val order = binding.order?.copy(
                            fee = state.fee.fee.toBigDecimal(),
                            transferFee = state.fee.transferFee.toBigDecimal()
                        )
                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                            binding.invalidateAll()
                        }
                        hasFee = true
                    }
                    is GetFeeState.ShowError -> {
                        showDiscount(false)

                        analytics.logEvent(
                            USER_CLICK_BUY_SELL_ERROR,
                            Bundle().createEvent(state.message)
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
                        analytics.logEvent(
                            USER_CLICK_BUY_SELL_ERROR,
                            Bundle().createEvent(state.message)
                        )
                    }
                }
            }
        })

        viewModel.getGetNonceStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetNonceState.Success -> {
                        val order = binding.order?.copy(nonce = state.nonce)
                        binding.order = order
                    }
                    is GetNonceState.ShowError -> {
                        analytics.logEvent(
                            USER_CLICK_BUY_SELL_ERROR,
                            Bundle().createEvent(state.message)
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
                            analytics.logEvent(
                                USER_CLICK_CANCEL_ORDER,
                                Bundle().createEvent()
                            )
                        }
                    }, {

                    }, {

                    }, {
                        dialogHelper.showInvalidatedDialog(it)
                    })
        }

        orderAdapter?.mode = Attributes.Mode.Single
        binding.rvRelatedOrder.adapter = orderAdapter
        viewModel.getRelatedOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetRelatedOrdersState.Success -> {
                        orderAdapter?.submitList(listOf())
                        orderAdapter?.submitList(state.orders)

                        binding.tvRelatedOrder.visibility =
                            if (hasRelatedOrder) View.VISIBLE else View.GONE
                    }
                    is GetRelatedOrdersState.ShowError -> {
                        analytics.logEvent(
                            USER_CLICK_BUY_SELL_ERROR,
                            Bundle().createEvent(state.message)
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
                                viewModel.terminateUserRequests()
                            }
                            else -> {
                                refresh()
                            }
                        }
                    }
                    is UserInfoState.ShowError -> {
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
                    }
                }
            }
        })

        viewModel.getEligibleAddressCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is CheckEligibleAddressState.Success -> {
                        if (this.eleigibleAddress != state.eligibleAddress || state.isWalletChangeEvent) {
                            this.eleigibleAddress = state.eligibleAddress
                            if (state.eligibleAddress.success && !state.eligibleAddress.eligibleAddress &&
                                currentFragment is LimitOrderV2Fragment
                            ) {
                                showAlertWithoutIcon(
                                    title = getString(R.string.title_error),
                                    message = String.format(
                                        getString(R.string.address_not_eligible),
                                        if (state.eligibleAddress.account.isNotBlank())
                                            """ (${state.eligibleAddress.account})""" else ""
                                    )
                                )
                            }
                        }
                    }
                    is CheckEligibleAddressState.ShowError -> {
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
                    }
                }
            }
        })

        viewModel.checkEligibleWalletCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CheckEligibleWalletState.Loading)
                when (state) {
                    is CheckEligibleWalletState.Success -> {
                        eligibleWalletStatus = state.eligibleWalletStatus
                        if (state.eligibleWalletStatus.success && !state.eligibleWalletStatus.eligible) {
                            binding.tvSubmitOrder.setViewEnable(false)
                        } else {
                            onVerifyWalletComplete()
                        }
                    }
                    is CheckEligibleWalletState.ShowError -> {
                        onVerifyWalletComplete()
                    }
                }
            }
        })

        currentActivity.mainViewModel.checkEligibleWalletCallback.observe(
            viewLifecycleOwner,
            Observer { event ->
                event?.peekContent()?.let { state ->
                    when (state) {
                        is CheckEligibleWalletState.Success -> {
                            eligibleWalletStatus = state.eligibleWalletStatus
                            verifyEligibleWallet(true)
                        }
                        is CheckEligibleWalletState.ShowError -> {

                        }
                    }
                }
            })

        binding.tvManageOrder.setOnClickListener {
            when {
                !isNetworkAvailable() -> {
                    showNetworkUnAvailable()
                }

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

            analytics.logEvent(
                LO_MANAGE_ORDERS_TAPPED,
                Bundle().createEvent()
            )
        }

        viewModel.cancelOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CancelOrdersState.Loading)
                when (state) {
                    is CancelOrdersState.Success -> {
                        getRelatedOrders()
                        getNonce()
                        viewModel.getPendingBalances(wallet)
                    }
                    is CancelOrdersState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        compositeDisposable.add(binding.edtPrice.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    hasUserFocus = it
                    playAnimation()
                }
            })

        compositeDisposable.add(binding.edtTotal.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                totalLock.set(it || isTotalFocus)
                if (it) {
                    playAnimation()
                    updateCurrentFocus(binding.edtTotal)
                    if (binding.edtTotal.text.isNullOrEmpty()) {
                        binding.edtAmount.setText("")
                    }
                }
            })

        compositeDisposable.add(binding.edtAmount.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                amountLock.set(it)
                if (it) {
                    playAnimation()
                    updateCurrentFocus(binding.edtAmount)
                    if (binding.edtAmount.text.isNullOrEmpty()) {
                        binding.edtTotal.setText("")
                    }
                }
            })


        compositeDisposable.add(binding.edtAmount.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                if (amountLock.get()) {
                    binding.edtTotal.setAmount(calcTotalAmount)
                    getFee()
                }
            })

        compositeDisposable.add(binding.edtTotal.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                if (!totalLock.get() || isAmountFocus) return@subscribe

                if (hasUserFocus != true && priceText.isEmpty()) {
                    binding.edtPrice.setAmount(marketPrice)
                }

                binding.edtAmount.setAmount(calcAmount)
                getFee()
                if (text.isNullOrEmpty()) {
                    binding.edtAmount.setText("")
                }
            })

        compositeDisposable.add(
            binding.edtPrice.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->

                    binding.tvRateWarning.colorRateV2(text.toString().percentage(marketPrice))
                    binding.order?.let { order ->
                        if (isAmountFocus) {
                            binding.edtTotal.setAmount(calcTotalAmount)
                        } else {
                            binding.edtAmount.setAmount(calcAmount)
                        }

                        val bindingOrder = binding.order?.copy(
                            srcAmount = srcAmount,
                            minRate = minRate
                        )

                        order.let {
                            if (binding.order != bindingOrder) {
                                binding.order = bindingOrder
                                binding.executePendingBindings()
                            }
                        }
                    }

                    if (text.isNullOrEmpty()) {
                        binding.edtAmount.setText("")
                    }
                })

        binding.tlHeader.addTab(binding.tlHeader.newTab().setText("BUY"))
        binding.tlHeader.addTab(binding.tlHeader.newTab().setText("SELL"))

        binding.tlHeader.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                type = if (position == 0) {
                    LocalLimitOrder.TYPE_BUY
                } else {
                    LocalLimitOrder.TYPE_SELL
                }
                binding.isSell = isSell
                binding.executePendingBindings()
                viewModel.getLimitOrder(wallet, type)
                resetUI()
            }
        })

        binding.tvTokenPair.setOnClickListener {
            navigator.navigateToLimitOrderMarket(currentFragment, type, quoteToken?.tokenSymbol)
            analytics.logEvent(
                LO_PAIR_CHANGE,
                Bundle().createEvent()
            )
        }

        binding.imgCandle.setOnClickListener {
            navigator.navigateToChartScreen(
                currentFragment,
                wallet,
                baseToken,
                binding.market?.chartMarket ?: "",
                binding.market,
                type
            )

            analytics.logEvent(
                LO_CHART,
                Bundle().createEvent()
            )
        }

        binding.imgFlag.setOnClickListener {
            navigator.navigateToNotificationScreen(currentFragment)
        }

        binding.tv25Percent.setOnClickListener {

            updateCurrentFocus(viewByType)
            hideKeyboard()
            viewByType.setAmount(
                balanceText.toBigDecimalOrDefaultZero().multiply(
                    0.25.toBigDecimal()
                ).toDisplayNumber()
            )
            analytics.logEvent(
                USER_CLICK_25_PERCENT,
                Bundle().createEvent()
            )

        }

        binding.tv50Percent.setOnClickListener {
            updateCurrentFocus(viewByType)
            hideKeyboard()
            viewByType.setAmount(
                balanceText.toBigDecimalOrDefaultZero().multiply(
                    0.5.toBigDecimal()
                ).toDisplayNumber()
            )

            analytics.logEvent(
                USER_CLICK_50_PERCENT,
                Bundle().createEvent()
            )
        }

        binding.tv100Percent.setOnClickListener {
            updateCurrentFocus(viewByType)
            hideKeyboard()
            val currentOrder = order
            if (currentOrder != null) {
                if (currentOrder.tokenSource.isETHWETH) {
                    viewByType.setText(
                        currentOrder.availableAmountForTransfer(
                            balanceText.toBigDecimalOrDefaultZero(),
                            currentOrder.gasPrice.toBigDecimalOrDefaultZero()
                        ).toDisplayNumber()
                    )
                } else {
                    viewByType.setAmount(balanceText)
                }
            }

            analytics.logEvent(
                USER_CLICK_100_PERCENT,
                Bundle().createEvent()
            )

        }

        binding.tvPrice.setOnClickListener {
            binding.edtPrice.setAmount(marketPrice)
            binding.tvRateWarning.text = ""
            analytics.logEvent(
                USER_CLICK_MARKET_PRICE_TEXT,
                Bundle().createEvent()
            )
        }

        binding.tvPriceTitle.setOnClickListener {
            binding.edtPrice.setAmount(marketPrice)
            binding.tvRateWarning.text = ""
            analytics.logEvent(
                USER_CLICK_PRICE_TEXT,
                Bundle().createEvent()
            )
        }

        binding.tvOriginalFee.paintFlags =
            binding.tvOriginalFee.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvLearnMore.underline(getString(R.string.learn_more))

        binding.tvLearnMore.setOnClickListener {
            openUrl(getString(R.string.order_fee_url))
            analytics.logEvent(
                LO_LEARNMORE,
                Bundle().createEvent()
            )
        }

        binding.tvSubmitOrder.setOnClickListener {
            val error: String
            when {
                !isNetworkAvailable() -> {
                    showNetworkUnAvailable()
                }
                srcAmount.isEmpty() -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.specify_amount)
                    )
                }
                srcAmount.toBigDecimalOrDefaultZero() >
                    viewModel.calAvailableAmount(
                        binding.order?.tokenSource,
                        pendingBalances
                    ).toBigDecimalOrDefaultZero() -> {

                    error = getString(R.string.limit_order_insufficient_balance)
                    showAlertWithoutIcon(
                        title = getString(R.string.title_amount_too_big),
                        message = error
                    )

                    analytics.logEvent(
                        USER_CLICK_BUY_SELL_ERROR,
                        Bundle().createEvent(error)
                    )
                }

                binding.order?.amountTooSmall(srcAmount, dstAmount) == true -> {
                    error = getString(R.string.limit_order_amount_too_small)
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = error
                    )
                    analytics.logEvent(
                        USER_CLICK_BUY_SELL_ERROR,
                        Bundle().createEvent(error)
                    )
                }

                binding.edtPrice.textToDouble() == 0.0 -> {
                    error = getString(R.string.limit_order_invalid_rate)
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = error
                    )
                    analytics.logEvent(
                        USER_CLICK_BUY_SELL_ERROR,
                        Bundle().createEvent(error)
                    )
                }

                priceText.toBigDecimalOrDefaultZero() > marketPrice.toBigDecimalOrDefaultZero() * 10.toBigDecimal() -> {
                    error = getString(R.string.limit_order_rate_too_big)
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = error
                    )
                    analytics.logEvent(
                        USER_CLICK_BUY_SELL_ERROR,
                        Bundle().createEvent(error)
                    )
                }

                userInfo == null || userInfo!!.uid <= 0 -> {
                    moveToLoginTab()
                    error = getString(
                        R.string.sign_in_to_use_limit_order_feature
                    )
                    showAlertWithoutIcon(
                        title = getString(R.string.sign_in_required_title),
                        message = error
                    )
                    analytics.logEvent(
                        USER_CLICK_BUY_SELL_ERROR,
                        Bundle().createEvent(error)
                    )
                }

                (wallet?.isPromo == true) -> {
                    error = getString(
                        R.string.submit_order_promo_code
                    )
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error),
                        message = error
                    )
                    analytics.logEvent(
                        USER_CLICK_BUY_SELL_ERROR,
                        Bundle().createEvent(error)
                    )
                }

                eleigibleAddress?.success == true && eleigibleAddress?.eligibleAddress != true -> {
                    error = String.format(
                        getString(R.string.address_not_eligible),
                        if (eleigibleAddress?.account?.isNotBlank() == true) """ (${eleigibleAddress?.account})""" else ""
                    )
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error),
                        message = error
                    )
                    analytics.logEvent(
                        USER_CLICK_BUY_SELL_ERROR,
                        Bundle().createEvent(error)
                    )
                }

                else -> binding.order?.let {
                    viewModel.checkEligibleWallet(wallet)
                }
            }
        }


        viewModel.saveOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveLimitOrderState.Success -> {

                        val warningOrderList = viewModel.warningOrderList(
                            minRate,
                            orderAdapter?.orderList ?: listOf()
                        )

                        when {
                            warningOrderList.isNotEmpty() -> {
                                hideKeyboard()
                                navigator.navigateToCancelOrderFragment(
                                    currentFragment,
                                    wallet,
                                    ArrayList(warningOrderList),
                                    binding.order,
                                    viewModel.needConvertWETH(
                                        binding.order,
                                        pendingBalances
                                    )
                                )
                            }

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
                                navigator.navigateToOrderConfirmV2Screen(
                                    currentFragment,
                                    wallet,
                                    binding.order
                                )
                            }
                        }
                    }
                    is SaveLimitOrderState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }

                }
            }
        })

        binding.edtPrice.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.edtPrice.clearFocus()
                playAnimation(true)
            }
            false
        }

        binding.edtAmount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.edtAmount.clearFocus()
                playAnimation(true)
            }
            false
        }

        binding.edtTotal.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.edtTotal.clearFocus()
                playAnimation(true)
            }
            false
        }

        binding.tvName.setOnClickListener {
            if (context != null) {
                val clipboard =
                    context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("Copy", wallet?.address)
                if (clipboard != null && clip != null) {
                    clipboard.setPrimaryClip(clip)
                    showAlert(getString(R.string.address_copy))
                    analytics.logEvent(
                        LO_USER_CLICK_COPY_WALLET_ADDRESS,
                        Bundle().createEvent()
                    )
                }
            }

        }


        binding.edtPrice.setKeyImeChangeListener(object : KeyImeChange {
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    playAnimation(true)
                }
            }
        })

        binding.edtTotal.setKeyImeChangeListener(object : KeyImeChange {
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    playAnimation(true)
                }
            }
        })

        binding.edtAmount.setKeyImeChangeListener(object : KeyImeChange {
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    playAnimation(true)
                }
            }
        })
    }

    fun showTutorial() {
        if (activity == null) return
        if (mediator.isShownLimitOrderTutorial()) return
        binding.tvPrice.doOnPreDraw {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                val targets = ArrayList<Target>()
                val overlayLOPairTargetBinding =
                    DataBindingUtil.inflate<LayoutLoPairTargetBinding>(
                        LayoutInflater.from(activity), R.layout.layout_lo_pair_target, null, false
                    )

                val firstTarget = Target.Builder()
                    .setAnchor(binding.tvPrice)
                    .setShape(Circle(resources.getDimension(R.dimen.tutorial_90_dp)))
                    .setOverlay(overlayLOPairTargetBinding.root)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {
                        }

                        override fun onEnded() {
                            mediator.showLimitOrderTutorial(true)
                        }
                    })
                    .build()
                targets.add(firstTarget)

                val overlayLOPriceTargetBinding =
                    DataBindingUtil.inflate<LayoutLoPriceTargetBinding>(
                        LayoutInflater.from(activity), R.layout.layout_lo_price_target, null, false
                    )

                val location = IntArray(2)
                val view = binding.edtPrice
                view.getLocationInWindow(location)
                val x = location[0].toFloat()
                val y = location[1] + view.height + resources.getDimension(R.dimen.tutorial_15_dp)

                val secondTarget = Target.Builder()
                    .setAnchor(x, y)
                    .setShape(Circle(resources.getDimension(R.dimen.tutorial_120_dp)))
                    .setOverlay(overlayLOPriceTargetBinding.root)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {
                            binding.edtAmount.setText(getString(R.string.tutorial_amount))
                        }

                        override fun onEnded() {
                            binding.edtAmount.setText("")
                        }
                    })
                    .build()
                targets.add(secondTarget)

                val overlayFeeTargetBinding =
                    DataBindingUtil.inflate<LayoutLoFeeTargetBinding>(
                        LayoutInflater.from(activity), R.layout.layout_lo_fee_target, null, false
                    )

                val third = Target.Builder()
                    .setAnchor(binding.tvFee)
                    .setShape(Circle(resources.getDimension(R.dimen.tutorial_90_dp)))
                    .setOverlay(overlayFeeTargetBinding.root)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {
                        }

                        override fun onEnded() {
                        }
                    })
                    .build()
                targets.add(third)

                val overlayManageOrderTargetBinding =
                    DataBindingUtil.inflate<LayoutLoManageOrderTargetBinding>(
                        LayoutInflater.from(activity),
                        R.layout.layout_lo_manage_order_target,
                        null,
                        false
                    )

                val scrollBounds = Rect()
                binding.scView.getHitRect(scrollBounds)
                val offset = if (binding.tvManageOrder.getLocalVisibleRect(scrollBounds)) {
                    0
                } else {
                    scrollHeight
                }

                val submitOrderView = binding.tvSubmitOrder
                submitOrderView.getLocationInWindow(location)
                val xManageOrderView = location[0].toFloat() + submitOrderView.width / 2
                val yManageOrderView =
                    (location[1] + submitOrderView.height + resources.getDimension(R.dimen.tutorial_48_dp) - offset)
                val forth = Target.Builder()
                    .setAnchor(xManageOrderView, yManageOrderView)
                    .setShape(Circle(resources.getDimension(R.dimen.tutorial_80_dp)))
                    .setOverlay(overlayManageOrderTargetBinding.root)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {
                        }

                        override fun onEnded() {
                        }
                    })
                    .build()
                targets.add(forth)

                // create spotlight
                spotlight = Spotlight.Builder(activity!!)
                    .setBackgroundColor(R.color.color_tutorial)
                    .setTargets(targets)
                    .setDuration(1000L)
                    .setAnimation(DecelerateInterpolator(2f))
                    .setContainer(activity!!.window.decorView.findViewById(android.R.id.content))
                    .setOnSpotlightListener(object : OnSpotlightListener {
                        override fun onStarted() {
                        }

                        override fun onEnded() {
                        }
                    })
                    .build()


                if (currentFragment is LimitOrderV2Fragment) {
                    spotlight?.start()
                } else {
                    spotlight?.finish()
                }

                overlayLOPairTargetBinding.tvNext.setOnClickListener {
                    spotlight?.next()
                }

                overlayLOPriceTargetBinding.tvNext.setOnClickListener {
                    spotlight?.next()
                }

                overlayFeeTargetBinding.tvNext.setOnClickListener {
                    if (offset > 0) {
                        playAnimation()
                    }
                    spotlight?.next()
                }

                overlayManageOrderTargetBinding.tvNext.setOnClickListener {
                    spotlight?.next()
                    if (offset > 0) {
                        playAnimation(true)
                    }
                }

            }, 500)
        }
    }

    fun getFee() {
        hasFee = false
        showDiscount(false)
        viewModel.getFee(
            binding.order,
            srcAmount,
            dstAmount,
            wallet
        )
        if (binding.tvFee.text.isNullOrBlank()) {
            binding.tvFee.text = String.format(
                getString(R.string.limit_order_fee),
                "0",
                tokenSourceSymbol
            )
        }
    }

    fun setSelectedTab(type: Int) {
        if (type == LocalLimitOrder.TYPE_BUY) {
            binding.tlHeader.getTabAt(0)?.select()
        } else if (type == LocalLimitOrder.TYPE_SELL) {
            binding.tlHeader.getTabAt(1)?.select()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: WalletChangeEvent) {
        viewModel.getSelectedMarket(wallet)
        viewModel.getLimitOrder(wallet, type)
        viewModel.getLoginStatus()
        wallet?.let { viewModel.checkEligibleAddress(it, true) }
    }

    private fun playAnimation(isReady: Boolean = false) {
        val animator = ObjectAnimator.ofInt(
            binding.scView,
            "scrollY",
            if (isReady) 0 else scrollHeight
        )

        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    fun getLimitOrder() {
        wallet?.let {
            viewModel.getLimitOrder(it, type)
        }
    }

    fun checkEligibleAddress() {
        wallet?.let {
            viewModel.checkEligibleAddress(it)
        }
    }

    fun verifyEligibleWallet(isDisablePopup: Boolean = false) {
        eligibleWalletStatus?.let {
            if (it.success && !it.eligible) {
                if (!isDisablePopup) {
                    binding.tvSubmitOrder.setViewEnable(false)
                }
                binding.tvSubmitOrder.setViewEnable(false)

                showError(it.message)
            } else {
                binding.tvSubmitOrder.setViewEnable(true)
            }
        }
    }

    private fun onVerifyWalletComplete() {
        binding.tvSubmitOrder.setViewEnable(true)
        if (hasFee) {
            saveLimitOrder()
        } else {
            showError(getString(R.string.no_fee_error))
        }
    }

    private fun saveLimitOrder() {
        binding.order?.let {

            val order = it.copy(
                srcAmount = srcAmount,
                minRate = minRate,
                marketRate = marketRate.toDisplayNumber()
            )

            if (binding.order != order) {
                binding.order = order
            }
            viewModel.saveLimitOrder(
                order, true
            )

            analytics.logEvent(
                if (isSell) LO_SELL_TAPPED else LO_BUY_TAPPED,
                Bundle().createEvent(
                    listOf(TOKEN_PAIR, SRC_AMOUNT, DES_AMOUNT, LO_PRICE),
                    listOf(
                        order.pair,
                        order.displayAmount,
                        order.displayTotal,
                        order.price
                    )
                )
            )
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

    private fun showDiscount(isShown: Boolean) {
        val state = if (isShown) View.VISIBLE else View.GONE
        binding.tvOriginalFee.visibility = state
        binding.tvOff.visibility = state
        binding.progressBar.visibility = View.GONE
    }

    private fun moveToLoginTab() {
        (activity as? MainActivity)?.moveToTab(MainPagerAdapter.EXPLORE, true)
    }

    private fun getRelatedOrders() {
        if (binding.order != null && wallet != null && hasUserInfo) {
            viewModel.getRelatedOrders(binding.order!!, wallet!!)
        }
    }

    private fun getNonce() {
        if (binding.order != null && wallet != null && hasUserInfo) {
            viewModel.getNonce(binding.order!!, wallet!!)
        }
    }

    private fun getPendingBalance() {
        viewModel.getPendingBalances(wallet)
    }

    fun refresh() {
        getNonce()
        getFee()
        getRelatedOrders()
        getPendingBalance()
    }

    fun resetUI() {
        hasUserFocus = false
        binding.edtPrice.setAmount(marketPrice)

        binding.edtTotal.setText("")
        binding.tvFee.text = ""
        binding.edtAmount.setText("")
    }

    private fun calcTotalAmount(rateText: String, srcAmount: String): String {
        return if (rateText.toDoubleOrDefaultZero() != 0.0) {
            srcAmount.toBigDecimalOrDefaultZero()
                .divide(
                    rateText.toBigDecimalOrDefaultZero(),
                    18,
                    RoundingMode.UP
                ).toDisplayNumber()
        } else {
            ""
        }
    }

    private fun calcAmount(rate: String, dstAmount: String): String {
        return if (rate.isEmpty()) {
            ""
        } else {

            rate.toBigDecimalOrDefaultZero()
                .multiply(dstAmount.toBigDecimalOrDefaultZero())
                .toDisplayNumber()
        }
    }

    fun showFillOrder(notificationLimitOrder: NotificationLimitOrder) {
        clearFragmentBackStack()
        dialogHelper.showOrderFillDialog(notificationLimitOrder) { url ->
            openUrl(getString(R.string.transaction_etherscan_endpoint_url) + url)
        }
    }

    private fun clearFragmentBackStack() {
        val fm = currentFragment.childFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
    }

    override fun showPendingTxNotification(showNotification: Boolean) {
        if (::binding.isInitialized) {
            binding.vNotification.visibility = if (showNotification) View.VISIBLE else View.GONE
        }
    }

    override fun showUnReadNotification(showNotification: Boolean) {
        if (::binding.isInitialized) {
            binding.vFlagNotification.visibility = if (showNotification) View.VISIBLE else View.GONE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun updateAvailableAmount(pendingBalances: PendingBalances?) {
        val calAvailableAmount = binding.order?.let { order ->
            viewModel.calAvailableAmount(
                order.tokenSource,
                pendingBalances
            )
        }.toNumberFormat()

        val availableAmount = "$calAvailableAmount $tokenSourceSymbol"

        if (binding.availableAmount != availableAmount) {
            binding.availableAmount = availableAmount
            binding.executePendingBindings()
        }
    }

//    private fun getRate(order: LocalLimitOrder) {
//        viewModel.getMarketRate(order)
//        viewModel.getExpectedRate(
//            order,
//            binding.edtAmount.getAmountOrDefaultValue()
//        )
//    }

    private fun updateCurrentFocus(view: EditText?) {
        currentFocus?.isSelected = false
        currentFocus = view
        currentFocus?.isSelected = true
        totalLock.set(view == binding.edtTotal)
        amountLock.set(view == binding.edtAmount)
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        hasFee = false
        hideKeyboard()
        spotlight?.finish()
        super.onDestroyView()
    }

    override fun skipTutorial() {
        spotlight?.finish()
    }

    companion object {
        private const val NOTIFICATION_PARAM = "notification_param"
        fun newInstance(notification: NotificationLimitOrder? = null) =
            LimitOrderV2Fragment().apply {
                arguments = Bundle().apply {
                    putParcelable(NOTIFICATION_PARAM, notification)
                }
            }
    }

    override fun getLoginStatus() {
        viewModel.getLoginStatus()
    }
}
