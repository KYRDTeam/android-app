package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.focusChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentConvertBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.KEEP_ETH_BALANCE_FOR_GAS
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.SwapTokenTransactionState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.fragment_convert.*
import java.math.BigDecimal
import javax.inject.Inject


class ConvertFragment : BaseFragment() {

    private lateinit var binding: FragmentConvertBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var order: LocalLimitOrder? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var pendingBalances: PendingBalances? = null

    var hasUserFocus: Boolean = false

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LimitOrderViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        order = arguments?.getParcelable(LIMIT_ORDER_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.order = order

        viewModel.getGasPrice()
        viewModel.getGasLimit(wallet!!, binding.order!!)

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
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.swapTokenTransactionCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SwapTokenTransactionState.Loading)
                when (state) {
                    is SwapTokenTransactionState.Success -> {
                        showAlert(getString(R.string.swap_done))
                        onBackPressed()
            
                    is SwapTokenTransactionState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        imgBack.setOnClickListener {
            onBackPressed()


        tvCancel.setOnClickListener {
            onBackPressed()


        tvConvert.setOnClickListener {


            binding.order?.let {

                when {
                    binding.edtConvertedAmount.text.isNullOrEmpty() -> {
                        showAlert(getString(R.string.specify_amount))
            
                    binding.edtConvertedAmount.toBigDecimalOrDefaultZero() <
                        it.minConvertedAmount.toBigDecimalOrDefaultZero() -> {
                        showAlertWithoutIcon(
                            message = String.format(
                                getString(R.string.min_eth_amount),
                                it.minConvertedAmount
                            ),
                            title = getString(R.string.invalid_amount)
                        )
            

                    binding.edtConvertedAmount.toBigDecimalOrDefaultZero() >
                        viewModel.calAvailableAmount(
                            it.ethToken,
                            pendingBalances
                        ).toBigDecimalOrDefaultZero() -> {
                        showAlertWithoutIcon(
                            message = getString(R.string.eth_balance_not_enough),
                            title = getString(R.string.insufficient_eth)
                        )
            

                    binding.edtConvertedAmount.toBigDecimalOrDefaultZero() >
                        viewModel.calAvailableAmount(
                            order?.ethToken,
                            pendingBalances
                        ).toBigDecimalOrDefaultZero() -> {
                        showAlertWithoutIcon(
                            message = String.format(
                                getString(R.string.eth_balance_not_enough_for_fee),
                                it.copy(gasLimit = KEEP_ETH_BALANCE_FOR_GAS.toBigInteger())
                                    .displayGasFee
                            ),
                            title = getString(R.string.insufficient_eth)
                        )
            

                    else -> {
                        viewModel.convert(wallet, it)
            
        

    

        wallet?.let {
            viewModel.getPendingBalances(it)


        viewModel.getPendingBalancesCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPendingBalancesState.Success -> {
                        this.pendingBalances = state.pendingBalances
                        setupBalance(state.pendingBalances)
            
                    is GetPendingBalancesState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.compositeDisposable.add(binding.edtConvertedAmount.focusChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    hasUserFocus = it
        
    )

    }

    private fun setupBalance(pendingBalances: PendingBalances) {
        binding.order?.let { order ->
            val availableEth = viewModel.calAvailableAmount(
                order.ethToken,
                pendingBalances
            ).exactAmount()

            if (binding.tvEthBalance.text.toString() != availableEth) {
                binding.tvEthBalance.text =
                    String.format(getString(R.string.eth_balance), availableEth)
    

            val availableWeth = viewModel.calAvailableAmount(
                order.wethToken,
                pendingBalances
            ).exactAmount()

            if (binding.tvWethBalance.text.toString() != availableWeth) {
                binding.tvWethBalance.text =
                    String.format(getString(R.string.weth_balance), availableWeth)
    

            if (!hasUserFocus) {
                val pendingAmount =
                    pendingBalances.data[binding.order?.tokenSource?.symbol] ?: BigDecimal.ZERO
                val minCovertAmount =
                    binding.order?.minConvertedAmount.toBigDecimalOrDefaultZero() + pendingAmount
                if (binding.edtConvertedAmount.text.toString() != minCovertAmount.toDisplayNumber()) {
                    binding.edtConvertedAmount.setAmount(minCovertAmount.toDisplayNumber())
        
    

    }


    private fun onBackPressed() {
        activity?.onBackPressed()
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val LIMIT_ORDER_PARAM = "limit_order_param"
        fun newInstance(wallet: Wallet?, order: LocalLimitOrder?) =
            ConvertFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putParcelable(LIMIT_ORDER_PARAM, order)
        
    
    }
}
