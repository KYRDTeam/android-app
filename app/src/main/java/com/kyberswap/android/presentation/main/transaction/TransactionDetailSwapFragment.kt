package com.kyberswap.android.presentation.main.transaction

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTransactionDetailSwapBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import javax.inject.Inject


class TransactionDetailSwapFragment : BaseFragment() {

    private lateinit var binding: FragmentTransactionDetailSwapBinding

    @Inject
    lateinit var navigator: Navigator

    private var wallet: Wallet? = null

    private var transaction: Transaction? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
        transaction = arguments!!.getParcelable(TRANSACTION_PARAM)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionDetailSwapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.transaction = transaction

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.imgCopy.setOnClickListener {
            val clipboard =
                context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("Copy", transaction?.hash)
            clipboard!!.primaryClip = clip
            showAlert(getString(R.string.txhash_copy))
        }

        binding.imgEtherscan.setOnClickListener {
            openUrl(getString(R.string.transaction_etherscan_endpoint_url) + transaction?.hash)
        }
        binding.imgKyber.setOnClickListener {
            openUrl(getString(R.string.transaction_kyber_endpoint_url) + transaction?.hash)
        }

        binding


    }

    private fun openUrl(url: String?) {
        if (url.isNullOrEmpty()) return
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }


    override fun showProgress(showProgress: Boolean) {
        progressBar.visibility = if (showProgress) View.VISIBLE else View.GONE
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val TRANSACTION_PARAM = "transaction_param"
        fun newInstance(wallet: Wallet?, transaction: Transaction?) =
            TransactionDetailSwapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putParcelable(TRANSACTION_PARAM, transaction)
                }
            }
    }
}
