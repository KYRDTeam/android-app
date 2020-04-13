package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderTypeBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.colorRate
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.getAmountOrDefaultValue
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.isSomethingWrongError
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleOrDefaultZero
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class LimitOrderTypeFragment : BaseFragment() {

    private lateinit var binding: FragmentLimitOrderTypeBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LimitOrderTypeViewModel::class.java)
    }

    private var type: Int = LocalLimitOrder.TYPE_BUY

    private var pendingBalances: PendingBalances? = null

    private val srcAmount: String
        get() = binding.edtTotal.text.toString()
    private val dstAmount: String
        get() = binding.edtAmount.text.toString()

    private val tokenSourceSymbol: String?
        get() = binding.order?.tokenSource?.tokenSymbol

    private val tokenDestSymbol: String?
        get() = binding.order?.tokenDest?.tokenSymbol

    val compositeDisposable = CompositeDisposable()

    var hasUserFocus: Boolean? = false

    private val sourceLock = AtomicBoolean()
    private val destLock = AtomicBoolean()

    private var currentFocus: EditText? = null

    private val isDestFocus: Boolean
        get() = currentFocus == binding.edtAmount

    private val isSourceFocus: Boolean
        get() = currentFocus == binding.edtTotal

    private val rateText: String
        get() = binding.edtPrice.text.toString()

    private val rate: String?
        get() = binding.order?.combineRate

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt(LIMIT_ORDER_TYPE) ?: LocalLimitOrder.TYPE_BUY
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLimitOrderTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (!state.wallet.isSameWallet(wallet)) {
                            wallet = state.wallet
                            viewModel.getLimitOrder(wallet, type)
                            viewModel.getSelectedMarket(wallet)
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
                        if (hasUserFocus != true) {
                            binding.edtPrice.setText(if (type == LocalLimitOrder.TYPE_BUY) state.market.displayBuyPrice else state.market.displaySellPrice)
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
                            hasUserFocus = false
                            binding.order = state.order
                            binding.executePendingBindings()
                            viewModel.getPendingBalances(wallet)
                            viewModel.getFee(
                                binding.order,
                                srcAmount,
                                dstAmount,
                                wallet
                            )

                            getRate(state.order)
                            viewModel.getGasPrice()
                            viewModel.getGasLimit(wallet, binding.order)
                            getRelatedOrders()
                            updateBaseTokenToParentFragment(state.order.tokenDest)
                        }
                    }
                    is GetLocalLimitOrderState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getFeeCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showLoading(state == GetFeeState.Loading)
                when (state) {
                    is GetFeeState.Success -> {

                        binding.tvFee.text = String.format(
                            getString(R.string.limit_order_fee),
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

        binding.type = type
        binding.tvManageOrder.setOnClickListener {
            wallet?.let {
                navigator.navigateToManageOrder(currentFragment, wallet)
            }
        }


        compositeDisposable.add(binding.edtPrice.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    hasUserFocus = it
                }
            })

        compositeDisposable.add(binding.edtTotal.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                sourceLock.set(it || isSourceFocus)
                if (it) {
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
                destLock.set(it)
                if (it) {
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
                if (destLock.get()) {
                    when {
                        rateText.toBigDecimalOrDefaultZero() == BigDecimal.ZERO -> binding.edtTotal.setText(
                            ""
                        )
                        text.isNullOrEmpty() -> binding.edtTotal.setText("")
                        else -> binding.edtTotal.setAmount(
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

        compositeDisposable.add(binding.edtTotal.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                if (!sourceLock.get() || isDestFocus) return@subscribe

                binding.order?.let { order ->
                    when {
                        rateText.isEmpty() -> {
                            binding.edtAmount.setText("")

                            viewModel.getExpectedRate(
                                order,
                                if (text.isNullOrEmpty()) getString(R.string.default_source_amount) else text.toString()
                            )
                        }
                        text.isNullOrEmpty() -> binding.edtAmount.setText("")
                        else -> binding.edtAmount.setAmount(
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
                    binding.edtAmount.setText("")
                }
            })

        compositeDisposable.add(
            binding.edtPrice.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->

                    binding.tvRateWarning.colorRate(text.toString().percentage(rate))
                    binding.order?.let { order ->
                        if (isDestFocus) {
                            binding.edtTotal.setAmount(expectedSourceAmount)
                        } else {
                            binding.edtAmount.setAmount(
                                expectedDestAmount
                            )
                        }

                        val bindingOrder = binding.order?.copy(
                            srcAmount = srcAmount,
                            minRate = binding.edtPrice.toBigDecimalOrDefaultZero()
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


                        if (hasUserFocus != true) {
                            binding.edtPrice.setAmount(rate)
                        }

                        if (isDestFocus) {
                            binding.edtTotal.setAmount(expectedSourceAmount)
                        } else {
                            if (binding.edtPrice.text.isNotEmpty()) {
                                binding.edtAmount.setAmount(
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


                        if (hasUserFocus != true) {
                            binding.edtPrice.setAmount(order?.displayMarketRate)
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

    fun getRelatedOrders() {
        binding.order?.let { wallet?.let { it1 -> viewModel.getRelatedOrders(it, it1) } }
    }

    private fun getRate(order: LocalLimitOrder) {
        viewModel.getMarketRate(order)
        viewModel.getExpectedRate(
            order,
            binding.edtAmount.getAmountOrDefaultValue()
        )
    }

    private fun updateCurrentFocus(view: EditText?) {
        currentFocus?.isSelected = false
        currentFocus = view
        currentFocus?.isSelected = true
        sourceLock.set(view == binding.edtTotal)
        destLock.set(view == binding.edtAmount)
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    @Inject
    lateinit var dialogHelper: DialogHelper

    companion object {
        private const val LIMIT_ORDER_TYPE = "limit_order_type"
        fun newInstance(type: Int) =
            LimitOrderTypeFragment().apply {
                arguments = Bundle().apply {
                    putInt(LIMIT_ORDER_TYPE, type)
                }
            }
    }
}
