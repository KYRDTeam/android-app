package com.kyberswap.android.presentation.main.alert


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentPriceAlertBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import java.math.BigDecimal
import javax.inject.Inject


class PriceAlertFragment : BaseFragment() {

    private lateinit var binding: FragmentPriceAlertBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var alert: Alert? = null

    private var currentAlertNumber: Int = 0

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private var clearAlertPrice = false

    private val handler by lazy {
        Handler()
    }


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PriceAlertViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alert = arguments?.getParcelable(ALERT_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPriceAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (this.wallet?.address != state.wallet.address) {
                            this.wallet = state.wallet
                            viewModel.getCurrentAlert(state.wallet.address, alert)
                        }
                    }
                    is GetWalletState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.getCurrentAlertCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetCurrentAlertState.Success -> {
                        if (state.alert != binding.alert) {
                            binding.alert = state.alert
                            updateAlert()
                            setupCurrency()
                        }
                    }
                    is GetCurrentAlertState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.getAllAlert()
        viewModel.getAllAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertsState.Success -> {
                        if (currentAlertNumber != state.alerts.size) {
                            currentAlertNumber = state.alerts.size
                        }
                    }
                    is GetAlertsState.ShowError -> {

                    }
                }
            }
        })

        viewModel.createOrUpdateAlertCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CreateOrUpdateAlertState.Loading)
                when (state) {
                    is CreateOrUpdateAlertState.Success -> {
                        onCompleted()
                    }
                    is CreateOrUpdateAlertState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.compositeDisposable.add(binding.rgCurrencies.checkedChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                updateAlert()
                if (clearAlertPrice) {
                    binding.edtRate.text.clear()
                }
            })

        viewModel.compositeDisposable.add(binding.edtRate.textChanges()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.ratePercentage =
                    it.toString().percentage(price?.toPlainString()).toDisplayNumber()
            })

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.imgDone.setOnClickListener {
            val alert = binding.alert
            if (currentAlertNumber > Alert.MAX_ALERT_NUMBER) {
                dialogHelper.showExceedNumberAlertDialog {

                }
            } else {
                viewModel.createOrUpdateAlert(
                    alert?.copy(
                        base = unit,
                        alertPrice = binding.edtRate.toBigDecimalOrDefaultZero(),
                        isAbove = binding.ratePercentage.toBigDecimalOrDefaultZero() > BigDecimal.ZERO
                    )
                )
            }
        }

        binding.vChangeToken.setOnClickListener {
            navigator.navigateToTokenSelection(
                currentFragment, wallet
            )
        }
    }

    private fun onCompleted() {
        activity?.onBackPressed()
    }

    private fun setupCurrency() {
        val ethBase = this.alert?.isEthBase ?: false
        binding.rgCurrencies.check(
            if (ethBase) R.id.rbEth else
                R.id.rbUsd
        )
        binding.alert?.let {
            binding.rbEth.isEnabled =
                !(it.token.isETH || it.token.isWETH)
        }

        handler.post {
            clearAlertPrice = true
        }
    }

    private fun updateAlert() {
        binding.tvToken.text = StringBuilder()
            .append(binding.alert?.tokenSymbol)
            .append("/")
            .append(unit)
            .toString()

        binding.tvCurrentPrice.text = String.format(
            getString(R.string.alert_current_price),
            price?.toDisplayNumber()
        )
    }

    val unit: String
        get() = if (binding.rgCurrencies.checkedRadioButtonId == R.id.rbEth) getString(R.string.unit_eth) else getString(
            R.string.unit_usd
        )

    private val price: BigDecimal?
        get() = if (binding.rgCurrencies.checkedRadioButtonId == R.id.rbEth) binding.alert?.token?.rateEthNow else binding.alert?.token?.rateUsdNow


    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        private const val ALERT_PARAM = "alert_param"
        fun newInstance(alert: Alert?) =
            PriceAlertFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ALERT_PARAM, alert)
                }
            }
    }


}
