package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentCustomizeGasBinding
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetMaxPriceState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.FAIL_SPEED_UP_TX_EVENT
import com.kyberswap.android.util.USER_CLICK_SUBMIT_SPEED_UP_TX_EVENT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toNumberFormat
import kotlinx.android.synthetic.main.layout_expanable.*
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CustomizeGasFragment : BaseFragment() {

    private lateinit var binding: FragmentCustomizeGasBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var transaction: Transaction? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analytics: FirebaseAnalytics

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var wallet: Wallet? = null

    private var maxGasPrice: String = ""

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(CustomizeGasViewModel::class.java)
    }

    private var selectedGasFeeView: CompoundButton? = null

    private var percentage: BigDecimal = BigDecimal.ZERO

    private val speedUpFee: String?
        get() = transaction?.getFeeFromGwei(
            selectedGasPriceGwei
        )

    private val selectedGasPriceGwei: String
        get() = getSelectedGasPrice(
            binding.gas,
            selectedGasFeeView?.id
        )

    private val currentActivity by lazy {
        activity as MainActivity
    }

    private val selectedGasPrice: BigDecimal?
        get() = if (selectedGasPriceGwei.isEmpty()) null else Convert.toWei(
            selectedGasPriceGwei,
            Convert.Unit.GWEI
        )

    private val currentGasPrice: BigDecimal
        get() = transaction?.gasPrice.toBigDecimalOrDefaultZero()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transaction = arguments?.getParcelable(TRANSACTION_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomizeGasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getSelectedWallet()
        binding.transaction = transaction
        viewModel.getGasPrice()
        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val gas = updateGasPrice(state.gas)
                        if (gas != binding.gas) {
                            binding.gas = gas

                            binding.fee = transaction?.getFeeFromGwei(
                                getSelectedGasPrice(
                                    gas,
                                    selectedGasFeeView?.id
                                )
                            ).toNumberFormat()
                            binding.feeSuperFast =
                                transaction?.getFeeFromGwei(gas.superFast).toNumberFormat()
                            binding.feeFast = transaction?.getFeeFromGwei(gas.fast).toNumberFormat()
                            binding.feeStandard =
                                transaction?.getFeeFromGwei(gas.standard).toNumberFormat()
                            binding.feeSlow = transaction?.getFeeFromGwei(gas.low).toNumberFormat()

                            binding.executePendingBindings()
                        }
                    }
                    is GetGasPriceState.ShowError -> {

                    }
                }
            }
        })


        viewModel.speedUpTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SpeedUpTransactionState.Loading)
                when (state) {
                    is SpeedUpTransactionState.Success -> {
                        if (state.status) {
                            onBackPress()
                        } else {
                            showError(getString(R.string.can_not_speed_up_transaction))
                        }
                    }
                    is SpeedUpTransactionState.ShowError -> {

                        val error = state.message ?: getString(R.string.something_wrong)
                        if (error.contains(getString(R.string.nonce_too_low), true)) {
                            showError(getString(R.string.can_not_speed_up_transaction))
                        } else {
                            showError(error)
                        }
                        analytics.logEvent(
                            FAIL_SPEED_UP_TX_EVENT,
                            Bundle().createEvent(error)
                        )
                    }
                }
            }
        })

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
        setDefaultSelection()

        listOf(binding.rbSuperFast, binding.rbFast, binding.rbRegular, binding.rbSlow).forEach {
            it.setOnCheckedChangeListener { rb, isChecked ->
                if (isChecked) {
                    if (rb != selectedGasFeeView) {
                        selectedGasFeeView?.isChecked = false
                        rb.isSelected = true
                        selectedGasFeeView = rb

                        binding.fee = speedUpFee.toNumberFormat()
                    }
                }

            }
        }

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (state.wallet.address != wallet?.address) {
                            wallet = state.wallet
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        currentActivity.mainViewModel.getMaxPriceCallback.observe(
            viewLifecycleOwner,
            Observer { event ->
                event?.peekContent()?.let { state ->
                    when (state) {
                        is GetMaxPriceState.Success -> {
                            maxGasPrice = Convert.fromWei(
                                state.data,
                                Convert.Unit.GWEI
                            ).toDisplayNumber()
                            val currentGas = binding.gas
                            if (currentGas != null) {
                                val gas = updateGasPrice(currentGas)

                                binding.gas = gas

                                binding.feeSuperFast =
                                    transaction?.getFeeFromGwei(gas.superFast).toNumberFormat()
                                binding.feeFast =
                                    transaction?.getFeeFromGwei(gas.fast).toNumberFormat()
                                binding.feeStandard =
                                    transaction?.getFeeFromGwei(gas.standard).toNumberFormat()
                                binding.feeSlow =
                                    transaction?.getFeeFromGwei(gas.low).toNumberFormat()

                                binding.executePendingBindings()
                            }
                        }
                        is GetMaxPriceState.ShowError -> {

                        }
                    }
                }
            })

        binding.tvGasFee.setOnClickListener {
            dialogHelper.showBottomSheetGasFeeDialog(getString(R.string.gas_fee_explanation))
        }

        binding.tvDone.setOnClickListener {
            selectedGasPrice?.let { price ->
                transaction?.let { tx ->
                    wallet?.let { wallet ->
                        percentage =
                            if (currentGasPrice == BigDecimal.ZERO) {
                                BigDecimal.ZERO
                            } else {
                                (price - currentGasPrice).divide(
                                    currentGasPrice,
                                    10,
                                    RoundingMode.HALF_EVEN
                                )
                            }

                        if (percentage < 0.1.toBigDecimal()) {
                            showError(getString(R.string.speed_up_gas_price_hint))
                        } else {
                            analytics.logEvent(
                                USER_CLICK_SUBMIT_SPEED_UP_TX_EVENT,
                                Bundle().createEvent(tx.displayTransaction)
                            )
                            viewModel.speedUp(
                                wallet,
                                tx.copy(gasPrice = price.toDisplayNumber())
                            )
                        }

                    }

                }
            }
        }
    }

    private fun updateGasPrice(currentGas: Gas): Gas {
        return if (maxGasPrice.toBigDecimalOrDefaultZero() >= currentGas.fast.toBigDecimalOrDefaultZero()) {
            currentGas.copy(
                maxGasPrice = maxGasPrice
            )
        } else {
            currentGas.copy(
                maxGasPrice = maxGasPrice,
                fast = maxGasPrice,
                standard = maxGasPrice,
                low = maxGasPrice,
                default = maxGasPrice
            )
        }
    }

    private fun getSelectedGasPrice(gas: Gas?, id: Int?): String {
        if (gas == null) return ""
        return when (id) {
            R.id.rbSuperFast -> gas.superFast
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast
        }
    }

    private fun onBackPress() {
        activity?.onBackPressed()
    }

    private fun setDefaultSelection() {
        binding.rbFast.isChecked = true
        binding.rbFast.jumpDrawablesToCurrentState()
        selectedGasFeeView = rbFast
    }

    companion object {
        private const val TRANSACTION_PARAM = "transaction_param"
        fun newInstance(transaction: Transaction) =
            CustomizeGasFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TRANSACTION_PARAM, transaction)
                }
            }
    }
}

