package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentManageOrderBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class ManageOrderFragment : BaseFragment() {

    private lateinit var binding: FragmentManageOrderBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ManageOrderViewModel::class.java)
    }

    private var currentSelectedView: View? = null

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
        binding = FragmentManageOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rvOrder.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val orderAdapter =
            OrderAdapter(
                appExecutors
            ) {

                dialogHelper.showCancelOrder(it) {
                    viewModel.cancelOrder(it)
                }
            }
        orderAdapter.mode = Attributes.Mode.Single
        binding.rvOrder.adapter = orderAdapter

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvFilter.setOnClickListener {
            navigator.navigateToLimitOrderFilterScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )
        }

        binding.tv1Day.isSelected = true
        currentSelectedView = binding.tv1Day

        listOf(binding.tv1Day, binding.tv1Week, binding.tv1Month, binding.tv3Month)
            .forEach { tv ->
                tv.setOnClickListener {
                    if (currentSelectedView != it) {
                        currentSelectedView?.isSelected = false
                        currentSelectedView = it
                    }
                    it.isSelected = true

                    filterByDate(orderAdapter)
                }
            }

        wallet?.let {
            viewModel.getRelatedOrders(it)
        }

        viewModel.getFilterCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetFilterState.Success -> {
                        orderAdapter.submitList(null)
                        orderAdapter.submitList(
                            viewModel.filterOrders(
                                state.orderFilter
                            )
                        )
                        filterByDate(orderAdapter)
                    }
                    is GetFilterState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })


        viewModel.cancelOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CancelOrdersState.Loading)
                when (state) {
                    is CancelOrdersState.Success -> {

                    }
                    is CancelOrdersState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })
    }

    private fun filterByDate(orderAdapter: OrderAdapter) {
        currentSelectedView?.let {
            orderAdapter.submitList(null)
            orderAdapter.submitList(getFilterList(it.id))
        }
    }

    private fun getFilterList(id: Int): List<Order> {
        val value = when (id) {
            R.id.tv1Day -> {
                DAY_IN_SEC
            }
            R.id.tv1Week -> {
                7 * DAY_IN_SEC
            }
            R.id.tv1Month -> {
                30 * DAY_IN_SEC
            }
            else -> {
                Int.MAX_VALUE
            }
        }

        val now = System.currentTimeMillis() / 1000
        return viewModel.getCurrentFilterList().filter {
            it.createdAt >= now - value
        }

    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        const val DAY_IN_SEC = 24 * 60 * 60
        fun newInstance(wallet: Wallet?) =
            ManageOrderFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
