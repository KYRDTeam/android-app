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
import com.kyberswap.android.databinding.FragmentLimitOrderSuggestionBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.fragment_limit_order.*
import kotlinx.android.synthetic.main.fragment_limit_order.tvRate
import kotlinx.android.synthetic.main.fragment_swap.edtDest
import kotlinx.android.synthetic.main.fragment_swap.edtSource
import kotlinx.android.synthetic.main.fragment_swap.imgMenu
import javax.inject.Inject


class LimitOrderSuggestionFragment : BaseFragment() {

    private lateinit var binding: FragmentLimitOrderSuggestionBinding

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
        binding = FragmentLimitOrderSuggestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walletName = wallet?.name

        viewModel.getLimitOrders(wallet)
        viewModel.getLocalLimitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetLocalLimitOrderState.Success -> {
                        edtSource.setAmount(state.order.srcAmount)
                        edtDest.setAmount(
                            state.order.getExpectedDestAmount(state.order.srcAmount.toBigDecimalOrDefaultZero()).toDisplayNumber()
                        )
                        tvRate.setAmount(state.order.combineRate)
                        wallet?.let { wallet ->
                            viewModel.getRelatedOrders(
                                state.order,
                                wallet
                            )
                
                        binding.order = state.order
                        binding.executePendingBindings()
            
                    is GetLocalLimitOrderState.ShowError -> {

            
        
    
)

        binding.rvRelatedOrder.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        val relatedOrderAdapter =
            OrderAdapter(
                appExecutors
            ) {

    
        relatedOrderAdapter.mode = Attributes.Mode.Single
        binding.rvRelatedOrder.adapter = relatedOrderAdapter

        viewModel.getRelatedOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetRelatedOrdersState.Success -> {
                        relatedOrderAdapter.submitList(state.orders)
            
                    is GetRelatedOrdersState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.tvChangeRate.setOnClickListener {
            activity?.onBackPressed()




        imgMenu.setOnClickListener {
            showDrawer(true)


        binding.tvConfirm.setOnClickListener {
            navigator.navigateToOrderConfirmScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )

    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            LimitOrderSuggestionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
