package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentMarketBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import io.reactivex.disposables.CompositeDisposable
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MarketFragment : BaseFragment() {

    private lateinit var binding: FragmentMarketBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private var spinnerAdapter: CustomSpinnerAdapter? = null

    private var type: Int = LocalLimitOrder.TYPE_BUY

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MarketViewModel::class.java)
    }

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private var currentOrderedView: TextView? = null

    var currentMarket: String? = null

    private val quoteMarket: List<View>
        get() = listOf(binding.imgFav, binding.tvEth, binding.tvWBTC)

    private var currentSelectedQuoteView: View? = null

    private var isSpinnerChecked = false

    private val marketMap: Map<View, String>
        get() = mapOf(
            binding.imgFav to Token.FAV,
            binding.tvEth to Token.ETH_SYMBOL_STAR,
            binding.tvWBTC to Token.WBTC_SYMBOL
        )
    private var markets: List<MarketItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt(LIMIT_ORDER_TYPE) ?: LocalLimitOrder.TYPE_BUY
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMarketBinding.inflate(inflater, container, false)
        if (activity != null) {
            viewModel.getMarkets()
            spinnerAdapter?.setSelected(false)
        }
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
                            viewModel.getMarkets()
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })
        viewModel.getStableQuoteTokens()
        viewModel.getQuotesCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetQuoteTokensState.Success -> {
                        setupSpinner(state.quotes)
                    }

                    is GetQuoteTokensState.ShowError -> {

                    }
                }
            }
        })

        viewModel.saveSelectedMarketCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveSelectedMarketState.Success -> {
                        val act = activity
                        if (act != null) {
                            act.onBackPressed()
                        }
                    }

                    is SaveSelectedMarketState.ShowError -> {

                    }
                }
            }
        })

        binding.rvMarket.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val adapter = MarketAdapter(appExecutors, type == LocalLimitOrder.TYPE_BUY, {
            wallet?.let { wl ->
                viewModel.saveSelectedMarket(wl, it)
            }

        }, {
            viewModel.saveFav(it)
        })
        binding.rvMarket.adapter = adapter
        viewModel.getMarketsCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketsState.Success -> {
                        markets = state.items
                        val selectedMarket = currentMarket ?: Token.ETH_SYMBOL_STAR
                        adapter.submitFilterList(filterMarkets(selectedMarket))
                    }

                    is GetMarketsState.ShowError -> {

                    }
                }

            }
        })

        viewModel.saveFavMarketCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveFavMarketState.Success -> {
                        if (state.fav) {
                            showAlertWithoutIcon(message = getString(R.string.add_fav_success))
                        } else {
                            showAlertWithoutIcon(message = getString(R.string.remove_fav_success))
                        }
                    }
                    is SaveFavMarketState.ShowError -> {

                    }
                }
            }
        })

        viewModel.currentMarketLiveData.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { mk ->
                currentMarket = mk
                adapter.submitFilterList(filterMarkets(mk))
            }

        })
        binding.tvLimitOrderV1.setOnClickListener {
            navigator.navigateToLimitOrderV1(currentFragment)
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvEth.isSelected = true
        currentSelectedQuoteView = binding.tvEth
        currentMarket = marketMap[currentSelectedQuoteView!!]

        quoteMarket.forEach {
            it.setOnClickListener { v ->
                if (v != currentSelectedQuoteView) {
                    currentSelectedQuoteView?.isSelected = false
                    spinnerAdapter?.setSelected(false)
                    v.isSelected = true
                    currentSelectedQuoteView = v
                    spinnerAdapter?.notifyDataSetChanged()
                    viewModel.currentMarketLiveData.value = Event(marketMap[v] ?: "")
                }
            }
        }

        compositeDisposable.add(
            binding.edtSearch.textChanges()
                .skipInitialValue()
                .debounce(
                    250,
                    TimeUnit.MILLISECONDS
                )
                .map {
                    return@map it.trim().toString().toLowerCase(Locale.getDefault())
                }.observeOn(schedulerProvider.ui())
                .subscribe { searchedText ->
                    adapter.submitFilterList(filterMarkets(currentMarket ?: Token.ETH_SYMBOL_STAR))

                })

        binding.flPair.setOnClickListener {
            adapter.apply {
                setOrderBy(togglePairOrder())
                updateOrderDrawable(isAsc, binding.tvPair)
                updateOrderedView(binding.tvPair)
            }
        }

        binding.flPrice.setOnClickListener {
            adapter.apply {
                setOrderBy(togglePriceOrder())
                updateOrderDrawable(isAsc, binding.tvPrice)
                updateOrderedView(binding.tvPrice)
            }

        }

        binding.flVolume.setOnClickListener {
            adapter.apply {
                setOrderBy(toggleVolumeOrder())
                updateOrderDrawable(isAsc, binding.tvVolume)
                updateOrderedView(binding.tvVolume)
            }
        }

        binding.flChange24h.setOnClickListener {
            adapter.apply {
                setOrderBy(toggleChange24hOrder())
                updateOrderDrawable(isAsc, binding.tvChange24h)
                updateOrderedView(binding.tvChange24h)
            }
        }

        currentOrderedView = binding.tvPair
        updateOrderDrawable(false, binding.tvPair)
    }

    private fun updateOrderedView(view: TextView) {
        if (currentOrderedView != view) {
            currentOrderedView?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            currentOrderedView = view
        }
    }

    private fun updateOrderDrawable(isAsc: Boolean, view: TextView) {
        if (isAsc) {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_upward, 0)
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_downward, 0)
        }
    }

    private fun filterMarkets(mk: String): List<MarketItem> {

        val list = if (mk.equals(Token.FAV, true)) {
            markets?.filter { it.isFav }
        } else {
            markets?.filter { it.displayPair.endsWith(mk, true) }
        } ?: listOf()

        return list.filter {
            it.displayPair.contains(binding.edtSearch.text.toString(), true)
        }
    }

    private fun setupSpinner(items: List<String>) {
        val act = activity
        if (act != null) {
            if (spinnerAdapter == null) {
                spinnerAdapter = CustomSpinnerAdapter(
                    act,
                    android.R.layout.simple_spinner_item,
                    items
                )
            }
            binding.spnStableCoin.adapter = spinnerAdapter

            isSpinnerChecked = false
            binding.spnStableCoin.setOnItemSelectedEvenIfUnchangedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentSelectedQuoteView?.isSelected = false
                    currentSelectedQuoteView = null
                    spinnerAdapter?.setSelected(true)
                    viewModel.currentMarketLiveData.value = Event(items[position])
                }
            })
        }
    }

    override fun onDestroyView() {
        markets = null
        wallet = null
        currentOrderedView = null
        currentMarket = null
        currentSelectedQuoteView = null
        isSpinnerChecked = false
        compositeDisposable.clear()
        super.onDestroyView()
    }

    companion object {
        private const val LIMIT_ORDER_TYPE = "limit_order_type"
        fun newInstance(type: Int) =
            MarketFragment().apply {
                arguments = Bundle().apply {
                    putInt(LIMIT_ORDER_TYPE, type)
                }
            }
    }
}
