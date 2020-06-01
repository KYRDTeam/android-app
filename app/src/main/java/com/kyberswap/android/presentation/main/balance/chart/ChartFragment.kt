package com.kyberswap.android.presentation.main.balance.chart

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentChartBinding
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.limitorder.LimitOrderV2Fragment
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.CH_USER_CLICK_BUY_LIMIT_ORDER
import com.kyberswap.android.util.CH_USER_CLICK_SELL_LIMIT_ORDER
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
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

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private var token: Token? = null

    private var wallet: Wallet? = null

    private var chartMarket: String = ""

    private var market: MarketItem? = null

    private val marketChange: BigDecimal
        get() = market?.change.toBigDecimalOrDefaultZero()

    private var orderType: Int = LocalLimitOrder.TYPE_BUY

    private val rateChange: BigDecimal
        get() = if (isSelectedUnitEth) token?.changeEth24h
            ?: BigDecimal.ZERO else token?.changeUsd24h
            ?: BigDecimal.ZERO

    private var currentSelection = 0

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ChartViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    private val isSelectedUnitEth: Boolean
        get() = wallet?.unit == getString(R.string.unit_eth)

    private val tokenRate: String
        get() = if (isEth) token?.displayRateEthNow + " ETH" else token?.displayRateUsdNow + " USD"

    private val marketPriceByOrderType: String?
        get() {
            return if (orderType == LocalLimitOrder.TYPE_SELL) market?.displaySellPrice
            else market?.displayBuyPrice
        }

    private val marketPrice: String
        get() = marketPriceByOrderType + " " + market?.quoteSymbol

    private val isLimitOrder: Boolean
        get() = market != null

    private val rate: String?
        get() = if (isLimitOrder) marketPrice else tokenRate

    private val isEth: Boolean
        get() = if (quoteToken.isEmpty()) isSelectedUnitEth else isEthQuote

    private val quoteToken: String
        get() = chartMarket.split("_").last()

    private val isEthQuote: Boolean
        get() = quoteToken == Token.ETH_SYMBOL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = arguments?.getParcelable(TOKEN_PARAM)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        chartMarket = arguments?.getString(CHART_MARKET_PARAM) ?: ""
        market = arguments?.getParcelable(MARKET_PARAM)
        orderType = arguments?.getInt(ORDER_TYPE) ?: LocalLimitOrder.TYPE_BUY
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
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.peekContent()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        wallet = state.wallet
                        binding.isEth = isEth
                        binding.isLimitOrder = isLimitOrder
                        binding.tvRate.text = rate
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
                ChartType.DAY,
                chartMarket
            ),
            getString(R.string.tab_day)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.WEEK,
                chartMarket
            ),
            getString(R.string.tab_week)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.MONTH,
                chartMarket
            ),
            getString(R.string.tab_month)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.YEAR,
                chartMarket
            ),
            getString(R.string.tab_year)
        )
        chartPagerAdapter.addFragment(
            CandleStickChartFragment.newInstance(
                token,
                ChartType.ALL,
                chartMarket
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
                    binding.rateChange =
                        if (isLimitOrder) marketChange else {
                            if (position == 0) {
                                rateChange
                            } else {
                                chart.changedRate
                            }
                        }
                }
            }
        }

        binding.vpChart.addOnPageChangeListener(listener)

        handler.post {
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

        binding.tvBuyOrder.setOnClickListener {
            if (currentFragment is LimitOrderV2Fragment) {
                (currentFragment as LimitOrderV2Fragment).setSelectedTab(LocalLimitOrder.TYPE_BUY)
                activity?.onBackPressed()
            }
            analytics.logEvent(
                CH_USER_CLICK_BUY_LIMIT_ORDER,
                Bundle().createEvent()
            )
        }

        binding.tvSellOrder.setOnClickListener {
            if (currentFragment is LimitOrderV2Fragment) {
                (currentFragment as LimitOrderV2Fragment).setSelectedTab(LocalLimitOrder.TYPE_SELL)
                activity?.onBackPressed()
            }
            analytics.logEvent(
                CH_USER_CLICK_SELL_LIMIT_ORDER,
                Bundle().createEvent()
            )
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

                        val message =
                            if (isEth) {
                                state.data.eth24hVolume?.toDisplayNumber() ?: ""
                            } else {
                                state.data.usd24hVolume?.toDisplayNumber() ?: ""
                            }
                        if (message.toDoubleSafe() > 0 || isLimitOrder) {
                            binding.tv24hVol.visibility = View.VISIBLE
                            binding.tv24hTitle.visibility = View.VISIBLE
                            binding.tv24hVol.text = if (isLimitOrder) {
                                market?.displayMarketVol
                            } else {
                                String.format(
                                    if (isEth) getString(R.string.token_24h_vol) else getString(
                                        R.string.token_24h_vol_usd
                                    ), message
                                )
                            }
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
            binding.rateChange = if (isLimitOrder) marketChange else {
                if (binding.vpChart.currentItem == 0) {
                    rateChange
                } else {
                    rate
                }
            }
        }
    }

    private fun moveToSwapTab() {
        if (activity is MainActivity) {
            handler.post {
                activity?.bottomNavigation?.currentItem = MainPagerAdapter.SWAP
            }
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
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
        private const val CHART_MARKET_PARAM = "chart_market_param"
        private const val MARKET_PARAM = "market_param"
        private const val ORDER_TYPE = "order_type"
        fun newInstance(
            wallet: Wallet?,
            token: Token?,
            chartMarket: String,
            market: MarketItem?,
            orderType: Int
        ) =
            ChartFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TOKEN_PARAM, token)
                    putParcelable(WALLET_PARAM, wallet)
                    putString(CHART_MARKET_PARAM, chartMarket)
                    putParcelable(MARKET_PARAM, market)
                    putInt(ORDER_TYPE, orderType)
                }
            }
    }
}
