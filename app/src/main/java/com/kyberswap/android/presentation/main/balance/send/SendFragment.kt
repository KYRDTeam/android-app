package com.kyberswap.android.presentation.main.balance.send

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSendBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.swap.GetContactState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.isContact
import com.kyberswap.android.util.ext.setAllOnClickListener
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import kotlinx.android.synthetic.main.fragment_send.*
import net.cachapa.expandablelayout.ExpandableLayout
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SendFragment : BaseFragment() {

    private lateinit var binding: FragmentSendBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SendViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walletName = wallet?.name
        wallet?.let {
            viewModel.getSendInfo(wallet!!.address)



        viewModel.getGasPrice()
        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        var send = binding.send
                        send = send?.copy(
                            gasPrice = getSelectedGasPrice(state.gas),
                            gas = state.gas
                        )
                        viewModel.saveSend(send)
                        binding.send = send
            
                    is GetGasPriceState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.tvAdvanceOption.setOnClickListener {
            binding.expandableLayout.expand()


        binding.imgClose.setOnClickListener {
            binding.expandableLayout.collapse()


        listOf(binding.imgTokenSource, binding.tvSource).forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromSendTokenScreen(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet
                )
    


        binding.tvAddContact.setOnClickListener {
            navigator.navigateToAddContactScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet,
                edtAddress.text.toString()
            )


        binding.tvMore.setOnClickListener {
            navigator.navigateToContactScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )


        binding.expandableLayout.setOnExpansionUpdateListener { _, state ->
            if (state == ExpandableLayout.State.EXPANDED) {
                val animator = ObjectAnimator.ofInt(
                    binding.scView,
                    "scrollY",
                    binding.tvContinue.top
                )

                animator.duration = 300
                animator.interpolator = AccelerateInterpolator()
                animator.start()
    


        binding.rbFast.isChecked = true

        binding.imgBack.setOnClickListener {
            activity!!.onBackPressed()


        binding.rvContact.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        val contactAdapter =
            ContactAdapter(appExecutors) { contact ->
                viewModel.saveSend(binding.send?.copy(contact = contact))
                binding.edtAddress.setText(contact.address)
    
        binding.rvContact.adapter = contactAdapter

        viewModel.getContact(wallet!!.address)
        viewModel.getContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetContactState.Success -> {
                        contactAdapter.submitList(state.contacts.take(2))
            
                    is GetContactState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.imgQRCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()


        viewModel.getSendCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSendState.Success -> {
                        binding.send = state.send
            
                    is GetSendState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))

            
        
    
)

        viewModel.compositeDisposable.add(binding.edtSource.textChanges().skipInitialValue()
            .debounce(
                250,
                TimeUnit.MILLISECONDS
            )
            .observeOn(schedulerProvider.ui())
            .subscribe { amount ->
                val copy = binding.send?.copy(sourceAmount = amount.toString())
                viewModel.saveSend(copy)
                viewModel.getGasLimit(
                    copy,
                    wallet
                )
    )

        binding.tvContinue.setOnClickListener {
            when {
                edtSource.text.isNullOrEmpty() -> showAlert(getString(R.string.specify_amount))
                edtAddress.text.isNullOrEmpty() -> showAlert(getString(R.string.specify_contact_address))
                binding.edtSource.text.toString().toBigDecimalOrDefaultZero() > binding.send?.tokenSource?.currentBalance -> {
                    showAlert(getString(R.string.exceed_balance))
        
                !edtAddress.text.toString().isContact() -> showAlert(getString(R.string.invalid_contact_address))
                else -> viewModel.saveSend(binding.send, binding.edtAddress.text.toString())
    


        binding.grBalance.setAllOnClickListener(View.OnClickListener {
            binding.edtSource.setText(binding.send?.tokenSource?.currentBalance?.toPlainString())
)

        viewModel.saveSendCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSendState.Loading)
                when (state) {
                    is SaveSendState.Success -> {
                        navigator.navigateToSendConfirmationScreen(wallet)
            
                    is SaveSendState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)

    }


    private fun getSelectedGasPrice(gas: Gas): String {
        return when (binding.rgGas.checkedRadioButtonId) {
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast

    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.dispose()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showAlert(getString(R.string.message_cancelled))
     else {
                binding.edtAddress.setText(result.contents.toString())
                if (!result.contents.toString().isContact()) {
                    binding.edtAddress.error = getString(R.string.invalid_contact_address)
        
    
 else {
            super.onActivityResult(requestCode, resultCode, data)

    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            SendFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
