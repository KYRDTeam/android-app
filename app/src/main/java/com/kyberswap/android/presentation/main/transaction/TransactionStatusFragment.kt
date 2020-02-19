package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.daimajia.swipe.util.Attributes
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTransactionStatusBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.FAIL_CANCEL_TX_EVENT
import com.kyberswap.android.util.USER_CLICK_CANCEL_TX_EVENT
import com.kyberswap.android.util.USER_CLICK_SPEED_UP_TX_EVENT
import com.kyberswap.android.util.USER_CLICK_SUBMIT_CANCEL_TX_EVENT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import org.web3j.utils.Convert
import javax.inject.Inject

class TransactionStatusFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: FragmentTransactionStatusBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null
    private var transactionType: Int = Transaction.PENDING

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var transactionStatusAdapter: TransactionStatusAdapter? = null

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val handler by lazy {
        Handler()
    }

    private val isPending: Boolean
        get() = transactionType == Transaction.PENDING

    private val emptyTransaction: String
        get() = if (isPending) getString(R.string.empty_pending_transaction) else getString(
            R.string.empty_complete_transaction
        )

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionStatusViewModel::class.java)
    }

    private var defaultGasPrice: String = 1.toString()

    @Inject
    lateinit var dialogHelper: DialogHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionType = arguments?.getInt(TRANSACTION_TYPE) ?: Transaction.PENDING
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getSelectedWallet()

        wallet?.let {
            getTransactionsByFilter()
        }

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (wallet?.address != state.wallet.address) {
                            wallet = state.wallet
                            getTransactionsByFilter()
                        }
                        binding.wallet = wallet
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        setupTransactionList()



        context?.let {
            binding.swipeLayout.setColorSchemeColors(
                ContextCompat.getColor(
                    it,
                    R.color.colorAccent
                )
            )
        }
        binding.swipeLayout.setOnRefreshListener(this)

        if (isPending) {
            viewModel.getGasPrice()
        }

        viewModel.getTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetTransactionState.Loading -> {
                        showProgress(true)
                    }
                    is GetTransactionState.Success -> {
                        if (state.isLoaded) {
                            showProgress(false)
                        }

                        if (state.isFilterChanged) {
                            transactionStatusAdapter?.submitList(null)
                        }
                        updateTransactionList(state.transactions)
                    }
                    is GetTransactionState.ShowError -> {
                        showProgress(false)
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                    is GetTransactionState.FilterNotChange -> {
                        showProgress(false)
                        if (transactionStatusAdapter?.itemCount == 0) {
                            binding.emptyTransaction = emptyTransaction
                        } else {
                            binding.emptyTransaction = ""
                        }
                    }
                }
            }
        })

        viewModel.deleteTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {

                    is DeleteTransactionState.Success -> {
                        showAlert(getString(R.string.delete_transaction_success))
                    }
                    is DeleteTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private fun setupTransactionList() {
        binding.rvTransaction.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        if (transactionStatusAdapter == null) {
            transactionStatusAdapter = TransactionStatusAdapter(appExecutors, isPending, handler, {
                when (it.transactionType) {
                    Transaction.TransactionType.SWAP ->
                        navigator.navigateToSwapTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                    Transaction.TransactionType.SEND ->
                        navigator.navigateToSendTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                    Transaction.TransactionType.RECEIVED ->
                        navigator.navigateToReceivedTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                }

            }, {
                dialogHelper.showConfirmation(
                    getString(R.string.title_delete),
                    getString(R.string.delete_transaction_warning),
                    {
                        viewModel.deleteTransaction(it)
                    })

            }, {
                analytics.logEvent(
                    USER_CLICK_SPEED_UP_TX_EVENT,
                    Bundle().createEvent()
                )
                navigator.navigateToCustomGas(currentFragment, it)
            }, {

                analytics.logEvent(
                    USER_CLICK_CANCEL_TX_EVENT,
                    Bundle().createEvent()
                )
                val cancelGasPrice =
                    Convert.toWei(defaultGasPrice.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                        .max(
                            it.gasPrice.toBigDecimalOrDefaultZero() * 1.2.toBigDecimal()
                        )

                dialogHelper.showCancelTransactionDialog(it.getFeeFromWei(cancelGasPrice.toDisplayNumber())) {
                    wallet?.let { it1 ->

                        analytics.logEvent(
                            USER_CLICK_SUBMIT_CANCEL_TX_EVENT,
                            Bundle().createEvent(it.displayTransaction)
                        )
                        viewModel.cancelTransaction(
                            it1,
                            it.copy(gasPrice = cancelGasPrice.toDisplayNumber())
                        )
                    }
                }
            })
        }

        viewModel.cancelTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SpeedUpTransactionState.Loading)
                when (state) {
                    is SpeedUpTransactionState.Success -> {
                        if (!state.status) {
                            analytics.logEvent(FAIL_CANCEL_TX_EVENT, Bundle().createEvent())
                            showError(getString(R.string.can_not_cancel_transaction))
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
                            FAIL_CANCEL_TX_EVENT,
                            Bundle().createEvent(state.message)
                        )
                    }
                }
            }
        })

        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        defaultGasPrice = state.gas.fast
                    }
                    is GetGasPriceState.ShowError -> {

                    }
                }
            }
        })

        viewModel.nonceObserver.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { nonce ->
                transactionStatusAdapter?.smallestNonceHash = nonce
                transactionStatusAdapter?.notifyDataSetChanged()

            }
        })

        transactionStatusAdapter?.mode = Attributes.Mode.Single
        binding.rvTransaction.adapter = transactionStatusAdapter
    }

    private fun getTransactionsByFilter(isForceRefresh: Boolean = false) {
        wallet?.let {
            viewModel.getTransactionFilter(transactionType, it, isForceRefresh)
        }
    }

    override fun onRefresh() {
        getTransactionsByFilter(true)
    }

    private fun updateTransactionList(
        transactions: List<TransactionItem>
    ) {
        transactionStatusAdapter?.submitList(listOf())
        transactionStatusAdapter?.submitList(transactions)

        if (transactions.isEmpty()) {
            binding.emptyTransaction = emptyTransaction
        } else {
            binding.emptyTransaction = ""
        }
    }

    override fun showProgress(showProgress: Boolean) {
        handler.post {
            binding.swipeLayout.isRefreshing = showProgress
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        private const val TRANSACTION_TYPE = "transaction_type"
        fun newInstance(type: Int) =
            TransactionStatusFragment().apply {
                arguments = Bundle().apply {
                    putInt(TRANSACTION_TYPE, type)
                }
            }
    }
}
