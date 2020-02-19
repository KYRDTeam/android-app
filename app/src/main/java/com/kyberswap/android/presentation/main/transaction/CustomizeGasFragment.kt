package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentCustomizeGasBinding
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.FAIL_SPEED_UP_TX_EVENT
import com.kyberswap.android.util.USER_CLICK_SUBMIT_SPEED_UP_TX_EVENT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.layout_expanable.*
import org.web3j.utils.Convert
import java.math.BigDecimal
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

    private var wallet: Wallet? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(CustomizeGasViewModel::class.java)
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

    private val selectedGasPrice: BigDecimal
        get() = Convert.toWei(selectedGasPriceGwei, Convert.Unit.GWEI)

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
                        binding.gas = state.gas

                        binding.fee = transaction?.getFeeFromGwei(
                            getSelectedGasPrice(
                                binding.gas,
                                selectedGasFeeView?.id
                            )
                        )

                        binding.executePendingBindings()
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

                        binding.fee = speedUpFee
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

        binding.tvDone.setOnClickListener {
            selectedGasPrice.let {
                transaction?.let { tx ->
                    wallet?.let { wallet ->
                        percentage =
                            if (currentGasPrice == BigDecimal.ZERO) {
                                BigDecimal.ZERO
                            } else {
                                (selectedGasPrice - currentGasPrice) / currentGasPrice
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
                                tx.copy(gasPrice = selectedGasPrice.toDisplayNumber())
                            )
                        }

                    }

                }
            }
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

