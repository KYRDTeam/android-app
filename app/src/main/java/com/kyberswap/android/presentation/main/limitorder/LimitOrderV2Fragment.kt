package com.kyberswap.android.presentation.main.limitorder

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderV2Binding
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
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.colorRate
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.isSomethingWrongError
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
import com.kyberswap.android.util.ext.underline
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_limit_order.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class LimitOrderV2Fragment : BaseFragment(), PendingTransactionNotification, LoginState {

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

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LimitOrderV2ViewModel::class.java)
    }

    var baseToken: Token? = null

    private var pendingBalances: PendingBalances? = null

    private val totalAmount: String
        get() = binding.edtTotal.text.toString()
    private val amount: String
        get() = binding.edtAmount.text.toString()

    private val srcAmount: String
        get() = if (isSell) amount else totalAmount

    private val tokenSourceSymbol: String?
        get() = binding.order?.tokenSource?.tokenSymbol

    private val tokenDestSymbol: String?
        get() = binding.order?.tokenDest?.tokenSymbol

    val compositeDisposable = CompositeDisposable()

    var hasUserFocus: Boolean? = false

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

    private val marketPrice: String?
        get() =
            if (type == LocalLimitOrder.TYPE_SELL) binding.market?.displaySellPrice
            else binding.market?.displayBuyPrice

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

    private var eligibleWalletStatus: EligibleWalletStatus? = null

    @Inject
    lateinit var dialogHelper: DialogHelper

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

                            val marketRate = when {
                                isSell -> state.market.sellPrice
                                state.market.buyPrice.toDoubleSafe() == 0.0 -> {
                                    ""
                                }
                                else -> BigDecimal.ONE.divide(
                                    state.market.buyPrice.toBigDecimalOrDefaultZero(),
                                    18,
                                    RoundingMode.CEILING
                                ).toDisplayNumber()
                            }
                            val order = binding.order?.copy(marketRate = marketRate)
                            if (order != binding.order) {
                                binding.order = order
                            }
                            binding.executePendingBindings()
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
                                if (binding.edtPrice.text.isNotEmpty()) {
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
                            viewModel.getPendingBalances(wallet)
                            viewModel.getFee(
                                binding.order,
                                totalAmount,
                                amount,
                                wallet
                            )
                            viewModel.getGasPrice()
                            this.baseToken = state.order.tokenDest
                            resetUI()
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
                            totalAmount.toBigDecimalOrDefaultZero()
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
                                totalAmount.toBigDecimalOrDefaultZero()
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
                    }
                    is GetFeeState.ShowError -> {
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
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
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
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
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
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
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
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
                                    message = getString(R.string.address_not_eligible)
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
                    viewModel.getFee(
                        binding.order,
                        totalAmount,
                        amount,
                        wallet
                    )
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
                viewModel.getFee(
                    binding.order,
                    totalAmount,
                    amount,
                    wallet
                )

                if (text.isNullOrEmpty()) {
                    binding.edtAmount.setText("")
                }
            })

        compositeDisposable.add(
            binding.edtPrice.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->

                    binding.tvRateWarning.colorRate(text.toString().percentage(marketPrice))
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

        val adapter = LimitOrderV2PagerAdapter(childFragmentManager)
        adapter.addFragment(
            LimitOrderTypeFragment.newInstance(LocalLimitOrder.TYPE_BUY),
            getString(R.string.buy)
        )
        adapter.addFragment(
            LimitOrderTypeFragment.newInstance(LocalLimitOrder.TYPE_SELL),
            getString(R.string.sell)
        )

        binding.tlHeader.addTab(binding.tlHeader.newTab().setText("BUY"))
        binding.tlHeader.addTab(binding.tlHeader.newTab().setText("SELL"))

        binding.tlHeader.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                resetUI()
                val position = tab?.position ?: 0
                type = if (position == 0) {
                    LocalLimitOrder.TYPE_BUY
                } else {
                    LocalLimitOrder.TYPE_SELL
                }
                binding.isSell = isSell
                binding.executePendingBindings()
                viewModel.getLimitOrder(wallet, type)
            }
        })

        binding.tvTokenPair.setOnClickListener {
            navigator.navigateToLimitOrderMarket(currentFragment, type)
        }

        binding.imgCandle.setOnClickListener {
            navigator.navigateToChartScreen(
                currentFragment,
                wallet,
                baseToken,
                binding.market?.chartMarket ?: ""

            )
        }

        binding.imgFlag.setOnClickListener {
            navigator.navigateToNotificationScreen(currentFragment)
        }

        binding.tv25Percent.setOnClickListener {

            updateCurrentFocus(viewByType)
            hideKeyboard()
            viewByType.setAmount(
                binding.tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.25.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv50Percent.setOnClickListener {
            updateCurrentFocus(viewByType)
            hideKeyboard()
            viewByType.setAmount(
                binding.tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.5.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv100Percent.setOnClickListener {
            updateCurrentFocus(viewByType)
            hideKeyboard()
            binding.order?.let {
                if (it.tokenSource.isETHWETH) {
                    viewByType.setText(
                        it.availableAmountForTransfer(
                            binding.tvBalance.toBigDecimalOrDefaultZero(),
                            it.gasPrice.toBigDecimalOrDefaultZero()
                        ).toDisplayNumber()
                    )
                } else {
                    viewByType.setAmount(tvBalance.text.toString())
                }

            }
        }

        binding.tvPrice.setOnClickListener {
            binding.edtPrice.setText(marketPrice)
            binding.tvRateWarning.text = ""
        }

        binding.tvOriginalFee.paintFlags =
            binding.tvOriginalFee.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvLearnMore.underline(getString(R.string.learn_more))

        binding.tvLearnMore.setOnClickListener {
            openUrl(getString(R.string.order_fee_url))
        }


        binding.tvSubmitOrder.setOnClickListener {

            when {
                !isNetworkAvailable() -> {
                    showNetworkUnAvailable()
                }
                totalAmount.isEmpty() -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.specify_amount)
                    )
                }
                totalAmount.toBigDecimalOrDefaultZero() >
                        viewModel.calAvailableAmount(
                            binding.order?.tokenSource,
                            pendingBalances
                        ).toBigDecimalOrDefaultZero() -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.title_amount_too_big),
                        message = getString(R.string.limit_order_insufficient_balance)
                    )
                }
                binding.order?.hasSamePair == true -> showAlertWithoutIcon(
                    title = getString(R.string.title_unsupported),
                    message = getString(R.string.limit_order_source_different_dest)
                )
                binding.order?.amountTooSmall(totalAmount, amount) == true -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.limit_order_amount_too_small)
                    )
                }

                binding.edtPrice.textToDouble() == 0.0 -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.limit_order_invalid_rate)
                    )
                }

                priceText.toBigDecimalOrDefaultZero() > marketPrice.toBigDecimalOrDefaultZero() * 10.toBigDecimal() -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.limit_order_rate_too_big)
                    )
                }

                userInfo == null || userInfo!!.uid <= 0 -> {
                    moveToLoginTab()
                    showAlertWithoutIcon(
                        title = getString(R.string.sign_in_required_title),
                        message = getString(
                            R.string.sign_in_to_use_limit_order_feature
                        )
                    )
                }

                (wallet?.isPromo == true) -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error), message = getString(
                            R.string.submit_order_promo_code
                        )
                    )
                }

                eleigibleAddress?.success == true && eleigibleAddress?.eligibleAddress != true -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error),
                        message = getString(R.string.address_not_eligible)
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
                            warningOrderList.isNotEmpty() -> {
                                hideKeyboard()
                                navigator.navigateToCancelOrderFragment(
                                    currentFragment,
                                    wallet,
                                    ArrayList(warningOrderList),
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: WalletChangeEvent) {
        viewModel.getSelectedMarket(wallet)
        viewModel.getLimitOrder(wallet, type)
        viewModel.getLoginStatus()
        wallet?.let { viewModel.checkEligibleAddress(it, true) }
    }

    private fun playAnimation() {
//        val animator = ObjectAnimator.ofInt(
//            binding.scView,
//            "scrollY",
//            binding.tvFee.bottom
//        )
//
//        animator.duration = 300
//        animator.interpolator = AccelerateDecelerateInterpolator()
//        animator.start()
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
        saveLimitOrder()
    }

    private fun saveLimitOrder() {
        binding.order?.let {

            val order = it.copy(
                srcAmount = srcAmount,
                minRate = minRate
            )

            if (binding.order != order) {
                binding.order = order
            }
            viewModel.saveLimitOrder(
                order, true
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

    fun showDiscount(isShown: Boolean) {
        val state = if (isShown) View.VISIBLE else View.GONE
        binding.tvOriginalFee.visibility = state
        binding.tvOff.visibility = state
        binding.progressBar.visibility = View.GONE
    }

    private fun moveToLoginTab() {
        (activity as? MainActivity)?.moveToTab(MainPagerAdapter.PROFILE, true)
    }

    fun getRelatedOrders() {
        binding.order?.let { wallet?.let { it1 -> viewModel.getRelatedOrders(it, it1) } }
    }

    fun getNonce() {
        binding.order?.let { wallet?.let { it1 -> viewModel.getNonce(it, it1) } }
    }

    fun getPendingBalance() {
        viewModel.getPendingBalances(wallet)
    }

    fun refresh() {
        getRelatedOrders()
        getPendingBalance()
        getNonce()
    }

    private fun resetUI() {
        hasUserFocus = false
        binding.edtTotal.setText("")
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

    private fun updateBaseTokenToParentFragment(token: Token) {
        val parent = parentFragment
        if (parent is LimitOrderV2Fragment) {
            parent.baseToken = token
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
        }
        if (binding.availableAmount != calAvailableAmount) {
            binding.availableAmount = calAvailableAmount
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
        super.onDestroyView()
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
