package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentTransactionStatusBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import javax.inject.Inject

class TransactionStatusFragment : BaseFragment() {

    private lateinit var binding: FragmentTransactionStatusBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var transactionStatusAdapter: TransactionStatusAdapter? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionStatusViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
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

        binding.rvTransaction.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        if (transactionStatusAdapter == null)
            transactionStatusAdapter =
                TransactionStatusAdapter(appExecutors) {

                    when {
                        it.displayTransactionType == Transaction.SWAP_TRANSACTION -> navigator.navigateToSwapTransactionScreen(
                            wallet,
                            it
                        )
                        it.displayTransactionType == Transaction.SEND_TRANSACTION -> navigator.navigateToSendTransactionScreen(
                            wallet,
                            it
                        )
                        it.displayTransactionType == Transaction.RECEIVE_TRANSACTION -> navigator.navigateToReceivedTransactionScreen(
                            wallet,
                            it
                        )
            

        
        binding.rvTransaction.adapter = transactionStatusAdapter

        viewModel.getTransaction(wallet!!.address)

        viewModel.getTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetTransactionState.Loading)
                when (state) {
                    is GetTransactionState.Success -> {
                        updateTransactionList(state.transactions)
            
                    is GetTransactionState.ShowError -> {
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)

    }

    private fun updateTransactionList(transactions: List<TransactionItem>) {
        transactionStatusAdapter?.submitList(listOf())
        transactionStatusAdapter?.submitList(transactions)
    }

    override fun showProgress(showProgress: Boolean) {
        progressBar.visibility = if (showProgress) View.VISIBLE else View.GONE
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            TransactionStatusFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
