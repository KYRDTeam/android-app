package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTokenSearchBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.balance.GetBalanceState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.presentation.main.swap.TokenSearchLimitOrderAdapter
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.hideKeyboard
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LimitOrderTokenSearchFragment : BaseFragment() {
    private lateinit var binding: FragmentTokenSearchBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private var wallet: Wallet? = null

    private var isSourceToken: Boolean? = null

    private var currentSearchString = ""

    private var tokenList = mutableListOf<Token>()


    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)
            .get(LimitOrderTokenSearchViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        isSourceToken = arguments?.getBoolean(TARGET_PARAM, false)
        wallet?.let {
            viewModel.getPendingBalances(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTokenSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvToken.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        val tokenAdapter =
            TokenSearchLimitOrderAdapter(appExecutors) { token ->
                viewModel.saveTokenSelection(wallet!!.address, token, isSourceToken ?: false)

            }
        binding.rvToken.adapter = tokenAdapter

        viewModel.getTokenListCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetBalanceState.Loading)
                when (state) {
                    is GetBalanceState.Success -> {
                        val combineList = state.tokens.toMutableList()
                        val ethToken = combineList.find { it.isETH }
                        val wethToken = combineList.find { it.isWETH }

                        val ethBalance = ethToken?.currentBalance ?: BigDecimal.ZERO
                        val wethBalance = wethToken?.currentBalance ?: BigDecimal.ZERO

                        combineList.remove(ethToken)
                        combineList.remove(wethToken)

                        val pendingAmountEth =
                            state.pendingBalances?.data?.get(ethToken?.tokenSymbol)
                                ?: BigDecimal.ZERO

                        val pendingAmountWeth =
                            state.pendingBalances?.data?.get(wethToken?.tokenSymbol)
                                ?: BigDecimal.ZERO

                        val combineToken = wethToken?.copy(
                            tokenSymbol = getString(R.string.token_eth_star),
                            tokenName = getString(R.string.token_eth_star_name),
                            limitOrderBalance = ethBalance.plus(wethBalance).minus(pendingAmountEth).minus(
                                pendingAmountWeth
                            )
                        )?.updateBalance(
                            ethBalance.plus(wethBalance)
                        )

                        combineToken?.let { token -> combineList.add(0, token) }
                        tokenList.clear()
                        tokenList.addAll(combineList)
                        updateFilterListToken(currentSearchString, tokenAdapter)

                    }
                    is GetBalanceState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.saveLimitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapDataState.Loading)
                when (state) {
                    is SaveSwapDataState.Success -> {
                        onSelectionComplete()
                    }
                    is SaveSwapDataState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.compositeDisposable.add(
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
                    currentSearchString = searchedText
                    updateFilterListToken(currentSearchString, tokenAdapter)
                })

        binding.imgBack.setOnClickListener {
            hideKeyboard()
            activity?.onBackPressed()
        }

    }

    private fun onSelectionComplete() {
        hideKeyboard()
        activity?.onBackPressed()
    }

    private fun updateFilterListToken(
        searchedText: String?,
        tokenAdapter: TokenSearchLimitOrderAdapter
    ) {
        if (searchedText.isNullOrEmpty()) {
            tokenAdapter.submitFilterList(tokenList)
        } else {
            tokenAdapter.submitFilterList(
                getFilterTokenList(
                    currentSearchString,
                    tokenList
                )
            )
        }
    }


    private fun getFilterTokenList(searchedString: String, tokens: List<Token>): List<Token> {
        return tokens.filter { token ->
            token.tokenSymbol.toLowerCase(Locale.getDefault()).contains(searchedString) or
                token.tokenName.toLowerCase(Locale.getDefault()).contains(searchedString)
        }
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        super.onDestroyView()
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val TARGET_PARAM = "target_param"
        fun newInstance(wallet: Wallet?, isSourceToken: Boolean) =
            LimitOrderTokenSearchFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putBoolean(TARGET_PARAM, isSourceToken)

                }
            }
    }
}