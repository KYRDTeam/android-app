package com.kyberswap.android.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.toDisplayNumber
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


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(KyberListViewModel::class.java)
    }

    private val usd: String by lazy {
        getString(R.string.unit_usd)
    }

    private val eth: String by lazy {
        getString(R.string.unit_eth)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
    }

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
        binding.wallet = wallet

        binding.rvToken.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val tokenAdapter = TokenAdapter(appExecutors)
        tokenAdapter.mode = Attributes.Mode.Single
        binding.rvToken.adapter = tokenAdapter

        viewModel.getTokenBalance(wallet!!.address)
        viewModel.getWalletCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        this.wallet = state.wallet
                        if (state.wallet.unit != tvChangeUnit.text.toString()) {
                            tvChangeUnit.text = state.wallet.unit
                            tokenAdapter.showEth(tvChangeUnit.text.toString() == eth)
                        }

                    }
                    is GetWalletState.ShowError -> {
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })


        viewModel.getBalanceStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetBalanceState.Loading)
                when (state) {
                    is GetBalanceState.Success -> {
                        tokenAdapter.submitList(state.tokens)
                        val isETH = wallet!!.unit == eth
                        val balance = calcBalance(state.tokens, isETH)
                        if (balance > BigDecimal.ZERO &&
                            balance.toDisplayNumber() != wallet!!.balance
                        ) {
                            wallet!!.balance = balance.toDisplayNumber()
                            viewModel.updateWallet(wallet!!)
                        }
                    }
                    is GetBalanceState.ShowError -> {
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })

        binding.header.lnChangeUnit.setOnClickListener {
            val unit = toggleUnit(tvChangeUnit.text.toString())
            val balance = calcBalance(tokenAdapter.getData(), unit == eth)
            tvChangeUnit.text = unit
            wallet!!.unit = unit
            wallet!!.balance = balance.toDisplayNumber()
            viewModel.updateWallet(wallet!!)
            tokenAdapter.showEth(unit == eth)
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

    private fun toggleUnit(currentValue: String): String {
        if (currentValue == usd) return eth
        return usd
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            KyberListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
