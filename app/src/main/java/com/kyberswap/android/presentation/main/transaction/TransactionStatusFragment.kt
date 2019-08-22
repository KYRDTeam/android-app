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
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTransactionStatusBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
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

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        wallet = state.wallet
                        binding.wallet = wallet
                        getTransactionsByFilter()
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

        viewModel.getTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetTransactionState.Loading -> {
                        showProgress(!isPending)
                    }
                    is GetTransactionState.Success -> {
                        updateTransactionList(state.transactions, state.isFilterChanged)
                        if (state.isLoaded) {
                            showProgress(false)
                            if (state.transactions.isEmpty() && transactionStatusAdapter?.itemCount == 0) {
                                binding.emptyTransaction = emptyTransaction
                            } else {
                                binding.emptyTransaction = ""
                            }
                        }
                    }
                    is GetTransactionState.ShowError -> {
                        showProgress(false)
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
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
    }

    private fun setupTransactionList() {
        binding.rvTransaction.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        if (transactionStatusAdapter == null) {
            transactionStatusAdapter = TransactionStatusAdapter(appExecutors) {

                when {
                    it.type == Transaction.TransactionType.SWAP ->
                        navigator.navigateToSwapTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                    it.type == Transaction.TransactionType.SEND ->
                        navigator.navigateToSendTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                    it.type == Transaction.TransactionType.RECEIVED ->
                        navigator.navigateToReceivedTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                }

            }
        }
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
        transactions: List<TransactionItem>,
        isFilterChanged: Boolean
    ) {
        if (isFilterChanged) {
            transactionStatusAdapter?.submitList(null)
        }
        transactionStatusAdapter?.submitList(listOf())
        transactionStatusAdapter?.submitList(transactions)
    }

    override fun showProgress(showProgress: Boolean) {
        handler.post { binding.swipeLayout.isRefreshing = showProgress }
    }

    override fun onDestroyView() {
        viewModel.onCleared()
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
