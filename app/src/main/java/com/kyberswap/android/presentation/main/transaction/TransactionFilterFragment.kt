package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTransactionFilterBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.util.*
import javax.inject.Inject


class TransactionFilterFragment : BaseFragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: FragmentTransactionFilterBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private val seeMoreToken by lazy {
        getString(R.string.filter_see_more)
    }

    private val seeLessToken by lazy {
        getString(R.string.filter_see_less)
    }

    private var selectedView: View? = null

    private val handler by lazy {
        Handler()
    }

    private val viewTypes by lazy {
        listOf(binding.tvSend, binding.tvReceived, binding.tvSwap)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(TransactionFilterViewModel::class.java)
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
        binding = FragmentTransactionFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        listOf<View>(binding.tvSend, binding.tvReceived, binding.tvSwap).forEach {
            it.setOnClickListener {
                it.isSelected = !it.isSelected
            }
        }

        binding.rvToken.layoutManager = GridLayoutManager(
            activity,
            4
        )

        val adapter = TokenFilterAdapter(appExecutors)
        binding.rvToken.adapter = adapter

        wallet?.let {
            viewModel.getTransactionFilter(it.address)
        }

        viewModel.getTransactionFilterCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetTransactionFilterState.Loading)
                when (state) {
                    is GetTransactionFilterState.Success -> {
                        binding.filter = state.transactionFilter
                        setupFilterType(state.transactionFilter.types)
                        val list = state.tokens
                        adapter.submitFilterList(list)
                        if (list.filter { it.isSelected }.size > list.size / 2) {
                            binding.tvSelectAll.text =
                                getString(R.string.filter_deselect_all)
                        } else {
                            binding.tvSelectAll.text = getString(R.string.filter_select_all)
                        }
                    }
                    is GetTransactionFilterState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.tvSeeMore.setOnClickListener {
            submitTokenList(adapter)
        }

        listOf<View>(binding.edtFrom, binding.edtTo).forEach {
            it.setOnClickListener { view ->
                selectedView = view
                openMaterialDialog()
            }
        }

        binding.tvReset.setOnClickListener {
            val filter = binding.filter?.copy(
                from = "",
                to = "",
                tokens = adapter.getData().map {
                    it.name
                },
                types = viewTypes.map {
                    when (it) {
                        binding.tvSend -> Transaction.TransactionType.SEND
                        binding.tvReceived -> Transaction.TransactionType.RECEIVED
                        else -> Transaction.TransactionType.SWAP
                    }
                }

            )
            filter?.let {
                binding.filter = filter
                binding.executePendingBindings()
                adapter.resetFilter(true)
                binding.tvSelectAll.text = getString(R.string.filter_select_all)
                setupFilterType(filter.types)
            }


        }

        binding.tvApply.setOnClickListener {

            val filter = binding.filter?.copy(
                from = binding.edtFrom.text.toString(),
                to = binding.edtTo.text.toString(),
                tokens = adapter.getData().filter { it.isSelected }.map {
                    it.name
                },
                types = viewTypes.filter {
                    it.isSelected
                }.map {
                    when (it) {
                        binding.tvSend -> Transaction.TransactionType.SEND
                        binding.tvReceived -> Transaction.TransactionType.RECEIVED
                        else -> Transaction.TransactionType.SWAP
                    }
                }
            )

            filter?.let { it1 -> viewModel.saveTransactionFilter(it1) }

        }

        viewModel.saveTransactionFilterCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveTransactionFilterState.Success -> {
                        onSuccess()
                    }
                    is SaveTransactionFilterState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.tvSelectAll.setOnClickListener {
            adapter.resetFilter(isSelectAll)
            toggleSelectAll()
        }

    }

    private fun toggleSelectAll() {
        if (isSelectAll) {
            binding.tvSelectAll.text = getString(R.string.filter_deselect_all)
        } else {
            binding.tvSelectAll.text = getString(R.string.filter_select_all)
        }
    }

    private val isSelectAll: Boolean
        get() = binding.tvSelectAll.text == getString(R.string.filter_select_all)


    private fun onSuccess() {
        activity?.onBackPressed()
    }

    private fun setupFilterType(types: List<Transaction.TransactionType>) {
        binding.tvSend.isSelected = types.contains(Transaction.TransactionType.SEND)
        binding.tvSwap.isSelected = types.contains(Transaction.TransactionType.SWAP)
        binding.tvReceived.isSelected = types.contains(Transaction.TransactionType.RECEIVED)
    }


    private fun openMaterialDialog() {
        val now = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(
            this,
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show(fragmentManager, "Datepickerdialog")
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        selectedView?.let {
            (selectedView as? EditText)?.setText(
                String.format(
                    getString(R.string.date_format_yyyy_mm_dd),
                    year,
                    monthOfYear + 1,
                    dayOfMonth
                )
            )
        }
    }

    private fun submitTokenList(adapter: TokenFilterAdapter) {
        val text = binding.tvSeeMore.text.toString()
        if (text == seeMoreToken) {
            adapter.setFullMode(true)
            binding.tvSeeMore.text = seeLessToken
        } else {
            adapter.setFullMode(false)
            binding.tvSeeMore.text = seeMoreToken
        }
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            TransactionFilterFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
