package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTransactionStatusBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class TransactionStatusFragment : BaseFragment() {

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

    private val emptyTransaction: String
        get() = if (transactionType == Transaction.PENDING) getString(R.string.empty_pending_transaction) else getString(
            R.string.empty_complete_transaction
        )


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionStatusViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
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
        wallet?.let {
            viewModel.getTransactionFilter(transactionType, it)
        }
        binding.wallet = wallet
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

        viewModel.getTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetTransactionState.Success -> {
                        updateTransactionList(state.transactions, state.isFilterChanged)
                        if (state.transactions.isEmpty()) {
                            binding.emptyTransaction = emptyTransaction
                        } else {
                            binding.emptyTransaction = ""
                        }
                    }
                    is GetTransactionState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

    }

    private fun updateTransactionList(
        transactions: List<TransactionItem>,
        isFilterChanged: Boolean
    ) {
        if (isFilterChanged) {
            transactionStatusAdapter?.submitList(null)
        } else {
            transactionStatusAdapter?.submitList(listOf())
        }
        transactionStatusAdapter?.submitList(transactions)
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val TRANSACTION_TYPE = "transaction_type"
        fun newInstance(type: Int, wallet: Wallet?) =
            TransactionStatusFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putInt(TRANSACTION_TYPE, type)
                }
            }
    }
}
