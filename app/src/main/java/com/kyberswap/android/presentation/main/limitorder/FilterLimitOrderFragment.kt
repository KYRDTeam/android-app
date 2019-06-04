package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentLimitOrderFilterBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.SwapViewModel
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class FilterLimitOrderFragment : BaseFragment() {

    private lateinit var binding: FragmentLimitOrderFilterBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

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
        binding = FragmentLimitOrderFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tokenPairAdapter = FilterItemAdapter(appExecutors) {

        }

        binding.rvTokenPair.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvTokenPair.adapter = tokenPairAdapter
        tokenPairAdapter.submitList(
            listOf(
                "ETH  ➞  KNC",
                "ETH  ➞  DAI",
                "KNC  ➞  DAI",
                "KNC  ➞  ETH",
                "DAI  ➞  KNC"
            )
        )


        val addressAdapter = FilterItemAdapter(appExecutors) {

        }

        binding.rvAddress.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvAddress.adapter = addressAdapter
        addressAdapter.submitList(
            listOf(
                "0xb2745…fa232",
                "0xb2745…fa392",
                "0xb2745…fa445",
                "0xb2745…f5644"


            )
        )


        val statusAdapter = FilterItemAdapter(appExecutors) {

        }

        binding.rvStatus.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvStatus.adapter = statusAdapter
        statusAdapter.submitList(
            listOf(
                "Open",
                "In progress",
                "Filled",
                "Canceled",
                "Invalidated"
            )
        )

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }


    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            FilterLimitOrderFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
