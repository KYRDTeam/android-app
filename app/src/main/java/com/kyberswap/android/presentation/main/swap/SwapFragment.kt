package com.kyberswap.android.presentation.main.swap

import android.animation.ObjectAnimator
import android.app.Activity
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
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSwapBinding
import com.kyberswap.android.domain.SchedulerProvider
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
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.common.WalletObserver
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.alert.GetAlertState
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
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
import kotlinx.android.synthetic.main.fragment_swap.*
import kotlinx.android.synthetic.main.layout_expanable.*
import net.cachapa.expandablelayout.ExpandableLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


class SwapFragment : BaseFragment(), PendingTransactionNotification, WalletObserver {

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

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var sourceAmount: String? = null

    private val displaySourceAmount: String
        get() = sourceAmount.toBigDecimalOrDefaultZero().toDisplayNumber()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SwapViewModel::class.java)
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
                                if (promo?.destinationToken?.isNotEmpty() == true) {
                                    enableTokenSearch(isSourceToken = false, isEnable = false)
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
                            getRate(state.swap)
                            viewModel.getGasPrice()
                        } else if (currentFragment is SwapFragment) {
                            viewModel.getGasLimit(wallet, binding.swap)
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
                    tvRevertNotification.bottom
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
                                swap.sourceAddress, swap.destAddress, dstAmount.toString()
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
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
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
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
                        }
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
                        edtCustom.setSelection(edtCustom.text.length)
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

                    if (it.isNotEmpty() && it.toString().toInt() > 0 && it.toString().toInt() <= 100) {
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

        binding.edtSource.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyAmount()
            }
            false
        }

        binding.edtDest.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyAmount()
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


        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val swap = binding.swap?.copy(
                            gas = if (wallet?.isPromo == true) state.gas.toPromoGas() else state.gas
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

//        viewModel.getCapCallback.observe(viewLifecycleOwner, Observer {
//            it?.getContentIfNotHandled()?.let { state ->
//                showProgress(state == GetCapState.Loading)
//                when (state) {
//                    is GetCapState.Success -> {
//
//                        if (state.cap.rich) {
//                            showAlertWithoutIcon(
//                                message =
//                                getString(R.string.cap_rich)
//                            )
//                        } else if (state.swap.equivalentETHWithPrecision > state.cap.cap) {
//                            val amount = Convert.fromWei(state.cap.cap, Convert.Unit.ETHER)
//                            showAlertWithoutIcon(
//                                message = String.format(
//                                    getString(R.string.cap_reduce_amount),
//                                    amount.toDisplayNumber()
//                                )
//                            )
//                        } else {
//                            viewModel.saveSwap(
//                                state.swap, true
//                            )
//                        }
//                    }
//                    is GetCapState.ShowError -> {
//                        showError(
//                            state.message ?: getString(R.string.something_wrong)
//                        )
//                    }
//                }
//            }
//        })

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
                R.drawable.ic_info
            )
        }

        viewModel.saveSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapState.Loading)
                when (state) {
                    is SaveSwapState.Success -> {
                        val currentActivity = activity
                        if (currentActivity != null) {
                            val confirmedActivity = when {
                                wallet?.isPromo == true -> when {
                                    wallet?.isPromoPayment == true -> PromoPaymentConfirmActivity.newIntent(
                                        currentActivity,
                                        wallet
                                    )
                                    else -> PromoSwapConfirmActivity.newIntent(
                                        currentActivity,
                                        wallet
                                    )
                                }
                                else -> SwapConfirmActivity.newIntent(currentActivity, wallet)
                            }

                            startActivityForResult(confirmedActivity, SWAP_CONFIRM)
                        }
                    }
                }
            }
        })

        binding.tvContinue.setOnClickListener {
            val swap = binding.swap
            when {
                swap != null -> {
                    when {
                        swap.isExpectedRateZero && swap.isMarketRateZero -> {
                            showAlertWithoutIcon(message = getString(R.string.reserve_under_maintainance))
                        }
                        edtSource.text.isNullOrEmpty() -> {
                            val errorAmount = getString(R.string.specify_amount)
                            showAlertWithoutIcon(
                                title = getString(R.string.invalid_input),
                                message = errorAmount
                            )
                        }
                        edtSource.text.toString().toBigDecimalOrDefaultZero() > swap.tokenSource.currentBalance -> {
                            val errorExceedBalance = getString(R.string.exceed_balance)
                            showAlertWithoutIcon(
                                title = getString(R.string.title_amount_too_big),
                                message = errorExceedBalance
                            )
                        }
                        swap.hasSamePair -> showAlertWithoutIcon(
                            title = getString(R.string.title_unsupported),
                            message = getString(R.string.can_not_swap_same_token)
                        )
                        swap.amountTooSmall(edtSource.text.toString()) && !(swap.tokenSource.rateEthNowOrDefaultValue == BigDecimal.ZERO && !swap.isExpectedRateEmptyOrZero) -> {
                            val amountError = getString(R.string.swap_amount_small)
                            showAlertWithoutIcon(
                                title = getString(R.string.invalid_amount),
                                message = amountError
                            )
                        }
                        swap.copy(
                            gasPrice = getSelectedGasPrice(
                                swap.gas,
                                selectedGasFeeView?.id
                            )
                        ).insufficientEthBalance -> showAlertWithoutIcon(
                            getString(R.string.insufficient_eth),
                            getString(R.string.not_enough_eth_blance)
                        )
                        swap.tokenSource.isETH &&
                            availableAmount < edtSource.toBigDecimalOrDefaultZero() -> {
                            showAlertWithoutIcon(
                                getString(R.string.insufficient_eth),
                                getString(R.string.not_enough_eth_blance)
                            )
                        }
                        rbCustom.isChecked && edtCustom.text.isNullOrEmpty() -> {
                            showAlertWithoutIcon(message = getString(R.string.custom_rate_empty))
                        }
                        swap.isExpectedRateZero -> {
                            showAlertWithoutIcon(
                                title = getString(R.string.title_amount_too_big),
                                message = getString(R.string.can_not_handle_amount)
                            )
                        }
                        else -> wallet?.let {

                            val data = swap.copy(
                                sourceAmount = (if (sourceAmount.toBigDecimalOrDefaultZero() > BigDecimal.ZERO) sourceAmount else edtSource.text.toString())
                                    ?: "",
                                destAmount = edtDest.text.toString(),
                                minAcceptedRatePercent =
                                getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                                gasPrice = getSelectedGasPrice(swap.gas, selectedGasFeeView?.id)
                            )

//                            if (!((data.tokenSource.isETH && data.tokenDest.isWETH) || (data.tokenSource.isWETH && data.tokenDest.isETH))) {
//                                viewModel.getCap(it, data)
//                            } else {
//                                viewModel.saveSwap(
//                                    data, true
//                                )
//                            }
                            viewModel.saveSwap(
                                data, true
                            )
                        }
                    }
                }
            }

        }

        currentActivity.mainViewModel.checkEligibleWalletCallback.observe(
            currentActivity,
            Observer { event ->
                event?.peekContent()?.let { state ->
                    when (state) {
                        is CheckEligibleWalletState.Success -> {
                            if (state.eligibleWalletStatus.success && !state.eligibleWalletStatus.eligible) {
                                binding.tvContinue.setViewEnable(false)
                                showError(state.eligibleWalletStatus.message)
                            } else {
                                binding.tvContinue.setViewEnable(true)
                            }
                        }
                        is CheckEligibleWalletState.ShowError -> {

                        }
                    }
                }
            })

        binding.imgFlag.setOnClickListener {
            navigator.navigateToNotificationcreen(currentFragment)
        }

        setDefaultSelection()
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
        if (swap.tokenDest.rateEthNowOrDefaultValue > BigDecimal.ZERO && edtSource.text.isNotEmpty()) {
            binding.tvValueInUSD.text =
                getString(
                    R.string.dest_balance_usd_format,
                    binding.swap?.getExpectedDestUsdAmount(
                        edtSource.toBigDecimalOrDefaultZero(),
                        swap.tokenDest.rateUsdNow
                    )?.toDisplayNumber()
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
            binding.swap?.rateThreshold(getMinAcceptedRatePercent(id)),
            binding.swap?.combineRate
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

    private fun getSelectedGasPrice(gas: Gas, id: Int?): String {
        return when (id) {
            R.id.rbSuperFast -> gas.superFast
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast
        }
    }

    private fun getRate(swap: Swap) {
        if (swap.hasSamePair) return
        viewModel.getMarketRate(swap)
        viewModel.getExpectedRate(
            swap,
            edtSource.getAmountOrDefaultValue()
        )
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        notificationExt = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
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