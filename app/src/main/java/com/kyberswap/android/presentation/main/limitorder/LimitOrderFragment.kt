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
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.presentation.splash.GetWalletState
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
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LimitOrderViewModel::class.java)
    }

    private val handler by lazy { Handler() }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    var hasUserFocus: Boolean = false

    private var userInfo: UserInfo? = null

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
        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (state.wallet.address != wallet?.address) {
                            this.wallet = state.wallet
                            binding.walletName = state.wallet.name
                            viewModel.getLimitOrders(wallet)
                
            
                    is GetWalletState.ShowError -> {

            
        
    
)

        viewModel.getLoginStatus()
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
                

                        viewModel.getGasPrice()
                        viewModel.getGasLimit(wallet, binding.order)
            
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
    


        binding.imgMenu.setOnClickListener {
            showDrawer(true)


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
                getRate(it)
                viewModel.getFee(
                    it,
                    binding.edtSource.text.toString(),
                    binding.edtDest.text.toString(),
                    wallet
                )
                viewModel.saveLimitOrder(it)
    
            binding.setVariable(BR.order, limitOrder)
            binding.executePendingBindings()



        binding.rvRelatedOrder.layoutManager = LinearLayoutManager(
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
        
    
        orderAdapter.mode = Attributes.Mode.Single
        binding.rvRelatedOrder.adapter = orderAdapter

        viewModel.getRelatedOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetRelatedOrdersState.Success -> {
                        orderAdapter.submitList(listOf())
                        orderAdapter.submitList(state.orders)

                        val calAvailableAmount = viewModel.calAvailableAmount(
                            binding.order,
                            state.orders
                        )
                        if (binding.availableAmount != calAvailableAmount
                        ) {
                            binding.availableAmount =
                                calAvailableAmount
                
            
                    is GetRelatedOrdersState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.imgInfo.setOnClickListener {
            showAlert(
                getString(R.string.eth_star_notification),
                R.drawable.ic_confirm_info
            )


        viewModel.getGetNonceStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetNonceState.Success -> {
                        val order = binding.order?.copy(nonce = state.nonce)
                        binding.order = order

            
                    is GetNonceState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.tvManageOrder.setOnClickListener {
            navigator.navigateToManageOrder(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )


        viewModel.compositeDisposable.add(binding.edtRate.focusChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    hasUserFocus = it
        
    )

        viewModel.getGetMarketRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {

                        val order = binding.order?.copy(
                            marketRate = state.rate
                        )

                        if (binding.order != order) {
                            binding.tvRate.text = String.format(
                                getString(R.string.limit_order_current_rate),
                                binding.order?.tokenSource?.tokenSymbol,
                                order?.getExpectedDestAmount(BigDecimal.ONE)?.toDisplayNumber() + binding.order?.tokenDest?.tokenSymbol
                            )

                            if (!hasUserFocus) {
                                binding.edtRate.setAmount(order?.combineRate)
                    
                            binding.order = order
                            binding.executePendingBindings()
                
            
                    is GetMarketRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)


        viewModel.compositeDisposable.add(binding.edtRate.textChanges().skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                val percentage = it.toString().percentage(binding.order?.combineRate).toDouble()
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
                        val order = binding.order?.copy(
                            expectedRate = state.list[0]
                        )

                        binding.tvRate.text = String.format(
                            getString(R.string.limit_order_current_rate),
                            binding.order?.tokenSource?.tokenSymbol,
                            order?.getExpectedDestAmount(BigDecimal.ONE)?.toDisplayNumber() + " " + binding.order?.tokenDest?.tokenSymbol
                        )
                        if (!hasUserFocus) {
                            binding.edtRate.setAmount(binding.order?.combineRate)
                
                        binding.edtDest.setAmount(
                            binding.order?.getExpectedDestAmount(edtSource.toBigDecimalOrDefaultZero())?.toDisplayNumber()
                        )
                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                
            
                    is GetExpectedRateState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        viewModel.getGetGasLimitCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasLimitState.Success -> {
                        val order = binding.order?.copy(
                            gasLimit = state.gasLimit
                        )

                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                
            
                    is GetGasLimitState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val order = binding.order?.copy(
                            gasPrice = state.gas.fast
                        )
                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                
            
                    is GetGasPriceState.ShowError -> {

            
        
    
)

        viewModel.compositeDisposable.add(
            binding.edtRate.textChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->
                    if (text.isNullOrEmpty()) {
                        binding.edtDest.setText("")
            

                    binding.order?.let { order ->
                        if (order.hasSamePair) {
                            edtDest.setText(binding.edtSource.text)
                 else {
                            edtDest.setAmount(
                                order.getExpectedDestAmount(
                                    text.toString().toBigDecimalOrDefaultZero(),
                                    binding.edtSource.toBigDecimalOrDefaultZero()
                                ).toDisplayNumber()
                            )

                            val bindingOrder = binding.order?.copy(
                                srcAmount = edtSource.text.toString(),
                                minRate = edtRate.toBigDecimalOrDefaultZero()
                            )

                            bindingOrder?.let {
                                if (binding.order != bindingOrder) {
                                    binding.order = bindingOrder
                                    viewModel.saveLimitOrder(
                                        it
                                    )
                        
                    
                
            
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
                        edtDest.setAmount(
                            order.getExpectedDestAmount(
                                text.toString()
                                    .toBigDecimalOrDefaultZero()
                            )
                                .toDisplayNumber()
                        )
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

                        wallet?.let { wallet ->

                            val currentOrder = binding.order?.copy(
                                srcAmount = text.toString()
                            )

                            currentOrder?.let {
                                viewModel.getGasLimit(
                                    wallet, it
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

                        val order = binding.order?.copy(fee = state.fee.fee.toBigDecimal())
                        binding.tvFee.text = String.format(
                            getString(R.string.limit_order_fee),
                            edtSource.toBigDecimalOrDefaultZero().times(state.fee.fee.toBigDecimal()).toDisplayNumber(),
                            binding.order?.tokenSource?.tokenSymbol,
                            state.fee.fee.times(100),
                            edtSource.text,
                            binding.order?.tokenSource?.tokenSymbol
                        )
                        if (order != binding.order) {
                            binding.order = order
                            binding.executePendingBindings()
                
            
                    is GetFeeState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.tvDiscount.setOnClickListener {
            moveToSwapTab()


        binding.tvSubmitOrder.setOnClickListener {
            when {
                binding.edtSource.text.isNullOrEmpty() -> {
                    showAlert(getString(R.string.specify_amount))
        
                edtSource.text.toString().toBigDecimalOrDefaultZero() >
                    viewModel.calAvailableAmount(
                        binding.order,
                        orderAdapter.getData()
                    ).toBigDecimalOrDefaultZero() -> {
                    showAlert(getString(R.string.exceed_balance))
        
                binding.order?.hasSamePair == true -> showAlert(getString(R.string.same_token_alert))
                binding.order?.amountTooSmall(edtSource.text.toString()) == true -> {
                    showAlert(getString(R.string.swap_amount_small))
        

                userInfo == null || userInfo!!.uid <= 0 -> {
                    moveToLoginTab()
        


                else -> binding.order?.let {

                    val order = it.copy(
                        srcAmount = edtSource.text.toString(),
                        minRate = edtRate.toBigDecimalOrDefaultZero()
                    )

                    if (binding.order != order) {
                        binding.order = order
            
                    viewModel.saveLimitOrder(
                        order, true
                    )
        
    


        viewModel.saveOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveLimitOrderState.Success -> {
                        if (viewModel.needConvertWETH(binding.order)) {
                            navigator.navigateToConvertFragment(
                                currentFragment, wallet, binding.order
                            )
                 else {
                            if (viewModel.validate(binding.order, orderAdapter.getData())) {
                                navigator.navigateToOrderConfirmScreen(
                                    currentFragment,
                                    wallet
                                )
                     else {
                                navigator.navigateToLimitOrderSuggestionScreen(
                                    currentFragment,
                                    wallet
                                )
                    
                
            
                    is SaveLimitOrderState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            

        
    
)

        viewModel.cancelOrderCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CancelOrdersState.Loading)
                when (state) {
                    is CancelOrdersState.Success -> {

            
                    is CancelOrdersState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        userInfo = state.userInfo
            
                    is UserInfoState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

    }

    private fun moveToSwapTab() {
        if (activity is MainActivity) {
            handler.post {
                activity!!.bottomNavigation.currentItem = MainPagerAdapter.SWAP
    

    }

    private fun moveToLoginTab() {
        if (activity is MainActivity) {
            handler.post {
                activity!!.bottomNavigation.currentItem = MainPagerAdapter.PROFILE
                (currentFragment as? ProfileFragment)?.fromLimitOrder(true)
    

    }

    fun getLoginStatus() {
        viewModel.getLoginStatus()
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
        viewModel.getMarketRate(order)
        viewModel.getExpectedRate(
            order,
            edtSource.getAmountOrDefaultValue()
        )
    }

    companion object {
        fun newInstance() = LimitOrderFragment()
    }
}
