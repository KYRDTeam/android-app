package com.kyberswap.android.presentation.main.balance.kyberlist

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentKyberListBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.GetBalanceState
import com.kyberswap.android.presentation.main.balance.OrderType
import com.kyberswap.android.presentation.main.balance.TokenAdapter
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_token_header.*
import kotlinx.android.synthetic.main.layout_token_header.view.*
import java.math.BigDecimal
import javax.inject.Inject

class KyberListFragment : BaseFragment() {

    private lateinit var binding: FragmentKyberListBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val handler by lazy {
        Handler()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(parentFragment!!, viewModelFactory)
            .get(KyberListViewModel::class.java)
    }

    private val usd: String by lazy {
        getString(R.string.unit_usd)
    }

    private val eth: String by lazy {
        getString(R.string.unit_eth)
    }

    private val nameAndBal by lazy {
        listOf(binding.header.tvName, binding.header.tvBalance)
    }

    private val orderByOptions by lazy {
        listOf(
            binding.header.tvName,
            binding.header.tvBalance,
            binding.header.tvEth,
            binding.header.tvUsd,
            binding.header.tvChange24h
        )
    }

    private var selectedIndex: Int = 1

    private var orderBySelectedIndex: Int = 0

    private var tokenList = mutableListOf<Token>()
    private var currentSearchString = ""

    private var tokenAdapter: TokenAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKyberListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvToken.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
//        tokenAdapter =
//            TokenAdapter(appExecutors, handler,
//                {
//                    navigateToChartScreen(it)
//                },
//                {
//                    if (wallet?.isPromo == true) {
//                        moveToSwapTab()
//                    } else {
//                        wallet?.address?.let { it1 -> viewModel.save(it1, it, false) }
//                    }
//                },
//                {
//                    if (wallet?.isPromo == true) {
//                        moveToSwapTab()
//                    } else {
//                        wallet?.address?.let { it1 -> viewModel.save(it1, it, true) }
//                    }
//                },
//                {
//                    if (wallet?.isPromo == true && it.tokenSymbol == getString(R.string.promo_source_token)) {
//                        showAlertWithoutIcon(message = getString(R.string.can_not_tranfer_token))
//                    } else {
//                        wallet?.address?.let { it1 -> viewModel.saveSendToken(it1, it) }
//                    }
//
//                }
//            )
        tokenAdapter?.mode = Attributes.Mode.Single
        binding.rvToken.adapter = tokenAdapter

        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(parentFragment!!.viewLifecycleOwner, Observer {
            it?.peekContent()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        this.wallet = state.wallet
                        if (binding.wallet?.address != state.wallet.address) {
                            binding.wallet = state.wallet
//                            if (state.wallet.unit != tvChangeUnit.text.toString()) {
//                                tvChangeUnit.text = state.wallet.unit
//                                tokenAdapter.showEth(tvChangeUnit.text.toString() == eth)
//                            }
                            viewModel.getTokenBalance(state.wallet.address)
                        }

                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })
        tvName.isSelected = true

