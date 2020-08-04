package com.kyberswap.android.presentation.main.swap

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.EditText
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.databinding.FragmentSwapBinding
import com.kyberswap.android.databinding.LayoutSwapAdvanceTargetBinding
import com.kyberswap.android.databinding.LayoutSwapInputtedTargetBinding
import com.kyberswap.android.databinding.LayoutSwapPairTargetBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.EligibleWalletStatus
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.NotificationExt
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletChangeEvent
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.AlertDialogFragment
import com.kyberswap.android.presentation.common.DEFAULT_ACCEPT_RATE_PERCENTAGE
import com.kyberswap.android.presentation.common.KeyImeChange
import com.kyberswap.android.presentation.common.PLATFORM_FEE_BPS
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.common.TutorialView
import com.kyberswap.android.presentation.common.WalletObserver
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.alert.GetAlertState
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.ERROR_TEXT
import com.kyberswap.android.util.GAS_OPTION
import com.kyberswap.android.util.GAS_OPTIONS_FAST
import com.kyberswap.android.util.GAS_OPTIONS_REGULAR
import com.kyberswap.android.util.GAS_OPTIONS_SLOW
import com.kyberswap.android.util.GAS_OPTIONS_SUPER_FAST
import com.kyberswap.android.util.GAS_VALUE
import com.kyberswap.android.util.KBSWAP_ADVANCED
import com.kyberswap.android.util.KBSWAP_ERROR
import com.kyberswap.android.util.KBSWAP_SWAP_TAPPED
import com.kyberswap.android.util.KBSWAP_TOKEN_SELECT
import com.kyberswap.android.util.KBSWAP_TOKEN_SWITCH
import com.kyberswap.android.util.SLIPPAGE
import com.kyberswap.android.util.SW_USER_CLICK_COPY_WALLET_ADDRESS
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.formatDisplayNumber
import com.kyberswap.android.util.ext.getAmountOrDefaultValue
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.isSomethingWrongError
import com.kyberswap.android.util.ext.rounding
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.setViewEnable
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleSafe
import com.kyberswap.android.util.ext.toNumberFormat
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import kotlinx.android.synthetic.main.fragment_swap.*
import kotlinx.android.synthetic.main.layout_expanable.*
import net.cachapa.expandablelayout.ExpandableLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SwapFragment : BaseFragment(), PendingTransactionNotification, WalletObserver, TutorialView {

    private lateinit var binding: FragmentSwapBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var alertNotification: NotificationAlert? = null

    private var notification: Notification? = null

    private var notificationExt: NotificationExt? = null

    private var maxGasPrice: String = ""

    private val isUserSelectSwap: Boolean
        get() = currentFragment is SwapFragment

    private var platformFee: Int = PLATFORM_FEE_BPS

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var sourceAmount: String? = null

    private val displaySourceAmount: String
        get() = sourceAmount.toBigDecimalOrDefaultZero().toDisplayNumber()

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SwapViewModel::class.java)
    }

    private val tokenSources by lazy {
        listOf(binding.imgTokenSource, binding.tvSource)
    }

    private val tokenDests by lazy {
        listOf(binding.imgTokenDest, binding.tvDest)
    }

    private var selectedGasFeeView: CompoundButton? = null

    private var currentFocus: EditText? = null

    private val destLock = AtomicBoolean()

    private var hasExpectedRate: Boolean = false

    private var eligibleWalletStatus: EligibleWalletStatus? = null

    @Inject
    lateinit var mediator: StorageMediator

    private var spotlight: Spotlight? = null

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val availableAmount: BigDecimal
        get() = binding.swap?.let {
            it.availableAmountForSwap(
                it.tokenSource.currentBalance,
                it.allETHBalanceGasLimit.toBigDecimal(),
                getSelectedGasPrice(it.gas, selectedGasFeeView?.id).toBigDecimalOrDefaultZero()
            )
        } ?: BigDecimal.ZERO

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val handler by lazy {
        Handler()
    }

    private val currentActivity by lazy {
        activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertNotification = arguments?.getParcelable(ALERT_PARAM)
        notification = arguments?.getParcelable(NOTIFICATION_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSwapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getSelectedWallet()
        alertNotification?.let { viewModel.getAlert(it) }
        swap(notification)
        binding.tvContinue.setViewEnable(true)

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {

                        if (state.wallet.display() != binding.walletName) {
                            binding.walletName = state.wallet.display()
                        }

                        if (!state.wallet.isSameWallet(wallet)) {
                            if (wallet == null) {
                                this.wallet = state.wallet
                                swap(notification)
                            } else {
                                this.wallet = state.wallet
                            }
                            val promo = wallet?.promo
                            if (wallet?.isPromo == true) {
                                enableTokenSearch(isSourceToken = true, isEnable = false)
                                if (promo?.destinationToken?.isNotBlank() == true) {
                                    enableTokenSearch(isSourceToken = false, isEnable = false)
                                } else {
                                    enableTokenSearch(isSourceToken = false, isEnable = true)
                                }
                            } else {
                                enableTokenSearch(isSourceToken = true, isEnable = true)
                                enableTokenSearch(isSourceToken = false, isEnable = true)
                            }

                            viewModel.getSwapData(state.wallet, alertNotification)
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {
                        if (!state.swap.isSameTokenPair(binding.swap)) {
                            viewModel.getPlatformFee(state.swap)
                            if (state.swap.tokenSource.tokenSymbol == state.swap.tokenDest.tokenSymbol) {
                                showAlertWithoutIcon(
                                    title = getString(R.string.title_unsupported),
                                    message = getString(R.string.can_not_swap_same_token)
                                )
                            }

                            // Token pair change need to reset rate and get the new one
                            binding.swap = state.swap.copy(marketRate = "", expectedRate = "")
                            binding.executePendingBindings()
                            sourceAmount = state.swap.sourceAmount

                            if (isUserSelectSwap) {
                                getRate(state.swap)
                                viewModel.getGasLimit(wallet, binding.swap, platformFee)
                            }
                            viewModel.getGasPrice()
                        } else if (isUserSelectSwap) {
                            viewModel.getGasLimit(wallet, binding.swap, platformFee)
                        } else if (!isUserSelectSwap && hasExpectedRate) {
                            viewModel.disposeGetExpectedRate()
                        }
                    }
                    is GetSwapState.ShowError -> {

                    }
                }
            }
        })

        tvAdvanceOption.setOnClickListener {
            expandableLayout.expand()
            tvRevertNotification.text =
                getRevertNotification(rgRate.checkedRadioButtonId)
        }
        imgClose.setOnClickListener {
            expandableLayout.collapse()
        }

        expandableLayout.setOnExpansionUpdateListener { _, state ->
            if (state == ExpandableLayout.State.EXPANDED) {
                val animator = ObjectAnimator.ofInt(
                    binding.scView,
                    "scrollY",
                    tvRevertNotification.top
                )

                animator.duration = 300
                animator.interpolator = AccelerateInterpolator()
                animator.start()
            }
        }

        imgMenu.setOnClickListener {
            showDrawer(true)
        }


        imgSwap.setOnClickListener {
            updateCurrentFocus(edtSource)
            edtDest.clearFocus()
            resetAmount()
            val swap = binding.swap?.swapToken()
            swap?.let {
                viewModel.saveSwap(swap)
                getRate(it)
            }
            binding.setVariable(BR.swap, swap)
            binding.executePendingBindings()
            analytics.logEvent(KBSWAP_TOKEN_SWITCH, Bundle().createEvent())
        }

        viewModel.compositeDisposable.add(
            edtSource.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->
                    if (destLock.get() || currentFocus == binding.edtDest) {
                        return@subscribe
                    }
                    sourceAmount = text.toString()
                    if (text.isNullOrEmpty()) {
                        binding.edtDest.setText("")
                    }
                    binding.swap?.let { swap ->
                        if (swap.hasSamePair) {
                            edtDest.setText(text)
                        } else {
                            edtDest.setAmount(
                                swap.getExpectedAmount(
                                    swap.combineRate,
                                    text.toString()
                                ).toDisplayNumber()
                            )
                            getRate(swap)
                            wallet?.let {

                                val updatedSwap = swap.copy(
                                    sourceAmount = sourceAmount ?: "",
                                    minAcceptedRatePercent =
                                    getMinAcceptedRatePercent(rgRate.checkedRadioButtonId)
                                )
                                binding.swap = updatedSwap
                                viewModel.saveSwap(updatedSwap)
                            }
                        }
                    }
                })

        viewModel.compositeDisposable.add(
            binding.edtDest.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { dstAmount ->
                    if (destLock.get()) {
                        if (dstAmount.isEmpty()) binding.edtSource.setText("")
                        binding.swap?.let { swap ->
                            viewModel.estimateAmount(
                                swap.sourceAddress,
                                swap.destAddress,
                                dstAmount.toString(),
                                platformFee
                            )
                        }
                    }
                }
        )


        tokenSources.forEach {
            it.setOnClickListener {
                binding.swap?.let { swap ->
                    viewModel.saveSwap(
                        swap.copy(
                            sourceAmount = sourceAmount ?: "",
                            destAmount = edtDest.text.toString(),
                            minAcceptedRatePercent = getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                            gasPrice = getSelectedGasPrice(swap.gas, selectedGasFeeView?.id)
                        )
                    )
                }
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    currentFragment,
                    wallet,
                    true
                )

                analytics.logEvent(KBSWAP_TOKEN_SELECT, Bundle().createEvent())
            }

        }

        tokenDests.forEach {
            it.setOnClickListener {
                binding.swap?.let { swap ->
                    viewModel.saveSwap(
                        swap.copy(
                            sourceAmount = sourceAmount ?: "",
                            destAmount = edtDest.text.toString(),
                            minAcceptedRatePercent = getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                            gasPrice = getSelectedGasPrice(swap.gas, selectedGasFeeView?.id)
                        )
                    )
                }
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    currentFragment,
                    wallet,
                    false
                )

                analytics.logEvent(KBSWAP_TOKEN_SELECT, Bundle().createEvent())
            }
        }

        binding.tvTokenBalanceValue.setOnClickListener {
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.swap?.let {
                if (it.tokenSource.isETH) {
                    showAlertWithoutIcon(message = getString(R.string.small_amount_of_eth_transaction_fee))
                    sourceAmount = availableAmount.toDisplayNumber()
                    binding.edtSource.setText(sourceAmount)
                } else {
                    sourceAmount = it.tokenSource.currentBalance.rounding().toDisplayNumber()
                    binding.edtSource.setText(sourceAmount)
                    verifyAmount()
                }

            }
        }

        binding.tv25Percent.setOnClickListener {
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.edtSource.setAmount(
                tvTokenBalanceValue.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.25.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv50Percent.setOnClickListener {
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.edtSource.setAmount(
                tvTokenBalanceValue.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.5.toBigDecimal()
                ).toDisplayNumber()
            )
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
                        SW_USER_CLICK_COPY_WALLET_ADDRESS,
                        Bundle().createEvent()
                    )
                }
            }

        }

        binding.tv100Percent.setOnClickListener {
            updateCurrentFocus(edtSource)
            hideKeyboard()
            binding.swap?.let {
                if (it.tokenSource.isETH) {
                    showAlertWithoutIcon(message = getString(R.string.small_amount_of_eth_transaction_fee))
                    sourceAmount = availableAmount.toDisplayNumber()
                    binding.edtSource.setText(sourceAmount)
                } else {
                    sourceAmount = it.tokenSource.currentBalance.rounding().toDisplayNumber()
                    binding.edtSource.setText(sourceAmount)
                    verifyAmount()
                }

            }
        }

        viewModel.getExpectedRateCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetExpectedRateState.Success -> {
                        hasExpectedRate = true
                        val swap =
                            if (state.list.first().toBigDecimalOrDefaultZero() > BigDecimal.ZERO) {
                                binding.swap?.copy(
                                    expectedRate = state.list[0],
                                    isExpectedRateZero = false
                                )
                            } else {
                                binding.swap?.copy(
                                    expectedRate = binding.swap?.marketRate ?: "",
                                    isExpectedRateZero = true
                                )
                            }

                        if (swap != null) {
                            if (swap != binding.swap) {
                                binding.swap = swap
                                binding.executePendingBindings()
                            }

                            if (destLock.get() || currentFocus == binding.edtDest) {
                                sourceAmount =
                                    if (swap.expectedRate.toDoubleSafe() == 0.0) {
                                        BigDecimal.ZERO.toDisplayNumber()
                                    } else {
                                        edtDest.toBigDecimalOrDefaultZero().divide(
                                            swap.expectedRate.toBigDecimalOrDefaultZero(),
                                            18,
                                            RoundingMode.UP
                                        ).stripTrailingZeros().toPlainString()
                                    }

                                edtSource.setAmount(
                                    displaySourceAmount
                                )
                            } else {
                                binding.swap?.let {
                                    edtDest.setAmount(
                                        it.getExpectedAmount(
                                            it.combineRate,
                                            edtSource.text.toString()
                                        ).toDisplayNumber()
                                    )
                                }
                            }

                            showDestValueInUsd(swap)

                            tvRevertNotification.text =
                                getRevertNotification(rgRate.checkedRadioButtonId)
                        }
                    }
                    is GetExpectedRateState.ShowError -> {
                        analytics.logEvent(
                            KBSWAP_ERROR, Bundle().createEvent(
                                ERROR_TEXT,
                                state.message
                            )
                        )
                    }
                }
            }
        })

        viewModel.getGetMarketRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {
                        val swap = binding.swap?.copy(
                            marketRate = state.rate
                        )
                        tvRevertNotification.text =
                            getRevertNotification(rgRate.checkedRadioButtonId)

                        if (swap != binding.swap) {
                            binding.swap = swap
                            swap?.let { it1 -> showDestValueInUsd(it1) }
                            binding.executePendingBindings()
                        }
                    }
                    is GetMarketRateState.ShowError -> {
                        analytics.logEvent(
                            KBSWAP_ERROR, Bundle().createEvent(
                                ERROR_TEXT,
                                state.message
                            )
                        )
                    }
                }
            }
        })

        viewModel.getGetGasLimitCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasLimitState.Success -> {
                        val swap = binding.swap?.copy(
                            gasLimit = state.gasLimit.toString()
                        )

                        tvRevertNotification.text =
                            getRevertNotification(rgRate.checkedRadioButtonId)

                        if (swap != binding.swap) {
                            binding.swap = swap
                            binding.executePendingBindings()
                        }
                    }
                    is GetGasLimitState.ShowError -> {
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
                    }
                }
            }
        })

        viewModel.compositeDisposable.add(
            rbCustom.checkedChanges().skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    edtCustom.isEnabled = it
                    if (it) {
                        edtCustom.setText("3")
                        edtCustom.requestFocus()
                        edtCustom.setSelection(edtCustom.text?.length ?: 0)
                    } else {
                        edtCustom.setText("")
                    }

                })

        viewModel.compositeDisposable.add(
            edtCustom.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    edtCustom.isSelected = it.isNullOrEmpty() && rbCustom.isChecked

                    if (it.isNotEmpty() && it.toString().toInt() > 0 && it.toString()
                            .toInt() <= 100
                    ) {
                        tvRevertNotification.text =
                            getRevertNotification(R.id.rbCustom)
                    } else if (it.isNotEmpty() && it.toString().toInt() > 100) {
                        val remaining = it.dropLast(1)
                        edtCustom.setText(remaining)
                        edtCustom.setSelection(remaining.length)
                    }

                }
        )

        viewModel.compositeDisposable.add(binding.edtDest.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                destLock.set(it)
                if (it) {
                    currentFocus?.isSelected = false
                    currentFocus = edtDest
                    currentFocus?.isSelected = true
                }
            })


        viewModel.compositeDisposable.add(binding.edtSource.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    currentFocus?.isSelected = false
                    currentFocus = edtSource
                    currentFocus?.isSelected = true
                }
            })



        viewModel.compositeDisposable.add(
            rgRate.checkedChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe { id ->
                    tvRevertNotification.text = getRevertNotification(id)
                })

        listOf(rbSuperFast, rbFast, rbRegular, rbSlow).forEach {
            it.setOnCheckedChangeListener { rb, isChecked ->
                if (isChecked) {
                    if (rb != selectedGasFeeView) {
                        selectedGasFeeView?.isChecked = false
                        rb.isSelected = true
                        selectedGasFeeView = rb
                        binding.swap?.let { swap ->
                            viewModel.saveSwap(
                                swap.copy(
                                    gasPrice = getSelectedGasPrice(
                                        swap.gas,
                                        rb.id
                                    )
                                )
                            )
                        }
                    }
                }

            }
        }

        binding.edtSource.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyAmount()
                v.clearFocus()
            }
            false
        }

        binding.edtDest.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyAmount()
                v.clearFocus()
            }
            false
        }



        binding.edtSource.setKeyImeChangeListener(object : KeyImeChange {
            override fun onKeyIme(keyCode: Int, event: KeyEvent?) {
                if (KeyEvent.KEYCODE_BACK == event?.keyCode) {
                    verifyAmount()
                }
            }
        })

        edtCustom.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyAmount()
                v.clearFocus()
            }
            false
        }


        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val swap = binding.swap?.copy(
                            gas = if (wallet?.isPromo == true) state.gas.toPromoGas()
                                .copy(maxGasPrice = maxGasPrice) else state.gas.copy(maxGasPrice = maxGasPrice)
                        )
                        if (swap != binding.swap) {
                            binding.swap = swap
                            binding.executePendingBindings()
                        }
                    }
                    is GetGasPriceState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getPlatformFeeCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPlatformFeeState.Success -> {
                        if (state.platformFee.fee != platformFee) {
                            platformFee = state.platformFee.fee
                            viewModel.getGasLimit(wallet, binding.swap, platformFee)
                            if (binding.swap != null) {
                                getRate(binding.swap!!)
                            }
                        }
                    }
                    is GetPlatformFeeState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getKyberStatusback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetKyberStatusState.Success -> {
                        if (state.kyberEnabled.success && !state.kyberEnabled.data) {
                            showError(getString(R.string.kyber_down), time = 15)
                            binding.tvContinue.isEnabled = false
                        } else {
                            binding.tvContinue.isEnabled = true
                        }
                    }
                    is GetKyberStatusState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getAlertState.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertState.Success -> {
                        dialogHelper.showAlertTriggerDialog(state.alert) {

                        }
                    }
                    is GetAlertState.ShowError -> {

                    }
                }
            }
        })

        viewModel.estimateAmountState.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is EstimateAmountState.Success -> {
                        sourceAmount = state.amount
                        edtSource.setAmount(displaySourceAmount)
                        if (binding.swap != null) {
                            getRate(binding.swap!!)
                        }
                    }
                    is EstimateAmountState.ShowError -> {

                    }
                }
            }
        })

        binding.imgInfo.setOnClickListener {
            showAlert(
                String.format(
                    getString(R.string.swap_rate_notification),
                    binding.swap?.ratePercentage
                        .toBigDecimalOrDefaultZero()
                        .abs()
                        .toDisplayNumber()
                ),
                R.drawable.ic_info,
                timeInSecond = 30
            )
        }

        tvGasFee.setOnClickListener {
            dialogHelper.showBottomSheetGasFeeDialog()
        }

        viewModel.saveSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapState.Loading)
                when (state) {
                    is SaveSwapState.Success -> {
                        if (state.isExpectedRateZero) {
                            showAlertWithoutIcon(message = getString(R.string.please_wait_for_expected_rate_updated))
                        } else {
                            val currentActivity = activity
                            if (currentActivity != null) {
                                val confirmedActivity = when {
                                    wallet?.isPromo == true -> when {
                                        wallet?.isPromoPayment == true -> PromoPaymentConfirmActivity.newIntent(
                                            currentActivity,
                                            wallet,
                                            platformFee
                                        )
                                        else -> PromoSwapConfirmActivity.newIntent(
                                            currentActivity,
                                            wallet,
                                            platformFee
                                        )
                                    }
                                    else -> SwapConfirmActivity.newIntent(
                                        currentActivity,
                                        wallet,
                                        platformFee
                                    )
                                }

                                startActivityForResult(confirmedActivity, SWAP_CONFIRM)
                            }
                        }
                    }
                }
            }
        })

        binding.tvContinue.setOnClickListener {
            val swap = binding.swap
            when {
                swap != null -> {
                    val swapError: String
                    when {
                        swap.isExpectedRateZero && swap.isMarketRateZero -> {
                            swapError = getString(R.string.reserve_under_maintainance)
                            showAlertWithoutIcon(message = swapError)
                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }
                        edtSource.text.isNullOrEmpty() -> {
                            swapError = getString(R.string.specify_amount)
                            showAlertWithoutIcon(
                                title = getString(R.string.invalid_input),
                                message = swapError
                            )
                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }
                        edtSource.text.toString()
                            .toBigDecimalOrDefaultZero() > swap.tokenSource.currentBalance -> {
                            swapError = getString(R.string.exceed_balance)
                            showAlertWithoutIcon(
                                title = getString(R.string.title_amount_too_big),
                                message = swapError
                            )
                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }
                        swap.hasSamePair -> {
                            swapError = getString(R.string.can_not_swap_same_token)
                            showAlertWithoutIcon(
                                title = getString(R.string.title_unsupported),
                                message = swapError
                            )
                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }
                        swap.amountTooSmall(edtSource.text.toString()) && !(swap.tokenSource.rateEthNowOrDefaultValue == BigDecimal.ZERO && !swap.isExpectedRateEmptyOrZero) -> {
                            swapError = getString(R.string.swap_amount_small)
                            showAlertWithoutIcon(
                                title = getString(R.string.invalid_amount),
                                message = swapError
                            )
                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }
                        swap.copy(
                            gasPrice = getSelectedGasPrice(
                                swap.gas,
                                selectedGasFeeView?.id
                            )
                        ).insufficientEthBalance -> {
                            swapError = String.format(
                                getString(R.string.not_enough_eth_blance), swap.copy(
                                    gasPrice = getSelectedGasPrice(
                                        swap.gas,
                                        selectedGasFeeView?.id
                                    )
                                ).gasFeeEth.formatDisplayNumber()
                            )
                            showAlertWithoutIcon(
                                getString(R.string.insufficient_eth),
                                swapError
                            )

                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }
                        swap.tokenSource.isETH &&
                                availableAmount < edtSource.toBigDecimalOrDefaultZero() -> {
                            swapError = String.format(
                                getString(R.string.not_enough_eth_blance), swap.copy(
                                    gasPrice = getSelectedGasPrice(
                                        swap.gas,
                                        selectedGasFeeView?.id
                                    )
                                ).gasFeeEth.formatDisplayNumber()
                            )
                            showAlertWithoutIcon(
                                getString(R.string.insufficient_eth),
                                swapError
                            )
                            analytics.logEvent(
                                KBSWAP_ERROR, Bundle().createEvent(
                                    ERROR_TEXT,
                                    swapError
                                )
                            )
                        }
                        rbCustom.isChecked && edtCustom.text.isNullOrEmpty() -> {
                            swapError = getString(R.string.custom_rate_empty)
                            showAlertWithoutIcon(message = swapError)
                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }

                        swap.isExpectedRateZero -> {
                            swapError = getString(R.string.can_not_handle_amount)
                            showAlertWithoutIcon(
                                title = getString(R.string.title_amount_too_big),
                                message = swapError
                            )
                            analytics.logEvent(
                                KBSWAP_ERROR,
                                Bundle().createEvent(ERROR_TEXT, swapError)
                            )
                        }

                        else -> wallet?.let {

                            val gasPriceGwei = getSelectedGasPrice(swap.gas, selectedGasFeeView?.id)
                            val minAcceptedRatePercentage =
                                getMinAcceptedRatePercent(rgRate.checkedRadioButtonId)
                            val data = swap.copy(
                                sourceAmount = (if (sourceAmount.toBigDecimalOrDefaultZero() > BigDecimal.ZERO) sourceAmount else edtSource.text.toString())
                                    ?: "",
                                destAmount = edtDest.text.toString(),
                                minAcceptedRatePercent = minAcceptedRatePercentage,
                                gasPrice = gasPriceGwei
                            )
                            viewModel.verifySwap(it, data, platformFee)
                        }
                    }
                }
            }
            analytics.logEvent(KBSWAP_SWAP_TAPPED, Bundle().createEvent())
            swap?.let {
                analytics.logEvent(
                    KBSWAP_ADVANCED, Bundle().createEvent(
                        listOf(
                            GAS_OPTION, GAS_VALUE, SLIPPAGE
                        ),
                        listOf(
                            getGasPriceOption(selectedGasFeeView?.id),
                            getSelectedGasPrice(it.gas, selectedGasFeeView?.id),
                            getMinAcceptedRatePercent(rgRate.checkedRadioButtonId)
                        )
                    )
                )
            }

        }

        viewModel.checkEligibleWalletCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CheckEligibleWalletState.Loading)
                when (state) {
                    is CheckEligibleWalletState.Success -> {
                        eligibleWalletStatus = state.eligibleWalletStatus
                        if (state.eligibleWalletStatus.success && !state.eligibleWalletStatus.eligible) {
                            binding.tvContinue.setViewEnable(false)
                            showError(state.eligibleWalletStatus.message)
                        } else {
                            onVerifyWalletComplete(state.swap)
                        }
                    }
                    is CheckEligibleWalletState.ShowError -> {
                        onVerifyWalletComplete(state.swap)
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

        currentActivity.mainViewModel.getMaxPriceCallback.observe(
            viewLifecycleOwner,
            Observer { event ->
                event?.peekContent()?.let { state ->
                    when (state) {
                        is GetMaxPriceState.Success -> {
                            maxGasPrice = Convert.fromWei(
                                state.data,
                                Convert.Unit.GWEI
                            ).toDisplayNumber()
                            val currentSwap = binding.swap
                            if (currentSwap != null) {
                                val swap = currentSwap.copy(
                                    gas = currentSwap.gas.copy(maxGasPrice = maxGasPrice)
                                )

                                binding.swap = swap
                                binding.executePendingBindings()
                            }
                        }
                        is GetMaxPriceState.ShowError -> {

                        }
                    }
                }
            })

        binding.imgFlag.setOnClickListener {
            navigator.navigateToNotificationScreen(currentFragment)
        }

        setDefaultSelection()
    }

    fun verifyEligibleWallet(isDisablePopup: Boolean = false) {
        eligibleWalletStatus?.let {
            if (it.success && !it.eligible) {
                if (!isDisablePopup) {
                    binding.tvContinue.setViewEnable(false)
                }
                binding.tvContinue.setViewEnable(false)
                showError(it.message)
            } else {
                binding.tvContinue.setViewEnable(true)
            }
        }
    }

    fun swap(notification: Notification?) {
        if (notification != null) {
            newSwap(notification.data)
        }
    }

    fun newSwap(notificationExt: NotificationExt) {
        if (this.notificationExt != notificationExt) {
            this.notificationExt = notificationExt
        }

        if (notificationExt.alertId > 0) {
            viewModel.getAlert(NotificationAlert(notificationExt))
            wallet?.let { viewModel.getSwapData(it, NotificationAlert(notificationExt)) }
        } else {
            wallet?.let {
                viewModel.getSwapData(
                    it,
                    notificationExt = notificationExt
                )
            }
        }
    }

    private fun updateCurrentFocus(view: EditText?) {
        currentFocus?.isSelected = false
        currentFocus = view
        currentFocus?.isSelected = true
        destLock.set(view == binding.edtDest)
    }

    private fun setDefaultSelection() {
        rbFast.isChecked = true
        rbFast.jumpDrawablesToCurrentState()
        rbDefaultRate.isChecked = true
        rbDefaultRate.jumpDrawablesToCurrentState()
    }

    private fun showDestValueInUsd(swap: Swap) {
        if (swap.tokenDest.rateEthNowOrDefaultValue > BigDecimal.ZERO && edtSource.text?.isNotEmpty() == true) {
            binding.tvValueInUSD.text =
                getString(
                    R.string.dest_balance_usd_format,
                    binding.swap?.getExpectedDestUsdAmount(
                        edtSource.toBigDecimalOrDefaultZero(),
                        swap.tokenDest.rateUsdNow
                    )?.formatDisplayNumber()
                )
        } else {
            binding.tvValueInUSD.text = ""
        }
    }

    fun verifyAmount() {
        binding.swap?.let {
            if (it.isExpectedRateZero && it.isMarketRateZero) {
                showAlertWithoutIcon(
                    message = getString(R.string.reserve_under_maintainance),
                    timeInSecond = 5
                )
            } else if (it.isExpectedRateZero) {
                showAlertWithoutIcon(
                    title = getString(R.string.title_amount_too_big),
                    message = getString(R.string.can_not_handle_amount),
                    timeInSecond = 5
                )
            }
        }
    }

    fun showTutorial() {
        if (activity == null) return
        if (mediator.isShownSwapTutorial()) return
        binding.root.doOnPreDraw {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                val targets = ArrayList<Target>()
                val overlaySwapPairTargetBinding =
                    DataBindingUtil.inflate<LayoutSwapPairTargetBinding>(
                        LayoutInflater.from(activity),
                        R.layout.layout_swap_pair_target,
                        null,
                        false
                    )

                val firstTarget = Target.Builder()
                    .setAnchor(
                        resources.getDimension(R.dimen.tutorial_85_dp),
                        resources.getDimension(R.dimen.tutorial_165_dp)
                    )
                    .setShape(Circle(resources.getDimension(R.dimen.tutorial_90_dp)))
                    .setOverlay(overlaySwapPairTargetBinding.root)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {
                        }

                        override fun onEnded() {

                            mediator.showSwapTutorial(true)
                        }
                    })
                    .build()
                targets.add(firstTarget)

                val overlaySwapAmountTargetBinding =
                    DataBindingUtil.inflate<LayoutSwapInputtedTargetBinding>(
                        LayoutInflater.from(activity),
                        R.layout.layout_swap_inputted_target,
                        null,
                        false
                    )

                val location = IntArray(2)
                val view = binding.tv50Percent
                view.getLocationInWindow(location)
                val x = location[0] + view.width / 2f
                val y = (location[1] + view.height / 2f) * 3 / 4f

                val secondTarget = Target.Builder()
                    .setAnchor(x, y)
                    .setShape(Circle(resources.getDimension(R.dimen.tutorial_120_dp)))
                    .setOverlay(overlaySwapAmountTargetBinding.root)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {
                            binding.edtSource.setText(getString(R.string.tutorial_swap_amount))
                        }

                        override fun onEnded() {
                            binding.edtSource.setText("")
                        }
                    })
                    .build()
                targets.add(secondTarget)

                val overlaySwapAdvanceTarget =
                    DataBindingUtil.inflate<LayoutSwapAdvanceTargetBinding>(
                        LayoutInflater.from(activity),
                        R.layout.layout_swap_advance_target,
                        null,
                        false
                    )


                binding.tvContinue.getLocationInWindow(location)
                val xContinue = (location[0] + binding.tvContinue.width / 2f) / 2f
                val yContinue = (location[1] + binding.tvContinue.height / 2f) * 3 / 4f

                val thirdTarget = Target.Builder()
                    .setAnchor(xContinue, yContinue)
                    .setShape(Circle(resources.getDimension(R.dimen.tutorial_150_dp)))
                    .setOverlay(overlaySwapAdvanceTarget.root)
                    .setOnTargetListener(object : OnTargetListener {
                        override fun onStarted() {
                        }

                        override fun onEnded() {
                            expandableLayout.collapse(true)
                        }
                    })
                    .build()
                targets.add(thirdTarget)

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


                if (currentFragment is SwapFragment) {
                    spotlight?.start()
                } else {
                    spotlight?.finish()
                }

                overlaySwapPairTargetBinding.tvNext.setOnClickListener {
                    spotlight?.next()

                }

                overlaySwapAmountTargetBinding.tvNext.setOnClickListener {
                    expandableLayout.expand(true)
                    handler.postDelayed({
                        spotlight?.next()
                    }, 250)

                }

                overlaySwapAdvanceTarget.tvNext.setOnClickListener {
                    spotlight?.next()
                }
            }, 500)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: WalletChangeEvent) {
        getSwap()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun getSelectedWallet() {
        viewModel.getSelectedWallet()
    }

    fun getSwap() {
        if (notificationExt != null) return

        wallet?.let {
            viewModel.getSwapData(it)
        }
    }

    override fun refresh() {
        getSwap()
    }

    private fun resetAmount() {
        edtSource.setText("")
        sourceAmount = ""
        edtDest.setText("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SWAP_CONFIRM) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    showBroadcast(data.getStringExtra(HASH_PARAM) ?: "")
                    resetInfo()
                }
            }
        }
    }

    private fun resetInfo() {
        handler.postDelayed(
            {
                resetAmount()
                edtSource.clearFocus()
            }, 500
        )
    }

    private fun showBroadcast(hash: String) {
        val context = activity
        if (context is MainActivity) {
            context.showDialog(
                AlertDialogFragment.DIALOG_TYPE_BROADCASTED,
                Transaction(
                    type = Transaction.TransactionType.SWAP,
                    hash = hash
                )
            )
        }
    }

    fun getKyberEnable() {
        viewModel.getKyberStatus()
    }

    private fun enableTokenSearch(isSourceToken: Boolean, isEnable: Boolean) {
        if (isSourceToken) {
            tokenSources.forEach {
                it.isEnabled = isEnable
            }
        } else {
            tokenDests.forEach {
                it.isEnabled = isEnable
            }
        }

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        val image = if (isSourceToken) {
            binding.imgTokenSource
        } else {
            binding.imgTokenDest
        }

        if (isEnable) {
            colorMatrix.setSaturation(1f)
        } else {
            colorMatrix.setSaturation(0.5f)
        }
        val filter = ColorMatrixColorFilter(colorMatrix)
        image.colorFilter = filter

        binding.imgSwap.isEnabled = isEnable
    }

    private fun getRevertNotification(id: Int): String {
        return String.format(
            getString(R.string.rate_revert_notification),
            binding.tvSource.text,
            binding.tvDest.text,
            binding.swap?.rateThreshold(getMinAcceptedRatePercent(id))?.toNumberFormat(),
            binding.swap?.combineRate?.toNumberFormat()
        )
    }

    private fun getMinAcceptedRatePercent(id: Int): String {
        return when (id) {
            R.id.rbCustom -> {
                edtCustom.text.toString()
            }
            else -> DEFAULT_ACCEPT_RATE_PERCENTAGE.toString()
        }
    }

    private fun onVerifyWalletComplete(swap: Swap?) {
        binding.tvContinue.setViewEnable(true)
        viewModel.saveSwap(
            swap, true
        )
    }

    private fun getSelectedGasPrice(gas: Gas, id: Int?): String {
        val value = when (id) {
            R.id.rbSuperFast -> gas.superFast
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast
        }
        return if (value.isBlank()) {
            gas.superFast
        } else value
    }

    private fun getGasPriceOption(id: Int?): String {
        return when (id) {
            R.id.rbSuperFast -> GAS_OPTIONS_SUPER_FAST
            R.id.rbRegular -> GAS_OPTIONS_REGULAR
            R.id.rbSlow -> GAS_OPTIONS_SLOW
            else -> GAS_OPTIONS_FAST
        }
    }

    fun getRate() {
        binding.swap?.let {
            getRate(it)
        }
    }

    private fun getRate(swap: Swap) {
        if (swap.hasSamePair) return
        viewModel.getMarketRate(swap)
        hasExpectedRate = false
        viewModel.getExpectedRate(
            swap,
            edtSource.getAmountOrDefaultValue(),
            platformFee
        )
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        notificationExt = null
        handler.removeCallbacksAndMessages(null)
        spotlight?.finish()
        super.onDestroyView()
    }

    override fun skipTutorial() {
        spotlight?.finish()
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

    companion object {
        private const val ALERT_PARAM = "alert_param"
        private const val NOTIFICATION_PARAM = "notification_param"
        fun newInstance(alert: NotificationAlert?, notification: Notification?) =
            SwapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ALERT_PARAM, alert)
                    putParcelable(NOTIFICATION_PARAM, notification)
                }
            }
    }
}