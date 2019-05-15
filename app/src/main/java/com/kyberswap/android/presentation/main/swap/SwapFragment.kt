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
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.swap
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import net.cachapa.expandablelayout.ExpandableLayout
import timber.log.Timber
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walletName = wallet?.name

        binding.tvAdvanceOption.setOnClickListener {
            binding.expandableLayout.expand()
            binding.tvRevertNotification.text =
                getRevertNotification(binding.rgRate.checkedRadioButtonId)
        }
        binding.imgClose.setOnClickListener {
            binding.expandableLayout.collapse()
        }

        binding.lnSource.setOnClickListener {
            navigator.navigateToTokenSearch(R.id.swap_container, wallet, true)
        }

        binding.lnDest.setOnClickListener {
            navigator.navigateToTokenSearch(R.id.swap_container, wallet, false)
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
            binding.edtSource.swap(binding.edtDest)
            swap?.let {
                viewModel.saveSwap(swap)
                getRate(it)
            }

        }

        viewModel.compositeDisposable.add(binding.edtSource.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                binding.swap?.let { swapData ->
                    getExpectedRate(
                        swapData,
                        if (text.isNullOrEmpty()) getString(R.string.default_source_amount) else text.toString()
                    )
                    swapData.sourceAmount = text.toString()
                    viewModel.getGasLimit(wallet?.address, swapData)

                }
            })

        viewModel.getSwapData(wallet!!.address)

        viewModel.getSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {
                        if (binding.swap != state.swap) {
                            binding.swap = state.swap
                            getRate(state.swap)
                        }
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
                        if (swap != null && state.list.isNotEmpty()) {
                            swap.expectedRate = state.list[0]
                            swap.slippageRate = state.list[1]
                            viewModel.expectedRate = state.list[0]
                        }
                        binding.percentageRate = viewModel.ratePercentage()
                        binding.swap = swap
                        binding.edtDest.setText(
                            viewModel.getExpectedDestAmount(binding.edtSource.text)
                                .toPlainString()
                        )
                        if (swap != null) {
                            binding.tvValueInUSD.text =
                                getString(
                                    R.string.dest_balance_usd_format,
                                    viewModel.getExpectedDestUsdAmount(
                                        binding.edtSource.text,
                                        swap.tokenDest.rateUsdNow
                                    ).toPlainString()
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
                        binding.tvRate.text = state.rate
                        viewModel.marketRate = state.rate
                        binding.percentageRate = viewModel.ratePercentage()
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
                binding.tvRevertNotification.text = getRevertNotification(id)
            })


        viewModel.compositeDisposable.add(binding.rgGas.checkedChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe { id ->


            })

        viewModel.getGasPrice()
        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        binding.gas = state.gas
                        binding.swap?.gasPrice = getSelectedGasPrice(state.gas)
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

        viewModel.getEthRateFromSourceToken(binding.swap?.tokenSource?.tokenSymbol)
        viewModel.getEthRateFromSourceTokenCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {

                    }
                }

            }
        })

        viewModel.compositeDisposable.add(
            binding.edtCustom.textChanges().skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.tvRevertNotification.text =
                        getRevertNotification(R.id.rbCustom)
                }
        )

        binding.imgInfo.setOnClickListener {
            showAlert(
                String.format(
                    getString(R.string.swap_rate_notification),
                    viewModel.ratePercentage()
                )
            )
        }

        binding.tvContinue.setOnClickListener {
            binding.swap?.let {
                Timber.e(binding.swap?.gasPrice)
                if (viewModel.verifyCap(
                        binding.edtSource.text.toString().toBigDecimalOrDefaultZero() *
                            it.tokenSource.rateEthNow
                    )
                ) {
                    navigator.navigateToSwapConfirmationScreen()
                }
            }

        }

    }

    private fun getRevertNotification(id: Int): String {
        return when (id) {
            R.id.rbDefaultRate -> {
                String.format(
                    getString(R.string.rate_revert_notification),
                    binding.tvSource.text,
                    binding.tvDest.text,
                    viewModel.defaultRateThreshold,
                    viewModel.expectedRateDisplay
                )
            }

            R.id.rbCustom -> {
                String.format(
                    getString(R.string.rate_revert_notification),
                    binding.tvSource.text,
                    binding.tvDest.text,
                    viewModel.customRateThreshold(binding.edtCustom.text.toString())
                )
            }
            else -> ""
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
        viewModel.resetRate()
        getMarketRate(
            swap.tokenSource.tokenSymbol,
            swap.tokenDest.tokenSymbol
        )
        getExpectedRate(
            swap,
            if (binding.edtSource.text.isNullOrEmpty()) getString(R.string.default_source_amount)
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
