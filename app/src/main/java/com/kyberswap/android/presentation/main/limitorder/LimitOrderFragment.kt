package com.kyberswap.android.presentation.main.limitorder

import android.animation.ObjectAnimator
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.EligibleAddress
import com.kyberswap.android.domain.model.EligibleWalletStatus
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletChangeEvent
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.presentation.main.swap.SwapTokenTransactionState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.USER_CLICK_SUBMIT_LO_V1
import com.kyberswap.android.util.USER_CLICK_SUBMIT_LO_V1_WARNING
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.colorRate
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.getAmountOrDefaultValue
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.isSomethingWrongError
import com.kyberswap.android.util.ext.openUrl
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.setViewEnable
import com.kyberswap.android.util.ext.textToDouble
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleOrDefaultZero
import com.kyberswap.android.util.ext.toDoubleSafe
import com.kyberswap.android.util.ext.underline
import kotlinx.android.synthetic.main.fragment_limit_order.*
import kotlinx.android.synthetic.main.fragment_swap.edtDest
import kotlinx.android.synthetic.main.fragment_swap.edtSource
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class LimitOrderFragment : BaseFragment(), LoginState {

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
        ViewModelProvider(this, viewModelFactory).get(LimitOrderViewModel::class.java)
    }

    private val currentActivity by lazy {
        activity as MainActivity
    }

    private val handler by lazy { Handler() }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var analytics: FirebaseAnalytics

    var hasUserFocus: Boolean? = false

    private var userInfo: UserInfo? = null

    private var notification: NotificationLimitOrder? = null

    private val srcAmount: String
        get() = binding.edtSource.text.toString()
    private val dstAmount: String
        get() = binding.edtDest.text.toString()

    private val expectedDestAmount: String
        get() =
            if (rateText.isEmpty()) {
                binding.order?.getExpectedDestAmount(srcAmount.toBigDecimalOrDefaultZero())
                    ?.toDisplayNumber()
            } else {
                binding.order?.getExpectedDestAmount(
                    rateText.toBigDecimalOrDefaultZero(),
                    srcAmount.toBigDecimalOrDefaultZero()
                )?.toDisplayNumber()
            } ?: BigDecimal.ZERO.toString()

    private val expectedSourceAmount: String
        get() =
            if (rateText.toBigDecimalOrDefaultZero() == BigDecimal.ZERO) {
                BigDecimal.ZERO.toString()
            } else {
                if (rateText.toDoubleOrDefaultZero() != 0.0) {
                    dstAmount.toBigDecimalOrDefaultZero()
                        .divide(
                            rateText.toBigDecimalOrDefaultZero(),
                            18,
                            RoundingMode.UP
                        ).toDisplayNumber()
                } else {
                    ""
                }
            }

    private val rateText: String
        get() = binding.edtRate.text.toString()

    private var currentFocus: EditText? = null

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

    private val isDestFocus: Boolean
        get() = currentFocus == binding.edtDest

    private val isSourceFocus: Boolean
        get() = currentFocus == binding.edtSource

    private var eligibleWalletStatus: EligibleWalletStatus? = null

    private var eleigibleAddress: EligibleAddress? = null

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
        getLoginStatus()
        binding.tvSubmitOrder.setViewEnable(true)
        notification?.let {
            dialogHelper.showOrderFillDialog(it) { url ->
                openUrl(getString(R.string.transaction_etherscan_endpoint_url) + url)
            }
        }
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        binding.walletName = state.wallet.display()
                        if (!state.wallet.isSameWallet(wallet)) {
                            wallet = state.wallet
                            viewModel.getLoginStatus()
                            viewModel.getLimitOrders(wallet)
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getLocalLimitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetLocalLimitOrderState.Success -> {
                        if (!state.order.isSameTokenPairForAddress(binding.order)) {

                            if (state.order.tokenSource.tokenSymbol == state.order.tokenDest.tokenSymbol) {
                                showAlertWithoutIcon(
                                    title = getString(R.string.title_unsupported),
                                    message = getString(R.string.limit_order_source_different_dest)
                                )
                            }

                            if (!state.order.isSameSourceDestToken(binding.order)) {
                                hasUserFocus = false
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

                            if (!isDestFocus) {
                                edtSource.setAmount(state.order.srcAmount)
                            }
                            getRate(state.order)
                            viewModel.getGasPrice()
                            viewModel.getGasLimit(wallet, binding.order)
                            getRelatedOrders()
                        }
                    }
                    is GetLocalLimitOrderState.ShowError -> {

                    }
                }
            }
        })

        listOf(binding.imgTokenSource, binding.tvSource).forEach {
            it.setOnClickListener {
                saveState()
                navigator.navigateToTokenSearchFromLimitOrder(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet,
                    true
                )
            }
        }

        listOf(binding.imgTokenDest, binding.tvDest).forEach {
            it.setOnClickListener {
                saveState()
                navigator.navigateToTokenSearchFromLimitOrder(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet,
                    false
                )
            }
        }

        binding.imgClose.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvBalance.setOnClickListener {
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.order?.let {
                if (it.tokenSource.isETHWETH) {
                    binding.edtSource.setText(
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
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.edtSource.setAmount(
                tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.25.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv50Percent.setOnClickListener {
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.edtSource.setAmount(
                tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.5.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv100Percent.setOnClickListener {
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.order?.let {
                if (it.tokenSource.isETHWETH) {
                    binding.edtSource.setText(
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
            setWarning(false)
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
                getRelatedOrders()
                getPendingBalance()
                getNonce()
            }

            hasUserFocus = false

        }

        viewModel.swapTokenTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SwapTokenTransactionState.Success -> {
                        getLimitOrder()
                    }
                    is SwapTokenTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
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
                        if (binding.isWarning != true) {
                            orderAdapter?.submitList(listOf())
                            orderAdapter?.submitList(state.orders)

                            binding.tvRelatedOrder.visibility =
                                if (hasRelatedOrder) View.VISIBLE else View.GONE
                        }
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


        viewModel.getEligibleAddressCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is CheckEligibleAddressState.Success -> {
                        if (this.eleigibleAddress != state.eligibleAddress || state.isWalletChangeEvent) {
                            this.eleigibleAddress = state.eligibleAddress
                            if (state.eligibleAddress.success && !state.eligibleAddress.eligibleAddress && currentFragment is LimitOrderV2Fragment) {
                                showAlertWithoutIcon(
                                    title = getString(R.string.title_error),
                                    message = String.format(
                                        getString(R.string.address_not_eligible),
                                        if (state.eligibleAddress.account.isNotBlank()) """ (${state.eligibleAddress.account})""" else ""
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
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
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
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
                    }
                }
            }
        })

        viewModel.compositeDisposable.add(binding.edtRate.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    hasUserFocus = it
                }
            })

        viewModel.compositeDisposable.add(binding.edtSource.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                sourceLock.set(it || isSourceFocus)
                if (it) {
                    updateCurrentFocus(edtSource)
                    if (binding.edtSource.text.isNullOrEmpty()) {
                        binding.edtDest.setText("")
                    }
                }
            })

        viewModel.compositeDisposable.add(binding.edtDest.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                destLock.set(it)
                if (it) {
                    updateCurrentFocus(edtDest)
                    if (binding.edtDest.text.isNullOrEmpty()) {
                        binding.edtSource.setText("")
                    }
                }
            })

        viewModel.compositeDisposable.add(binding.edtDest.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                if (destLock.get()) {
                    when {
                        rateText.toBigDecimalOrDefaultZero() == BigDecimal.ZERO -> binding.edtSource.setText(
                            ""
                        )
                        text.isNullOrEmpty() -> binding.edtSource.setText("")
                        else -> binding.edtSource.setAmount(
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
                if (!sourceLock.get() || isDestFocus) return@subscribe

                binding.order?.let { order ->
                    when {
                        rateText.isEmpty() -> {
                            edtDest.setText("")

                            viewModel.getExpectedRate(
                                order,
                                if (text.isNullOrEmpty()) getString(R.string.default_source_amount) else text.toString()
                            )
                        }
                        text.isNullOrEmpty() -> edtDest.setText("")
                        else -> edtDest.setAmount(
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
                if (text.isNullOrEmpty()) {
                    binding.edtDest.setText("")
                }
            })

        viewModel.compositeDisposable.add(
            binding.edtRate.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->

                    binding.tvRateWarning.colorRate(text.toString().percentage(rate))
                    binding.order?.let { order ->
                        if (isDestFocus) {
                            binding.edtSource.setAmount(expectedSourceAmount)
                        } else {
                            binding.edtDest.setAmount(
                                expectedDestAmount
                            )
                        }

                        val bindingOrder = binding.order?.copy(
                            srcAmount = srcAmount,
                            minRate = edtRate.toBigDecimalOrDefaultZero()
                        )

                        order.let {
                            if (binding.order != bindingOrder) {
                                binding.order = bindingOrder
                                binding.executePendingBindings()
                            }
                        }
                    }

                    if (text.isNullOrEmpty()) {
                        binding.edtDest.setText("")
                        binding.tvInputtedRateRevert.text = ""
                    } else {
                        if (text.toString().toDoubleSafe() == 0.0) {
                            binding.tvInputtedRateRevert.text = ""
                        } else {
                            binding.tvInputtedRateRevert.text =
                                String.format(
                                    "1 %s = %s %s",
                                    tokenDestSymbol,
                                    (1.0 / text.toString().toDoubleSafe()).toBigDecimal()
                                        .toDisplayNumber(),
                                    tokenSourceSymbol
                                )
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

                        if (hasUserFocus != true) {
                            binding.edtRate.setAmount(rate)
                        }

                        if (isDestFocus) {
                            binding.edtSource.setAmount(expectedSourceAmount)
                        } else {
                            if (binding.edtRate.text?.isNotEmpty() == true) {
                                binding.edtDest.setAmount(
                                    expectedDestAmount
                                )
                            }
                        }
                    }
                    is GetExpectedRateState.ShowError -> {
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
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
            binding.edtRate.setSelection(binding.edtRate.text?.length ?: 0)
        }

        binding.tvSubmitOrderWarning.setOnClickListener {
            setWarning(false)
            saveLimitOrder()
            analytics.logEvent(
                USER_CLICK_SUBMIT_LO_V1_WARNING,
                Bundle().createEvent()
            )
        }

        viewModel.getFeeCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showLoading(state == GetFeeState.Loading)
                when (state) {
                    is GetFeeState.Success -> {

                        binding.hasDiscount =
                            state.fee.discountPercent > 0 && srcAmount.isNotEmpty()


                        binding.tvFee.text = String.format(
                            getString(R.string.limit_order_fee),
                            srcAmount.toBigDecimalOrDefaultZero()
                                .times(state.fee.totalFee.toBigDecimal()).toDisplayNumber()
                                .exactAmount(),
                            tokenSourceSymbol
                        )

                        binding.tvFeeNoDiscount.text = String.format(
                            getString(R.string.limit_order_fee_no_discount),
                            srcAmount.toBigDecimalOrDefaultZero()
                                .times(state.fee.totalFee.toBigDecimal()).toDisplayNumber()
                                .exactAmount(),
                            tokenSourceSymbol
                        )

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

        binding.tvSubmitOrder.setOnClickListener {
            val warningOrderList = viewModel.warningOrderList(
                binding.edtRate.toBigDecimalOrDefaultZero(),
                orderAdapter?.orderList ?: listOf()
            )

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
                    showAlertWithoutIcon(
                        title = getString(R.string.title_amount_too_big),
                        message = getString(R.string.limit_order_insufficient_balance)
                    )
                }
                binding.order?.hasSamePair == true -> showAlertWithoutIcon(
                    title = getString(R.string.title_unsupported),
                    message = getString(R.string.limit_order_source_different_dest)
                )
                binding.order?.amountTooSmall(srcAmount, dstAmount) == true -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.limit_order_amount_too_small)
                    )
                }

                binding.edtRate.textToDouble() == 0.0 -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.limit_order_invalid_rate)
                    )
                }

                binding.order?.isRateTooBig == true -> {
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
                        message = String.format(
                            getString(R.string.address_not_eligible),
                            if (eleigibleAddress?.account?.isNotBlank() == true) """ (${eleigibleAddress?.account})""" else ""
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
                    viewModel.checkEligibleWallet(wallet)

                    analytics.logEvent(
                        USER_CLICK_SUBMIT_LO_V1,
                        Bundle().createEvent()
                    )
                }
            }
        }

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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                                checkEligibleAddress()
                                verifyEligibleWallet()
                                getRelatedOrders()
                                getPendingBalance()
                                getNonce()
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: WalletChangeEvent) {
        getLoginStatus()
        getLimitOrder()
        verifyEligibleWallet()
        wallet?.let {
            viewModel.checkEligibleAddress(it, true)
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

    private fun updateCurrentFocus(view: EditText?) {
        currentFocus?.isSelected = false
        currentFocus = view
        currentFocus?.isSelected = true
        sourceLock.set(view == binding.edtSource)
        destLock.set(view == binding.edtDest)
    }

    fun getSelectedWallet() {
        viewModel.getSelectedWallet()
    }

    fun saveState() {
        hideKeyboard()
        saveOrder()
        if (binding.isWarning == true) {
            hasUserFocus = false
        }
        setWarning(false)
    }

    fun getLimitOrder() {
        wallet?.let {
            viewModel.getLimitOrders(it)
        }
    }

    fun checkEligibleAddress() {
        wallet?.let {
            viewModel.checkEligibleAddress(it)
        }
    }

    fun getPendingBalance() {
        viewModel.getPendingBalances(wallet)
    }

    private fun onVerifyWalletComplete() {
        binding.tvSubmitOrder.setViewEnable(true)
        saveLimitOrder()
    }

    fun onRefresh() {
        getRelatedOrders()
        getPendingBalance()
        getNonce()
    }

    private fun saveOrder() {
        binding.order?.let {

            val order = it.copy(
                srcAmount = srcAmount,
                minRate = edtRate.toBigDecimalOrDefaultZero()
            )
            viewModel.saveLimitOrder(order)
        }
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
        cbUnderstand.isChecked = false
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
        wallet = null
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

    companion object {
        private const val NOTIFICATION_PARAM = "notification_param"
        fun newInstance(notification: NotificationLimitOrder? = null) = LimitOrderFragment().apply {
            arguments = Bundle().apply {
                putParcelable(NOTIFICATION_PARAM, notification)
            }
        }
    }
}
