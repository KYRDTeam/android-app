package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTransactionStatusBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.layout_token_header.*
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

    private val handler by lazy {
        Handler()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(parentFragment!!, viewModelFactory)
            .get(TransactionStatusViewModel::class.java)
    }

    private val usd: String by lazy {
        getString(R.string.unit_usd)
    }

    private val eth: String by lazy {
        getString(R.string.unit_eth)
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
        binding.wallet = wallet

        binding.rvTransaction.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val transactionStatusAdapter =
            TransactionStatusAdapter(appExecutors, {

    )
        transactionStatusAdapter.mode = Attributes.Mode.Single
        binding.rvTransaction.adapter = transactionStatusAdapter

        viewModel.getWallet(wallet!!.address)
        viewModel.getWalletCallback.observe(parentFragment!!.viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        this.wallet = state.wallet
                        if (state.wallet.unit != tvChangeUnit.text.toString()) {
                            tvChangeUnit.text = state.wallet.unit
                            transactionStatusAdapter.showEth(tvChangeUnit.text.toString() == eth)
                

            
                    is GetWalletState.ShowError -> {
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)

    }


    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }


    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            TransactionStatusFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
