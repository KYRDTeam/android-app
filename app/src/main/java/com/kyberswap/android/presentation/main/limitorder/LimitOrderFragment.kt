package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_limit_order.*
import kotlinx.android.synthetic.main.fragment_swap.edtDest
import kotlinx.android.synthetic.main.fragment_swap.edtSource
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.absoluteValue


class LimitOrderFragment : BaseFragment() {

    private lateinit var binding: FragmentLimitOrderBinding

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

    private val handler by lazy { Handler() }

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
        binding = FragmentLimitOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walletName = wallet?.name

        wallet?.let {
            viewModel.getLimitOrders(it.address)



        viewModel.getLocalLimitOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetLocalLimitOrderState.Success -> {
                        if (binding.order != state.order) {
                            if (state.order.tokenSource.tokenSymbol == state.order.tokenDest.tokenSymbol) {
                                showAlert(getString(R.string.same_token_alert))
                    

                            edtSource.setAmount(state.order.srcAmount)
                            getRate(state.order)

                            binding.order = state.order
                            binding.executePendingBindings()

                            wallet?.let { wallet ->
                                binding.order?.let { order ->
                                    viewModel.getRelatedOrders(
                                        order,
                                        wallet
                                    )
                        
                    


                
            
                    is GetLocalLimitOrderState.ShowError -> {

            
        
    
)

        listOf(binding.imgTokenSource, binding.tvSource).forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromLimitOrder(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet,
                    true
                )
    


        listOf(binding.imgTokenDest, binding.tvDest).forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromLimitOrder(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet,
                    false
                )
    


        binding.grTokenSource.setAllOnClickListener(View.OnClickListener {
            navigator.navigateToTokenSearchFromSwapTokenScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet,
                true
            )
)

        binding.grTokenDest.setAllOnClickListener(View.OnClickListener {
            navigator.navigateToTokenSearchFromSwapTokenScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet,
                false
            )
)

        binding.imgMenu.setOnClickListener {
            showDrawer(true)


        viewModel.getLimitOrders(wallet?.address!!)

        binding.tvSubmitOrder.setOnClickListener {
            binding.order?.let { order ->
                viewModel.updateOrder(
                    order.copy(
                        srcAmount = edtSource.text.toString()
                    )
                )
    

            navigator.navigateToLimitOrderSuggestionScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )


        binding.tvBalance.setOnClickListener {
            binding.edtSource.setAmount(tvBalance.text.toString())


        binding.tv25Percent.setOnClickListener {
            binding.edtSource.setAmount(
                tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.25.toBigDecimal()
                ).toDisplayNumber()
            )


        binding.tv50Percent.setOnClickListener {
            binding.edtSource.setAmount(
                tvBalance.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.5.toBigDecimal()
                ).toDisplayNumber()
            )



        binding.tv100Percent.setOnClickListener {
            binding.edtSource.setAmount(tvBalance.text.toString())



        binding.imgSwap.setOnClickListener {
            resetAmount()
            val limitOrder = binding.order?.swapToken()
            limitOrder?.let {
                viewModel.saveLimitOrder(limitOrder)
                getRate(it)
    
            binding.setVariable(BR.order, limitOrder)
            binding.executePendingBindings()
            viewModel.getFee(
                binding.order,
                binding.edtSource.text.toString(),
                binding.edtDest.text.toString(),
                wallet
            )



        binding.rvRelatedOrder.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val orderAdapter =
            OrderAdapter(
                appExecutors
            ) {

    
        orderAdapter.mode = Attributes.Mode.Single
        binding.rvRelatedOrder.adapter = orderAdapter


        viewModel.getRelatedOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetRelatedOrdersState.Success -> {
                        orderAdapter.submitList(state.orders)
            
                    is GetRelatedOrdersState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.tvManageOrder.setOnClickListener {
            navigator.navigateToManageOrder(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )


        viewModel.getGetMarketRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {
                        binding.tvRate.text = String.format(
                            getString(R.string.limit_order_current_rate),
                            binding.order?.tokenSource?.tokenSymbol,
                            viewModel.getExpectedDestAmount(BigDecimal.ONE).toDisplayNumber() + binding.order?.tokenDest?.tokenSymbol
                        )
                        binding.edtRate.setAmount(viewModel.combineRate)
            
                    is GetMarketRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        viewModel.compositeDisposable.add(binding.edtRate.textChanges().skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                val percentage = it.toString().percentage(viewModel.combineRate).toDouble()
                val rate = when {
                    percentage > 0.0 -> String.format(
                        getString(R.string.limit_order_rate_higher_market),
                        percentage.toString()
                    )
                    percentage == 0.0 -> getString(R.string.limit_order_rate_equal_market)
                    else -> String.format(
                        getString(R.string.limit_order_rate_lower_market),
                        percentage.absoluteValue.toString()
                    )
        
                binding.tvRateWarning.text = rate
    )

        viewModel.getExpectedRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetExpectedRateState.Success -> {
                        binding.tvRate.text = String.format(
                            getString(R.string.limit_order_current_rate),
                            binding.order?.tokenSource?.tokenSymbol,
                            viewModel.getExpectedDestAmount(BigDecimal.ONE).toDisplayNumber() + " " + binding.order?.tokenDest?.tokenSymbol
                        )
                        binding.edtRate.setAmount(viewModel.combineRate)
            
                    is GetExpectedRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        viewModel.compositeDisposable.add(binding.edtSource.textChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe { text ->
                if (text.isNullOrEmpty()) {
                    binding.edtDest.setText("")
        

                binding.order?.let { order ->
                    if (order.hasSamePair) {
                        edtDest.setText(text)
             else {
                        viewModel.getExpectedRate(
                            order,
                            if (text.isNullOrEmpty()) getString(R.string.default_source_amount) else text.toString()
                        )
                        viewModel.getFee(
                            binding.order,
                            binding.edtSource.text.toString(),
                            binding.edtDest.text.toString(),
                            wallet
                        )
            
        
    )



        viewModel.getFee(
            binding.order,
            binding.edtSource.text.toString(),
            binding.edtDest.text.toString(),
            wallet
        )

        viewModel.getFeeCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetFeeState.Success -> {
                        binding.tvFee.text = String.format(
                            getString(R.string.limit_order_fee),
                            edtSource.textToDouble().times(state.fee.fee),
                            binding.order?.tokenSource?.tokenSymbol,
                            state.fee.fee.times(100),
                            edtSource.text,
                            binding.order?.tokenSource?.tokenSymbol
                        )
            
                    is GetFeeState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.tvDiscount.setOnClickListener {
            moveToSwapTab()


    }

    private fun moveToSwapTab() {
        if (activity is MainActivity) {
            handler.post {
                activity!!.bottomNavigation.currentItem = MainPagerAdapter.SWAP
    

    }

    private fun resetAmount() {
        edtSource.setText("")
        edtDest.setText("")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        viewModel.compositeDisposable.dispose()
    }

    private fun getRate(order: LocalLimitOrder) {
        if (order.hasSamePair) return
        viewModel.setDefaultRate(order)
        viewModel.getMarketRate(order)
        viewModel.getExpectedRate(
            order,
            edtSource.getAmountOrDefaultValue()
        )
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            LimitOrderFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
