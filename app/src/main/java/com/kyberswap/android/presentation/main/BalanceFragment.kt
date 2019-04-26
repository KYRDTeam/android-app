package com.kyberswap.android.presentation.main

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentBalanceBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.toDisplayNumber
import java.math.BigDecimal
import javax.inject.Inject

class BalanceFragment : BaseFragment() {

    private lateinit var binding: FragmentBalanceBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var uri: Uri? = null

    private lateinit var builder: RequestBuilder<PictureDrawable>

    private var balance = BigDecimal.ZERO

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(BalanceViewModel::class.java)
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
        binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.wallet = wallet ?: Wallet()
        binding.rvToken.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        val tokenAdapter = TokenAdapter(appExecutors)
        binding.rvToken.adapter = tokenAdapter
        viewModel.getTokenBalance("0x2262d4f6312805851e3b27c40db2c7282e6e4a49")

        viewModel.getBalanceStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetBalanceState.Loading)
                when (state) {
                    is GetBalanceState.Success -> {
                        val list = tokenAdapter.getData().toMutableSet()
                        list.add(state.token)
                        tokenAdapter.submitList(list.toList())
                        balance += state.token.currentBalance.multiply(state.token.rateEthNow)
                        if (balance > BigDecimal.ZERO) {
                            binding.tvBalance.text = balance.toDisplayNumber()
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
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            BalanceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
