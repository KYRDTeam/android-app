package com.kyberswap.android.presentation.main.swap

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSwapBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.DEFAULT_ACCEPT_RATE_PERCENTAGE
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.fragment_swap.*
import net.cachapa.expandablelayout.ExpandableLayout
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject


class SwapFragment : BaseFragment() {

    private lateinit var binding: FragmentSwapBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SwapViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSwapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            wallet?.let {
                viewModel.getSwapData(it.address)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walletName = wallet?.name

        binding.tvAdvanceOption.setOnClickListener {
            binding.expandableLayout.expand()
            binding.tvRevertNotification.text =
                getRevertNotification(binding.rgRate.checkedRadioButtonId, binding.swap)
        }
        binding.imgClose.setOnClickListener {
            binding.expandableLayout.collapse()
        }

        listOf(binding.imgTokenSource, binding.tvSource).forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    R.id.swap_container,
                    wallet,
                    true
                )
            }
        }

        listOf(binding.imgTokenDest, binding.tvDest).forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    R.id.swap_container,
                    wallet,
                    false
                )
            }
        }

        listOf(binding.tvTokenBalance, binding.tvTokenBalanceValue).forEach {
            it.setOnClickListener {
                binding.edtSource.setText(binding.tvTokenBalanceValue.text)
            }
        }

        binding.expandableLayout.setOnExpansionUpdateListener { _, state ->
            if (state == ExpandableLayout.State.EXPANDED) {
                val animator = ObjectAnimator.ofInt(
                    binding.scView,
                    "scrollY",
                    binding.tvRevertNotification.bottom
                )

                animator.duration = 300
                animator.interpolator = AccelerateInterpolator()
                animator.start()
            }
        }

        binding.imgMenu.setOnClickListener {
            showDrawer(true)
        }

        binding.imgSwap.setOnClickListener {
            val swap = binding.swap?.swapToken()
            binding.setVariable(BR.swap, swap)
            binding.executePendingBindings()
            swap?.let {
                viewModel.saveSwap(swap)
                getRate(it)
            }
            binding.edtSource.setText("")
            binding.tvRevertNotification.text =
                getRevertNotification(binding.rgRate.checkedRadioButtonId, swap)

        }

        viewModel.compositeDisposable.add(binding.edtSource.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                if (text.isNullOrEmpty()) {
                    binding.edtDest.setText("")
                }
                binding.swap?.let { swapData ->
                    if (swapData.samePair) {
                        edtDest.setText(text)
                    } else {
                        getExpectedRate(
                            swapData,
                            if (text.isNullOrEmpty()) getString(R.string.default_source_amount) else text.toString()
                        )
                        swapData.sourceAmount = text.toString()

                        wallet?.let {
                            viewModel.getGasLimit(it, swapData)
                        }
                    }
                }
            })

        viewModel.getSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {

                        if (binding.swap != state.swap) {
                            if (state.swap.tokenSource.tokenSymbol == state.swap.tokenDest.tokenSymbol) {
                                showAlert(getString(R.string.same_token_alert))
                            }

                            binding.swap = state.swap
                            binding.executePendingBindings()
                            if (state.swap.sourceAmount.toBigIntegerOrDefaultZero()
                                != BigInteger.ZERO
                            ) {
                                binding.edtSource.setText(state.swap.sourceAmount)
                            }
                            getRate(state.swap)

                            binding.swap?.let { swap ->
                                val updatedSwap = swap.copy(
                                    gasPrice = getSelectedGasPrice(
                                        viewModel.gas
                                    ),
                                    minAcceptedRatePercent = getMinAcceptedRatePercent(
                                        binding
                                            .rgRate.checkedRadioButtonId
                                    )
                                )

                                if (updatedSwap != swap) {
                                    viewModel.saveSwap(updatedSwap)
                                }
                            }
                        }

                        viewModel.getGasLimit(wallet!!, binding.swap!!)
                    }
                    is GetSwapState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getExpectedRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetExpectedRateState.Success -> {
                        val swap = binding.swap
                        if (swap != null) {
                            binding.percentageRate = viewModel.ratePercentage
                            if (binding.edtSource.text.isNotBlank()) {
                                binding.edtDest.setText(
                                    swap.getExpectedDestAmount(
                                        binding.edtSource.text.toString()
                                            .toBigDecimalOrDefaultZero()
                                    ).toDisplayNumber()
                                )
                            }
                            binding.tvValueInUSD.text =
                                getString(
                                    R.string.dest_balance_usd_format,
                                    swap.getExpectedDestUsdAmount(
                                        binding.edtSource
                                            .text.toString()
                                            .toBigDecimalOrDefaultZero()
                                    ).toDisplayNumber()
                                )
                        }
                    }
                    is GetExpectedRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.getGetMarketRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {
                        binding.tvRate.text =
                            state.rate.toBigDecimalOrDefaultZero().setScale(2, RoundingMode.UP)
                                .toPlainString()
                        binding.percentageRate = viewModel.ratePercentage
                    }
                    is GetMarketRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        binding.rbFast.isChecked = true
        binding.rbDefaultRate.isChecked = true

        viewModel.compositeDisposable.add(binding.rbCustom.checkedChanges().skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.edtCustom.isEnabled = it
                if (it) {
                    binding.edtCustom.requestFocus()
                } else {
                    binding.edtCustom.setText("")
                }

            })

        viewModel.compositeDisposable.add(binding.rgRate.checkedChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe { id ->
                binding.tvRevertNotification.text = getRevertNotification(id, binding.swap)
            })


        viewModel.compositeDisposable.add(binding.rgGas.checkedChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe { id ->
                binding.swap?.let { swap ->
                    swap.gasPrice = getSelectedGasPrice(viewModel.gas)
                    viewModel.saveSwap(swap)

                }
            })

        viewModel.getGasPrice()
        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        binding.gas = state.gas
                    }
                    is GetGasPriceState.ShowError -> {

                    }
                }
            }
        })

        viewModel.getCap(wallet?.address)
        viewModel.getCapCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetCapState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.compositeDisposable.add(
            binding.edtCustom.textChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.tvRevertNotification.text =
                        getRevertNotification(R.id.rbCustom, binding.swap)
                }
        )

        binding.imgInfo.setOnClickListener {
            showAlert(
                String.format(
                    getString(R.string.swap_rate_notification),
                    binding.swap?.ratePercentage
                        .toBigDecimalOrDefaultZero()
                        .abs()
                        .toDisplayNumber()
                )
            )
        }

        viewModel.saveSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapState.Loading)
                when (state) {
                    is SaveSwapState.Success -> {
                        navigator.navigateToSwapConfirmationScreen(wallet)
                    }
                }
            }
        })

        binding.tvContinue.setOnClickListener {
            when {
                binding.edtSource.text.isNullOrEmpty() -> showAlert(getString(R.string.specify_amount))
                binding.edtSource.text.toString().toBigDecimalOrDefaultZero() > binding.swap?.tokenSource?.currentBalance -> {
                    showAlert(getString(R.string.exceed_balance))
                }
                binding.swap?.samePair == true -> showAlert(getString(R.string.same_token_alert))
                else -> binding.swap?.let { swap ->
                    if (viewModel.verifyCap(
                            binding.edtSource.text.toString().toBigDecimalOrDefaultZero() *
                                swap.tokenSource.rateEthNow
                        )
                    ) {
                        swap.minAcceptedRatePercent =
                            getMinAcceptedRatePercent(binding.rgRate.checkedRadioButtonId)
                        viewModel.updateSwap(swap)
                    }
                }
            }

        }

    }

    private fun getRevertNotification(id: Int, swap: Swap?): String {
        return String.format(
            getString(R.string.rate_revert_notification),
            binding.tvSource.text,
            binding.tvDest.text,
            swap?.rateThreshold(getMinAcceptedRatePercent(id)),
            swap?.displayExpectedRate
        )
    }

    private fun getMinAcceptedRatePercent(id: Int): String {
        return when (id) {
            R.id.rbCustom -> {
                binding.edtCustom.text.toString()
            }
            else -> DEFAULT_ACCEPT_RATE_PERCENTAGE.toString()
        }
    }

    private fun getSelectedGasPrice(gas: Gas): String {
        return when (binding.rgGas.checkedRadioButtonId) {
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast
        }
    }

    private fun getRate(swap: Swap) {
        if (swap.samePair) return
        viewModel.resetRate()
        getMarketRate(
            swap.tokenSource.tokenSymbol,
            swap.tokenDest.tokenSymbol
        )
        getExpectedRate(
            swap,
            if (binding.edtSource.text.isNullOrEmpty() ||
                binding.edtSource.text.toString().toDouble() == 0.0
            ) getString(
                R.string.default_source_amount
            )
            else binding.edtSource.text.toString()
        )
    }


    private fun getExpectedRate(swapData: Swap, amount: String) {
        viewModel.getExpectedRate(
            swapData.walletAddress,
            swapData,
            amount
        )
    }

    private fun getMarketRate(source: String, dest: String) {
        viewModel.getMarketRate(source, dest)
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.dispose()
        super.onDestroyView()
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            SwapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
