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
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.*
import kotlinx.android.synthetic.main.fragment_swap.*
import kotlinx.android.synthetic.main.layout_expanable.*
import net.cachapa.expandablelayout.ExpandableLayout
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

    private val tokenSources by lazy {
        listOf(binding.imgTokenSource, binding.tvSource)
    }

    private val tokenDests by lazy {
        listOf(binding.imgTokenDest, binding.tvDest)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

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
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        this.wallet = state.wallet
                        if (binding.swap?.walletAddress != state.wallet.address) {
                            val promo = wallet?.promo
                            if (promo != null) {

                                enableTokenSearch(isSourceToken = true, isEnable = false)
                                if (promo.destinationToken.isNotEmpty()) {
                                    enableTokenSearch(isSourceToken = false, isEnable = false)
                                }
                            } else {
                                enableTokenSearch(isSourceToken = true, isEnable = true)
                                enableTokenSearch(isSourceToken = false, isEnable = true)
                            }

                            binding.walletName = state.wallet.name
                            viewModel.getSwapData(state.wallet)
                            viewModel.getCap(state.wallet.address)
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
                        if (binding.swap != state.swap) {
                            if (state.swap.tokenSource.tokenSymbol == state.swap.tokenDest.tokenSymbol) {
                                showAlert(getString(R.string.same_token_alert))
                            }

                            edtSource.setAmount(state.swap.sourceAmount)
                            getRate(state.swap)

                            binding.swap = state.swap
                            binding.executePendingBindings()

                        }
                        viewModel.getGasPrice()
                        viewModel.getGasLimit(wallet, binding.swap)
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
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->
                    if (text.isNullOrEmpty()) {
                        binding.edtDest.setText("")
                    }
                    binding.swap?.let { swap ->
                        if (swap.hasSamePair) {
                            edtDest.setText(text)
                        } else {
                            edtDest.setAmount(swap.getExpectedDestAmount(text.toString().toBigDecimalOrDefaultZero()).toDisplayNumber())
                            viewModel.getExpectedRate(
                                swap,
                                if (text.isNullOrEmpty())
                                    swap.getDefaultSourceAmount(getString(R.string.default_source_amount))
                                        .toDisplayNumber() else
                                    text.toString()
                            )
                            wallet?.let {

                                val updatedSwap = swap.copy(
                                    sourceAmount = edtSource.text.toString(),
                                    minAcceptedRatePercent =
                                    getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                                    gasPrice = getSelectedGasPrice(swap.gas)
                                )
                                binding.swap = updatedSwap
                                viewModel.getGasLimit(
                                    it, updatedSwap
                                )
                            }
                        }
                    }
                })


        tokenSources.forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    currentFragment,
                    wallet,
                    true
                )
            }

        }

        tokenDests.forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    currentFragment,
                    wallet,
                    false
                )
            }
        }

        viewModel.getExpectedRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetExpectedRateState.Success -> {
                        val swap = binding.swap?.copy(
                            expectedRate = state.list[0]
                        )

                        if (swap != null) {
                            edtDest.setAmount(
                                binding.swap?.getExpectedDestAmount(edtSource.toBigDecimalOrDefaultZero())?.toDisplayNumber()
                            )
                            binding.tvValueInUSD.text =
                                getString(
                                    R.string.dest_balance_usd_format,
                                    binding.swap?.getExpectedDestUsdAmount(
                                        edtSource.toBigDecimalOrDefaultZero(),
                                        swap.tokenDest.rateUsdNow
                                    )?.toDisplayNumber()
                                )

                            tvRevertNotification.text =
                                getRevertNotification(rgRate.checkedRadioButtonId)

                            if (swap != binding.swap) {
                                binding.swap = swap
                                binding.executePendingBindings()
                            }

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
                        val swap = binding.swap?.copy(
                            marketRate = state.rate
                        )
                        tvRevertNotification.text =
                            getRevertNotification(rgRate.checkedRadioButtonId)
                        if (swap != binding.swap) {
                            binding.swap = swap
                            binding.executePendingBindings()
                        }
                    }
                    is GetMarketRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
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
                        showAlert(state.message ?: getString(R.string.something_wrong))
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
                        edtCustom.requestFocus()
                    } else {
                        edtCustom.setText("")
                    }

                })

        viewModel.compositeDisposable.add(
            rgRate.checkedChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe { id ->
                    tvRevertNotification.text = getRevertNotification(id)
                })


        viewModel.compositeDisposable.add(
            rgGas.checkedChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.swap?.let { swap ->
                        viewModel.saveSwap(swap.copy(gasPrice = getSelectedGasPrice(swap.gas)))
                    }
                })

        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val swap = binding.swap?.copy(
                            gas = state.gas
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
            edtCustom.textChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    tvRevertNotification.text =
                        getRevertNotification(R.id.rbCustom)
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
                edtSource.text.isNullOrEmpty() -> {
                    val errorAmount = getString(R.string.specify_amount)
                    showAlert(errorAmount)
                }
                edtSource.text.toString().toBigDecimalOrDefaultZero() > binding.swap?.tokenSource?.currentBalance -> {
                    val errorExceedBalance = getString(R.string.exceed_balance)
                    showAlert(errorExceedBalance)
                }
                binding.swap?.hasSamePair == true -> showAlert(getString(R.string.same_token_alert))
                binding.swap?.amountTooSmall(edtSource.text.toString()) == true -> {
                    val amountError = getString(R.string.swap_amount_small)
                    showAlert(amountError)
                }
                else -> binding.swap?.let { swap ->
                    wallet?.let {
                        viewModel.saveSwap(
                            swap.copy(
                                sourceAmount = edtSource.text.toString(),
                                destAmount = edtDest.text.toString(),
                                minAcceptedRatePercent =
                                getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                                gasPrice = getSelectedGasPrice(swap.gas)
                            ), true
                        )
                    }

                }
            }

        }

        rbFast.isChecked = true
        rbDefaultRate.isChecked = true

    }

    private fun resetAmount() {
        edtSource.setText("")
        edtDest.setText("")
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

    private fun getSelectedGasPrice(gas: Gas): String {
        return when (rgGas.checkedRadioButtonId) {
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
        viewModel.compositeDisposable.dispose()
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = SwapFragment()
    }
}
