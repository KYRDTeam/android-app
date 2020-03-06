package com.kyberswap.android.presentation.main.balance.chart

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentChartBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleSafe
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import javax.inject.Inject


class ChartFragment : BaseFragment() {

    private lateinit var binding: FragmentChartBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var token: Token? = null

    private var wallet: Wallet? = null

    private val rateChange: BigDecimal
        get() = if (isEth) token?.changeEth24h ?: BigDecimal.ZERO else token?.changeUsd24h
            ?: BigDecimal.ZERO

    private var currentSelection = 0

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ChartViewModel::class.java)
    }

//    private var vol24hData = Data()

//    private val message: String
//        get() {
//            return if (isEth) {
//                vol24hData.eth24hVolume?.toDisplayNumber() ?: ""
//            } else {
//                vol24hData.usd24hVolume?.toDisplayNumber() ?: ""
//            }
//        }

    private val handler by lazy {
        Handler()
    }

    private val isEth: Boolean
        get() = wallet?.unit == getString(R.string.unit_eth)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = arguments!!.getParcelable(TOKEN_PARAM)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(parentFragment!!.viewLifecycleOwner, Observer {
            it?.peekContent()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        wallet = state.wallet
                        binding.isEth = isEth
                        viewModel.getVol24h(token)
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        binding.token = token

        val chartPagerAdapter =
            ChartPagerAdapter(
                childFragmentManager
            )

        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.DAY
            ),
            getString(R.string.tab_day)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.WEEK
            ),
            getString(R.string.tab_week)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.MONTH
            ),
            getString(R.string.tab_month)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.YEAR
            ),
            getString(R.string.tab_year)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.ALL
            ),
            getString(R.string.tab_all)
        )

        binding.vpChart.adapter = chartPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.vpChart)

        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentSelection = position
                val chart = chartPagerAdapter.getRegisteredFragment(position)
                if (chart is CandleStickChartFragment) {
                    binding.rate = if (position == 0) {
                        rateChange
                    } else {
                        chart.changedRate
                    }
                }
            }
        }

        binding.vpChart.addOnPageChangeListener(listener)

        binding.vpChart.post {
            listener.onPageSelected(0)
        }

        binding.tvBuy.setOnClickListener {
            wallet?.let { wallet ->
                token?.let {
                    if (wallet.isPromo) {
                        moveToSwapTab()
                    } else {
                        viewModel.save(wallet.address, token!!, false)
                    }
                }
            }

        }

        binding.tvSell.setOnClickListener {
            wallet?.let { wallet ->
                token?.let {
                    if (wallet.isPromo) {
                        moveToSwapTab()
                    } else {
                        viewModel.save(wallet.address, token!!, true)
                    }

                }
            }
        }

        binding.tvSend.setOnClickListener {
            wallet?.let { wallet ->
                token?.let {
                    if (it.tokenSymbol == getString(R.string.promo_source_token)) {
                        showAlertWithoutIcon(message = getString(R.string.can_not_tranfer_token))
                    } else {
                        viewModel.saveSendToken(wallet.address, it)
                    }

                }
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                p0?.let {
                    currentSelection = p0.position
                }
            }
        })

        viewModel.callbackSaveSend.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSendState.Loading)
                when (state) {
                    is SaveSendState.Success -> {
                        moveToSendScreen()
                    }
                    is SaveSendState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.get24hCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetVol24hState.Success -> {

                        val message = if (isEth) {
                            state.data.eth24hVolume?.toDisplayNumber() ?: ""
                        } else {
                            state.data.usd24hVolume?.toDisplayNumber() ?: ""
                        }
                        if (message.toDoubleSafe() > 0) {
                            binding.tv24hVol.visibility = View.VISIBLE
                            binding.tv24hTitle.visibility = View.VISIBLE
                            binding.tv24hVol.text =
                                String.format(
                                    if (isEth) getString(R.string.token_24h_vol) else getString(
                                        R.string.token_24h_vol_usd
                                    ), message
                                )
                        } else {
                            binding.tv24hVol.visibility = View.INVISIBLE
                            binding.tv24hTitle.visibility = View.INVISIBLE
                        }
                    }
                    is GetVol24hState.ShowError -> {

                    }
                }
            }
        })

        viewModel.callback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapDataState.Loading)
                when (state) {
                    is SaveSwapDataState.Success -> {
                        moveToSwapTab()
                    }
                    is SaveSwapDataState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }


    fun getIndexByChartType(chartType: ChartType): Int {
        return when (chartType) {
            ChartType.DAY -> {
                0
            }
            ChartType.WEEK -> {
                1
            }
            ChartType.MONTH -> {
                2
            }

            ChartType.YEAR -> {
                3
            }

            ChartType.ALL -> {
                4
            }
        }
    }

    fun updateChangeRate(rate: BigDecimal, chartType: ChartType) {
        if (getIndexByChartType(chartType) == currentSelection) {
            binding.rate = if (binding.vpChart.currentItem == 0) {
                rateChange
            } else {
                rate
            }
        }
    }


    private fun moveToSwapTab() {
        if (activity is MainActivity) {
            handler.post {
                activity!!.bottomNavigation.currentItem = MainPagerAdapter.SWAP
            }
        }
    }

    private fun moveToSendScreen() {
        navigator.navigateToSendScreen(
            currentFragment,
            wallet
        )
    }

    companion object {
        private const val TOKEN_PARAM = "token_param"
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?, token: Token?) =
            ChartFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TOKEN_PARAM, token)
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
