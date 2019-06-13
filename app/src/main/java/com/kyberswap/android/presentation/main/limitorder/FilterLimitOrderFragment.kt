package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderFilterBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_limit_order_filter.*
import javax.inject.Inject


class FilterLimitOrderFragment : BaseFragment() {

    private lateinit var binding: FragmentLimitOrderFilterBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var orderFilter: OrderFilter? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(FilterViewModel::class.java)
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

        wallet?.let { viewModel.getFilter(it) }

        binding.rvTokenPair.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvTokenPair.adapter = tokenPairAdapter

        val addressAdapter = FilterItemAdapter(appExecutors) {

        }

        binding.rvAddress.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvAddress.adapter = addressAdapter

        val statusAdapter = FilterItemAdapter(appExecutors) {

        }

        binding.rvStatus.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvStatus.adapter = statusAdapter

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.getFilterStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetFilterState.Success -> {
                        this.orderFilter = state.orderFilter
                        rgOrder.check(if (state.orderFilter.oldest) R.id.rbOldest else R.id.rbLatest)
                        tokenPairAdapter.submitList(state.orderFilter.listOrders)
                        addressAdapter.submitList(state.orderFilter.listAddress)
                        statusAdapter.submitList(state.orderFilter.listStatus)

                    }
                    is GetFilterState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        binding.tvApply.setOnClickListener {
            orderFilter?.apply {
                listAddress = addressAdapter.getData()
                listOrders = tokenPairAdapter.getData()
                listStatus = statusAdapter.getData()
            }

            orderFilter?.let {
                viewModel.saveOrderFilter(it)
            }
        }

        viewModel.saveFilterStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveFilterState.Success -> {
                        onSaveFilterSuccess()

                    }
                    is SaveFilterState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        binding.tvReset.setOnClickListener {
            orderFilter?.apply {
                listAddress = listOf()
                listOrders = listOf()
                listStatus = listOf()
            }

            orderFilter?.let {
                viewModel.saveOrderFilter(it)
            }
        }
    }

    private fun onSaveFilterSuccess() {
        activity?.onBackPressed()
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
