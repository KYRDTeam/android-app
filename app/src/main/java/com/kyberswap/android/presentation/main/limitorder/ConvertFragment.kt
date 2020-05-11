package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding3.view.focusChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentConvertBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.KEEP_ETH_BALANCE_FOR_GAS
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.util.USER_CLICK_COVERT_ETH_WETH
import com.kyberswap.android.util.USER_CLICK_COVERT_ETH_WETH_ERROR
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import kotlinx.android.synthetic.main.fragment_convert.*
import java.math.BigDecimal
import javax.inject.Inject

class ConvertFragment : BaseFragment() {

    private lateinit var binding: FragmentConvertBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private var wallet: Wallet? = null

    private var limitOrder: LocalLimitOrder? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var pendingBalances: PendingBalances? = null

    private var hasUserFocus: Boolean? = false

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LimitOrderViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        limitOrder = arguments?.getParcelable(LIMIT_ORDER_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.order = limitOrder
        wallet?.let {
            viewModel.getPendingBalances(it)
        }

        viewModel.getPendingBalancesCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPendingBalancesState.Success -> {
                        this.pendingBalances = state.pendingBalances
                        setupBalance(state.pendingBalances)
                    }
                    is GetPendingBalancesState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.getGasPrice()
        viewModel.getGasLimit(wallet, binding.order)

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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.convertCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == ConvertState.Loading)
                when (state) {
                    is ConvertState.Success -> {
                        showAlertWithoutIcon(
                            title = getString(R.string.transaction_broadcasted),
                            message = getString(R.string.transaction_broadcasted_message)
                        )
                        hideKeyboard()
                        if (binding.order?.type == LocalLimitOrder.TYPE_LIMIT_ORDER_V1) {
                            navigator.navigateToOrderConfirmScreen(
                                currentFragment,
                                wallet,
                                limitOrder
                            )
                        } else {
                            navigator.navigateToOrderConfirmV2Screen(
                                currentFragment,
                                wallet,
                                limitOrder
                            )
                        }
                    }
                    is ConvertState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        imgBack.setOnClickListener {
            onBackPressed()
        }

        tvCancel.setOnClickListener {
            onBackPressed()
        }


        tvConvert.setOnClickListener {

            var error: String
            binding.order?.let {

                when {
                    binding.edtConvertedAmount.text.isNullOrEmpty() -> {
                        showAlert(getString(R.string.specify_amount))
                    }
                    binding.edtConvertedAmount.toBigDecimalOrDefaultZero() <
                        it.minConvertedAmount.toBigDecimalOrDefaultZero() -> {

                        error = String.format(
                            getString(R.string.min_eth_amount),
                            it.minConvertedAmount
                        )
                        showAlertWithoutIcon(
                            message = error,
                            title = getString(R.string.invalid_amount)
                        )
                        analytics.logEvent(
                            USER_CLICK_COVERT_ETH_WETH_ERROR,
                            Bundle().createEvent(error)
                        )
                    }

                    binding.edtConvertedAmount.toBigDecimalOrDefaultZero() >
                        it.ethToken.currentBalance -> {
                        error = getString(R.string.eth_balance_not_enough)
                        showAlertWithoutIcon(
                            message = error,
                            title = getString(R.string.insufficient_eth)
                        )
                        analytics.logEvent(
                            USER_CLICK_COVERT_ETH_WETH_ERROR,
                            Bundle().createEvent(error)
                        )
                    }

                    binding.edtConvertedAmount.toBigDecimalOrDefaultZero() >
                        it.availableAmountForTransfer(
                            it.ethToken.currentBalance, it.gasPrice.toBigDecimalOrDefaultZero()
                        ) -> {

                        error = String.format(
                            getString(R.string.eth_balance_not_enough_for_fee),
                            it.copy(gasLimit = KEEP_ETH_BALANCE_FOR_GAS.toBigInteger())
                                .displayGasFee
                        )
                        showAlertWithoutIcon(
                            message = error,
                            title = getString(R.string.insufficient_eth)
                        )
                        analytics.logEvent(
                            USER_CLICK_COVERT_ETH_WETH_ERROR,
                            Bundle().createEvent(error)
                        )
                    }

                    else -> {

                        viewModel.convert(
                            wallet,
                            it,
                            binding.edtConvertedAmount.toBigDecimalOrDefaultZero()
                        )

                        analytics.logEvent(
                            USER_CLICK_COVERT_ETH_WETH,
                            Bundle().createEvent()
                        )
                    }
                }

            }
        }

        viewModel.compositeDisposable.add(binding.edtConvertedAmount.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    hasUserFocus = it
                }
            })

    }

    private fun setupBalance(pendingBalances: PendingBalances) {
        binding.order?.let { order ->
            val availableEth = viewModel.calAvailableAmount(
                order.ethToken,
                pendingBalances
            ).exactAmount()

            if (binding.tvEthBalance.text.toString() != availableEth) {
                binding.tvEthBalance.text =
                    String.format(getString(R.string.eth_balance), availableEth)
            }

            val availableWeth = viewModel.calAvailableAmount(
                order.wethToken,
                pendingBalances
            ).exactAmount()

            if (binding.tvWethBalance.text.toString() != availableWeth) {
                binding.tvWethBalance.text =
                    String.format(getString(R.string.weth_balance), availableWeth)
            }


            if (hasUserFocus != true) {
                val pendingAmount =
                    pendingBalances.data[binding.order?.tokenSource?.symbol] ?: BigDecimal.ZERO
                var minCovertAmount =
                    binding.order?.minConvertedAmount.toBigDecimalOrDefaultZero() + pendingAmount

                if (minCovertAmount < BigDecimal.ZERO) minCovertAmount = BigDecimal.ZERO
                if (binding.edtConvertedAmount.text.toString() != minCovertAmount.toPlainString()) {
                    binding.edtConvertedAmount.setAmount(minCovertAmount.stripTrailingZeros().toPlainString())
                }
            }
        }
    }


    private fun onBackPressed() {
        activity?.onBackPressed()
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val LIMIT_ORDER_PARAM = "limit_order_param"
        fun newInstance(wallet: Wallet?, order: LocalLimitOrder?) =
            ConvertFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putParcelable(LIMIT_ORDER_PARAM, order)
                }
            }
    }
}
