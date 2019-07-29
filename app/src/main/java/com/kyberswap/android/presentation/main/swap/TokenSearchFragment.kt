package com.kyberswap.android.presentation.main.swap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.hideKeyboard
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TokenSearchFragment : BaseFragment() {
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

    private var isSend: Boolean? = null

    private var currentSearchString = ""

    private var tokenList = mutableListOf<Token>()


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(TokenSearchViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        isSend = arguments?.getBoolean(SEND_PARAM, false)
        isSourceToken = arguments?.getBoolean(TARGET_PARAM, false)
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
            TokenSearchAdapter(appExecutors) { token ->
                if (token.tokenSymbol == getString(R.string.promo_source_token)) {
                    showAlertWithoutIcon(message = getString(R.string.can_not_tranfer_token))
        

                if (isSend == true) {
                    viewModel.saveSendTokenSelection(wallet!!.address, token)
         else {
                    viewModel.saveTokenSelection(wallet!!.address, token, isSourceToken ?: false)
        
    

        binding.rvToken.adapter = tokenAdapter
        viewModel.getTokenList(wallet!!.address)

        viewModel.getTokenListCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetBalanceState.Loading)
                when (state) {
                    is GetBalanceState.Success -> {
                        tokenList.clear()
                        tokenList.addAll(state.tokens)
                        updateFilterListToken(currentSearchString, tokenAdapter)

            
                    is GetBalanceState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.saveSwapCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapDataState.Loading)
                when (state) {
                    is SaveSwapDataState.Success -> {
                        onSelectionComplete()
            
                    is SaveSwapDataState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.saveSendCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSendState.Loading)
                when (state) {
                    is SaveSendState.Success -> {
                        onSelectionComplete()
            
                    is SaveSendState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.compositeDisposable.add(
            binding.edtSearch.textChanges()
                .skipInitialValue()
                .debounce(
                    250,
                    TimeUnit.MILLISECONDS
                )
                .map {
                    return@map it.trim().toString().toLowerCase()
        .observeOn(schedulerProvider.ui())
                .subscribe { searchedText ->
                    currentSearchString = searchedText
                    updateFilterListToken(currentSearchString, tokenAdapter)
        )

        binding.imgBack.setOnClickListener {
            hideKeyboard()
            activity!!.onBackPressed()



    }

    private fun onSelectionComplete() {
        hideKeyboard()
        activity?.onBackPressed()
    }

    private fun updateFilterListToken(searchedText: String?, tokenAdapter: TokenSearchAdapter) {
        if (searchedText.isNullOrEmpty()) {
            tokenAdapter.submitFilterList(tokenList)
 else {
            tokenAdapter.submitFilterList(
                getFilterTokenList(
                    currentSearchString,
                    tokenList
                )
            )

    }


    private fun getFilterTokenList(searchedString: String, tokens: List<Token>): List<Token> {
        return tokens.filter { token ->
            token.tokenSymbol.toLowerCase().contains(searchedString) or
                token.tokenName.toLowerCase().contains(searchedString)

    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        super.onDestroyView()
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val TARGET_PARAM = "target_param"
        private const val SEND_PARAM = "send_param"
        fun newInstance(wallet: Wallet?, isSend: Boolean, isSourceToken: Boolean) =
            TokenSearchFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putBoolean(SEND_PARAM, isSend)
                    putBoolean(TARGET_PARAM, isSourceToken)

        
    
    }
}