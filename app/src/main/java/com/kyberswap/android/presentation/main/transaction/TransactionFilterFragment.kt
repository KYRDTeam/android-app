package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
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
import com.kyberswap.android.presentation.main.balance.GetBalanceState
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

    private var transaction: Transaction? = null

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
        binding.transaction = transaction

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()


        listOf<View>(binding.tvSend, binding.tvReceived, binding.tvSwap).forEach {
            it.setOnClickListener {
                it.isSelected = !it.isSelected
    


        binding.rvToken.layoutManager = GridLayoutManager(
            activity,
            4
        )

        val adapter = TokenFilterAdapter(appExecutors) {


        binding.rvToken.adapter = adapter

        viewModel.getTokenList(wallet!!.address)
        viewModel.getTokenListCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetBalanceState.Loading)
                when (state) {
                    is GetBalanceState.Success -> {
                        adapter.submitList(state.tokens)
            
                    is GetBalanceState.ShowError -> {
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)

        binding.tvSeeMore.setOnClickListener {
            submitTokenList(adapter)


        listOf<View>(binding.edtFrom, binding.edtTo).forEach {
            it.setOnClickListener { view ->
                selectedView = view
                openMaterialDialog()
    


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
            (selectedView as? EditText)?.setText("$dayOfMonth/${monthOfYear + 1}/$year")

    }

    private fun submitTokenList(adapter: TokenFilterAdapter) {
        val text = binding.tvSeeMore.text.toString()
        if (text == seeMoreToken) {
            adapter.setFullMode(true)
            binding.tvSeeMore.text = seeLessToken
 else {
            adapter.setFullMode(false)
            binding.tvSeeMore.text = seeMoreToken

    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            TransactionFilterFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
