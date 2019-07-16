package com.kyberswap.android.presentation.main.balance

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentBalanceBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.showKeyboard
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_token_header.*
import kotlinx.android.synthetic.main.layout_token_header.view.*
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class BalanceFragment : BaseFragment(), PendingTransactionNotification {

    private lateinit var binding: FragmentBalanceBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    var currentSelectedView: View? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(BalanceViewModel::class.java)
    }

    private val balanceAddress by lazy { listOf(binding.tvAddress, binding.tvQr) }


    private val handler by lazy {
        Handler()
    }

    private var wallet: Wallet? = null

    private val usd by lazy {
        getString(R.string.unit_usd)
    }

    private val eth by lazy {
        getString(R.string.unit_eth)
    }

    private val other by lazy {
        getString(R.string.other)
    }

    private val favourite by lazy {
        getString(R.string.favourite)
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

    private var currentSearchString = ""

    private var tokenAdapter: TokenAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (state.wallet.address != binding.wallet?.address) {
                            viewModel.getTokenBalance(state.wallet.address)
                        }
                        Timber.e(state.wallet.toString())
                        this.wallet = state.wallet
                        binding.tvUnit.text = state.wallet.unit
                        binding.tvBalance.text = state.wallet.displayBalance
                        if (binding.wallet != state.wallet) {
                            binding.wallet = state.wallet
                        }

                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        viewModel.visibilityCallback.observe(viewLifecycleOwner, Observer {
            it?.peekContent()?.let { visibility ->
                hideBalance(visibility)
            }
        })

        binding.tvKyberList.isSelected = true
        currentSelectedView = binding.tvKyberList

        binding.tvKyberList.setOnClickListener {
            if (it.isSelected) return@setOnClickListener
            tokenAdapter?.setTokenType(TokenType.LISTED)
            setSelectedOption(it)
        }

        binding.tvFavOther.setOnClickListener {
            toggleOtherFavDisplay(it as TextView)
        }

        balanceAddress.forEach { view ->
            view.setOnClickListener {
                (activity as MainActivity).getCurrentFragment()
                navigator.navigateToBalanceAddressScreen(
                    (activity as MainActivity).getCurrentFragment()
                )
            }
        }

        binding.imgVisibility.setOnClickListener {
            val selected = !it.isSelected
            it.isSelected = selected
            viewModel.updateVisibility(selected)
        }

        binding.imgMenu.setOnClickListener {
            showDrawer(true)
        }

        listOf(binding.edtSearch, binding.imgSearch).forEach {
            it.setOnClickListener {
                binding.edtSearch.requestFocus()
                it.showKeyboard()
            }

        }

        viewModel.compositeDisposable.add(
            binding.edtSearch.textChanges().skipInitialValue().debounce(
                250,
                TimeUnit.MILLISECONDS
            )
                .map {
                    return@map it.trim().toString().toLowerCase()
                }.observeOn(schedulerProvider.ui())
                .subscribe {
                    viewModel.updateSearchKeyword(it)
                })

        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.edtSearch.clearFocus()
            }
            false
        }

        binding.rvToken.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        tokenAdapter =
            TokenAdapter(appExecutors, handler,
                {
                    navigateToChartScreen(it)
                },
                {
                    if (wallet?.isPromo == true) {
                        moveToSwapTab()
                    } else {
                        wallet?.address?.let { it1 -> viewModel.save(it1, it, false) }
                    }
                },
                {
                    if (wallet?.isPromo == true) {
                        moveToSwapTab()
                    } else {
                        wallet?.address?.let { it1 -> viewModel.save(it1, it, true) }
                    }
                },
                {
                    if (wallet?.isPromo == true && it.tokenSymbol == getString(R.string.promo_source_token)) {
                        showAlertWithoutIcon(message = getString(R.string.can_not_tranfer_token))
                    } else {
                        wallet?.address?.let { it1 -> viewModel.saveSendToken(it1, it) }
                    }

                },
                {
                    viewModel.saveFav(it)
                }
            )
        tokenAdapter?.mode = Attributes.Mode.Single
        binding.rvToken.adapter = tokenAdapter


        tvName.isSelected = true

        viewModel.getBalanceStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetBalanceState.Success -> {
                        if (binding.swipeLayout.isRefreshing) {
                            binding.swipeLayout.isRefreshing = false
                        }
                        tokenAdapter?.setFullTokenList(state.tokens)
                        tokenAdapter?.submitFilterList(
                            getFilterTokenList(
                                currentSearchString,
                                state.tokens
                            )
                        )

                        val isETH = wallet?.unit == eth
                        setCurrencyDisplay(isETH)
                        val balance = calcBalance(state.tokens, isETH)
                        if (balance.toDisplayNumber() != wallet?.balance) {
                            wallet?.balance = balance.toDisplayNumber()
                            viewModel.updateWallet(wallet)
                        }
                    }
                    is GetBalanceState.ShowError -> {

                    }
                }
            }
        })

        viewModel.saveTokenCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveTokenState.Success -> {
                        if (state.fav) {
                            showAlertWithoutIcon(message = getString(R.string.add_fav_success))
                        } else {
                            showAlertWithoutIcon(message = getString(R.string.remove_fav_success))
                        }
                    }
                    is SaveTokenState.ShowError -> {

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

        viewModel.searchedKeywordsCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { searchedString ->
                currentSearchString = searchedString
                if (searchedString.isEmpty()) {
                    tokenAdapter?.let {
                        it.submitFilterList(it.getFullTokenList())
                    }

                } else {
                    tokenAdapter?.let {
                        tokenAdapter?.submitFilterList(
                            getFilterTokenList(
                                searchedString,
                                it.getFullTokenList()
                            )
                        )
                    }
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
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
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
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
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
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })
    }

    private fun hideBalance(isHide: Boolean) {
        binding.tvBalance.text = if (isHide) "******" else binding.wallet?.displayBalance
    }

    override fun showNotification(showNotification: Boolean) {
        binding.vNotification.visibility = if (showNotification) View.VISIBLE else View.GONE
    }

    private fun setSelectedOption(view: View) {
        currentSelectedView?.isSelected = false
        view.isSelected = true
        currentSelectedView = view
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
            balance +=
                if (token.currentBalance == BigDecimal.ZERO) {
                    BigDecimal.ZERO
                } else {
                    token.currentBalance.multiply(
                        if (isETH) {
                            token.rateEthNow
                        } else {
                            token.rateUsdNow
                        }
                    )
                }
        }
        return balance

    }

    override fun onDestroy() {
        viewModel.compositeDisposable.clear()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
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
    }


    private fun setCurrencyDisplay(isEth: Boolean) {
        binding.header.tvEth.isSelected = isEth
        binding.header.tvUsd.isSelected = !isEth
    }

    private fun toggleOtherFavDisplay(view: TextView) {
        if (view.isSelected) {
            view.text =
                if (view.text == other) favourite else other
        }

        if (view.text == other) {
            tokenAdapter?.setTokenType(TokenType.OTHER)
        } else {
            tokenAdapter?.setTokenType(TokenType.FAVOURITE)
        }

        setSelectedOption(view)
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
        fun newInstance() = BalanceFragment()
    }
}
