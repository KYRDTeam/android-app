package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentOrderConfirmBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class OrderConfirmFragment : BaseFragment() {

    private lateinit var binding: FragmentOrderConfirmBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LimitOrderViewModel::class.java)
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
        binding = FragmentOrderConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        viewModel.getLimitOrders(wallet)
        viewModel.getLocalLimitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetLocalLimitOrderState.Success -> {
                        binding.order = state.order
                        binding.executePendingBindings()
                    }
                    is GetLocalLimitOrderState.ShowError -> {

                    }
                }
            }
        })

        binding.imgInfo.setOnClickListener {
            showAlert(
                getString(R.string.limit_order_confirm_info),
                R.drawable.ic_confirm_info
            )
        }

        binding.tvCancel.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvContinue.setOnClickListener {
            viewModel.submitOrder(binding.order, wallet)
        }

        viewModel.submitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SubmitOrderState.Loading)
                when (state) {
                    is SubmitOrderState.Success -> {
                        onSubmitOrderSuccess()
                    }
                    is SubmitOrderState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_confirm_info
                        )
                    }
                }
            }
        })
    }

    private fun onSubmitOrderSuccess() {
        val fm = (activity as MainActivity).getCurrentFragment()?.childFragmentManager
        if (fm != null)
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            OrderConfirmFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