        viewModel.getBalanceStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetBalanceState.Success -> {
                        binding.swipeLayout.isRefreshing = false
                        tokenList.clear()
                        tokenList.addAll(state.tokens)
                        tokenAdapter?.submitFilterList(
                            getFilterTokenList(
                                currentSearchString,
                                state.tokens
                            )
                        )

                        val isETH = wallet?.unit == eth
                        setCurrencyDisplay(isETH)
                        val balance = calcBalance(state.tokens, isETH)
                        if (balance > BigDecimal(1E-10) &&
                            balance.toDisplayNumber() != wallet?.balance
                        ) {
                            wallet?.balance = balance.toDisplayNumber()
                            viewModel.updateWallet(wallet)
                        }
                    }
                    is GetBalanceState.ShowError -> {

                    }
                }
            }
        })


        binding.header.tvEth.setOnClickListener { view ->
            tokenAdapter?.let {
                orderByCurrency(
                    true,
                    it.toggleEth(),
                    view as TextView
                )
            }
        }

        binding.header.tvUsd.setOnClickListener { view ->
            tokenAdapter?.let {
                orderByCurrency(
                    false,
                    it.toggleUsd(),
                    view as TextView
                )
            }
        }

        binding.header.tvChange24h.setOnClickListener { view ->
            tokenAdapter?.let {
                orderByChange24h(it.toggleChange24h(), view as TextView)
            }

        }

        setNameBalanceSelectedOption(selectedIndex)

        nameAndBal.forEachIndexed { index, view ->
            view.setOnClickListener {
                setNameBalanceSelectedOption(nameAndBal.indexOf(view))
            }
        }

        viewModel.searchedKeywordsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { searchedString ->
                currentSearchString = searchedString
                if (searchedString.isEmpty()) {
                    tokenAdapter?.submitFilterList(tokenList)
                } else {
                    tokenAdapter?.submitFilterList(getFilterTokenList(searchedString, tokenList))
                }
            }
        })

        viewModel.visibilityCallback.observe(viewLifecycleOwner, Observer {
            it?.peekContent()?.let { visibility ->
                tokenAdapter?.hideBalance(visibility)
            }
        })

        binding.swipeLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.saveTokenSelectionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapDataState.Loading)
                when (state) {
                    is SaveSwapDataState.Success -> {
                    }
                    is SaveSwapDataState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
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
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.callbackSaveSend.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSendState.Loading)
                when (state) {
                    is SaveSendState.Success -> {
                        navigateToSendScreen()
                    }
                    is SaveSendState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

    }

    private fun orderByCurrency(isEth: Boolean, type: OrderType, view: TextView) {
        tokenAdapter?.let {
            it.setOrderBy(type)
            val balance = calcBalance(it.getData(), isEth)
            it.showEth(isEth)
            val unit = if (isEth) eth else usd
            val updatedWallet = wallet?.copy(unit = unit, balance = balance.toDisplayNumber())
            if (updatedWallet != wallet) {
                viewModel.updateWallet(updatedWallet)
            }
            updateOrderDrawable(it.isAsc, view)
        }
        setCurrencyDisplay(isEth)
        updateOrderOption(orderByOptions.indexOf(view), view)
    }

    private fun orderByChange24h(type: OrderType, view: TextView) {
        tokenAdapter?.let {
            it.setOrderBy(type)
            updateOrderDrawable(it.isAsc, view)
        }
        updateOrderOption(orderByOptions.indexOf(view), view)
    }

    private fun updateOrderOption(index: Int, view: TextView) {
        if (index != orderBySelectedIndex) {
            toggleDisplay(false, orderByOptions[orderBySelectedIndex])
            orderBySelectedIndex = index
            toggleDisplay(true, view)
        }
    }

    private fun updateOrderDrawable(isAsc: Boolean, view: TextView) {
        if (isAsc) {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_upward, 0)
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_downward, 0)
        }
    }

    private fun navigateToSendScreen() {
        navigator.navigateToSendScreen((activity as MainActivity).getCurrentFragment(), wallet)
    }


    private fun navigateToChartScreen(token: Token?) {
        navigator.navigateToChartScreen(
            (activity as MainActivity).getCurrentFragment(),
            wallet,
            token
        )
    }

    private fun getFilterTokenList(searchedString: String, tokens: List<Token>): List<Token> {
        return tokens.filter { token ->
            token.tokenSymbol.toLowerCase().contains(searchedString) or
                token.tokenName.toLowerCase().contains(searchedString)
        }
    }

    private fun calcBalance(tokens: List<Token>, isETH: Boolean): BigDecimal {
        var balance = BigDecimal.ZERO
        tokens.forEach { token ->
            balance += token.currentBalance.multiply(
                if (isETH) {
                    token.rateEthNow
                } else {
                    token.rateUsdNow
                }
            )
        }
        return balance

    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    private fun moveToSwapTab() {
        if (activity is MainActivity) {
            handler.post {
                activity!!.bottomNavigation.currentItem = 1
            }
        }
    }

    private fun toggleDisplay(isSelected: Boolean, view: TextView) {
        if (view != binding.header.tvEth && view != binding.header.tvUsd) {
            view.isSelected = isSelected
        }
        val drawable = if (isSelected) R.drawable.ic_arrow_downward else 0
        view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
//        if (view == binding.header.tvName) {
//            tokenAdapter?.setOrderBy(OrderType.NAME)
//
//        } else if (view == binding.header.tvBalance) {
//            tokenAdapter?.setOrderBy(OrderType.BALANCE)
//        }
    }


    private fun setCurrencyDisplay(isEth: Boolean) {
        binding.header.tvEth.isSelected = isEth
        binding.header.tvUsd.isSelected = !isEth
    }


    private fun setNameBalanceSelectedOption(index: Int) {
        tokenAdapter?.let {
            toggleDisplay(false, nameAndBal[selectedIndex])
            selectedIndex = if (it.isNotNameBalOrder) {
                index
            } else {
                if (index == selectedIndex) {
                    (selectedIndex + 1) % 2
                } else {
                    index
                }
            }

            val selectedView = nameAndBal[selectedIndex]
            toggleDisplay(true, selectedView)
            if (selectedView == binding.header.tvName) {
                it.setOrderBy(OrderType.NAME)
            } else if (selectedView == binding.header.tvBalance) {
                it.setOrderBy(OrderType.BALANCE)
            }

            updateOrderOption(orderByOptions.indexOf(selectedView), selectedView)
        }
    }

    companion object {
        fun newInstance() = KyberListFragment()
    }
}
