package com.kyberswap.android.presentation.main.swap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSwapBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.swap
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

    private var marketRate: String? = null
    private var expectedRate: String? = null

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
        binding.tvAdvanceOption.setOnClickListener {
            binding.expandableLayout.expand()
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
                binding.scView.postDelayed(
                    { binding.scView.fullScroll(View.FOCUS_DOWN) },
                    300
                )
            }
        }

        binding.imgMenu.setOnClickListener {
            showDrawer(true)
        }

        binding.imgSwap.setOnClickListener {
            val swap = binding.swap?.switch()
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
                        if (swap != null) {
                            swap.expectedRate = state.list[0]
                            swap.slippageRate = state.list[1]
                        }
                        expectedRate = state.list[0]
                        binding.percentageRate = expectedRate.percentage(marketRate)
                        binding.swap = swap
                    }
                    is GetExpectedRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.getGetMarketRateStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {
                        binding.tvRate.text = state.rate
                        marketRate = state.rate
                        binding.percentageRate = expectedRate.percentage(marketRate)
                    }
                    is GetMarketRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })
    }

    private fun getRate(swap: Swap) {
        resetRate()
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

    private fun resetRate() {
        marketRate = null
        expectedRate = null
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
