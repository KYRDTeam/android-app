package com.kyberswap.android.presentation.main.balance.send

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSendBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.swap.*
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.*
import kotlinx.android.synthetic.main.fragment_send.*
import net.cachapa.expandablelayout.ExpandableLayout
import javax.inject.Inject


class SendFragment : BaseFragment() {

    private lateinit var binding: FragmentSendBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SendViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    private var currentSelection: Contact? = null

    private var selectedGasFeeView: CompoundButton? = null

    private val contacts = mutableListOf<Contact>()

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
        binding.edtSource.setText("")
        binding.walletName = wallet?.name
        wallet?.let {
            viewModel.getSendInfo(it.address)
        }

        viewModel.getSendCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSendState.Success -> {
                        if (!state.send.isSameTokenPair(binding.send)) {
                            binding.send = state.send
                            edtSource.setAmount(state.send.sourceAmount)

                            if (state.send.contact.address.isNotBlank()) {
                                sendToContact(state.send.contact)
                            }
                        }
                        viewModel.getGasPrice()
                        viewModel.getGasLimit(
                            state.send,
                            wallet
                        )
                    }
                    is GetSendState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )

                    }
                }
            }
        })
        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val send = binding.send?.copy(
                            gasPrice = getSelectedGasPrice(state.gas, selectedGasFeeView?.id),
                            gas = state.gas
                        )
                        binding.send = send
                    }
                    is GetGasPriceState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.tvAdvanceOption.setOnClickListener {
            binding.expandableLayout.expand()
        }


        binding.imgClose.setOnClickListener {
            binding.expandableLayout.collapse()
        }

        listOf(binding.imgTokenSource, binding.tvSource).forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromSendTokenScreen(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet
                )
            }
        }

        listOf(rbSuperFast, rbFast, rbRegular, rbSlow).forEach {
            it.setOnCheckedChangeListener { rb, isChecked ->
                if (isChecked) {
                    if (rb != selectedGasFeeView) {
                        selectedGasFeeView?.isChecked = false
                        rb.isSelected = true
                        selectedGasFeeView = rb
                        binding.send?.let { send ->
                            viewModel.saveSend(
                                send.copy(
                                    gasPrice = getSelectedGasPrice(
                                        send.gas,
                                        rb.id
                                    )
                                )
                            )
                        }
                    }
                }

            }
        }

        binding.tvAddContact.setOnClickListener {
            navigator.navigateToAddContactScreen(
                currentFragment,
                wallet,
                onlyAddress(edtAddress.text.toString()),
                currentSelection
            )
        }

        binding.tvMore.setOnClickListener {
            navigator.navigateToContactScreen(currentFragment)
        }

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
            }
        }

        binding.rbFast.isChecked = true

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.rvContact.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        viewModel.compositeDisposable.add(binding.edtAddress.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { focused ->
                if (focused) {
                    currentSelection?.let {
                        binding.edtAddress.setText(it.address)
                    }
                } else {
                    currentSelection?.let {
                        binding.edtAddress.setText(it.nameAddressDisplay)
                    }

                }
            })

        viewModel.compositeDisposable.add(
            binding.edtAddress.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    ilAddress.error = null
                    if (currentSelection != null) {
                        tvAddContact.text = getString(R.string.edit_contact)
                    } else {
                        tvAddContact.text = getString(R.string.add_contact)
                    }
                })

        val contactAdapter =
            ContactAdapter(
                appExecutors, handler,
                {
                    sendToContact(it)
                },
                {
                    sendToContact(it)
                    wallet?.let { wallet ->
                        viewModel.saveSendContact(wallet.address, it)
                    }
                }, {
                    navigator.navigateToAddContactScreen(
                        currentFragment,
                        wallet,
                        it.address,
                        it
                    )
                }, {
                    dialogHelper.showConfirmation(
                        getString(R.string.title_delete),
                        getString(R.string.contact_confirm_delete),
                        {
                            viewModel.deleteContact(it)
                        })

                })

        viewModel.saveContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveContactState.Loading)
                when (state) {
                    is SaveContactState.Success -> {

                    }
                    is SaveContactState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.deleteContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is DeleteContactState.Success -> {
                        showAlertWithoutIcon(message = getString(R.string.delete_contact_success))
                    }
                    is DeleteContactState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.rvContact.adapter = contactAdapter

        wallet?.address?.let { viewModel.getContact(it) }
        viewModel.getContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetContactState.Success -> {
                        contacts.clear()
                        contacts.addAll(state.contacts)
                        currentSelection?.let {
                            currentSelection = contacts.find { ct ->
                                ct.address == it.address
                            }

                            currentSelection?.let { it1 -> sendToContact(it1) }


                        }
                        contactAdapter.submitList(state.contacts.take(2))
                    }
                    is GetContactState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        viewModel.getGetGasLimitCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasLimitState.Success -> {
                        val send = binding.send?.copy(
                            gasLimit = state.gasLimit.toString()
                        )

                        binding.send = send
                    }
                    is GetGasLimitState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.imgQRCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()
        }

        viewModel.compositeDisposable.add(binding.edtSource.textChanges().skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe { amount ->
                val copy = binding.send?.copy(sourceAmount = amount.toString())
                viewModel.getGasLimit(
                    copy,
                    wallet
                )
            })

        binding.tvContinue.setOnClickListener {
            when {
                edtSource.text.isNullOrEmpty() -> showError(message = getString(R.string.specify_amount))
                edtAddress.text.isNullOrEmpty() -> showError(message = getString(R.string.specify_contact_address))
                binding.edtSource.text.toString().toBigDecimalOrDefaultZero() > binding.send?.tokenSource?.currentBalance -> {
                    showError(message = getString(R.string.exceed_balance))
                }
                !onlyAddress(edtAddress.text.toString()).isContact() -> showError(
                    message = getString(
                        R.string.invalid_contact_address
                    )
                )
                hasPendingTransaction -> showError(message = getString(R.string.pending_transaction))
                else -> {
                    binding.send?.let { send ->
                        viewModel.saveSend(
                            send.copy(
                                sourceAmount = edtSource.text.toString(),
                                gasPrice = getSelectedGasPrice(send.gas, selectedGasFeeView?.id)
                            ),

                            onlyAddress(edtAddress.text.toString())
                        )
                    }
                }

//                    viewModel.saveSend(
//                    binding.send?.copy(
//                        sourceAmount = edtSource.text.toString(),
//                        gasPrice = getSelectedGasPrice(binding.send!!.gas, selectedGasFeeView?.id)
//                    ),
//                    onlyAddress(edtAddress.text.toString())
//                )
            }
        }

        binding.grBalance.setAllOnClickListener {
            binding.send?.let {
                if (it.tokenSource.isETH) {
                    showAlertWithoutIcon(message = getString(R.string.small_amount_of_eth_transaction_fee))
                    binding.edtSource.setAmount(
                        it.availableAmountForTransfer(
                            it.tokenSource.currentBalance,
                            Token.TRANSFER_ETH_GAS_LIMIT_DEFAULT.toBigDecimal(),
                            getSelectedGasPrice(
                                it.gas,
                                selectedGasFeeView?.id
                            ).toBigDecimalOrDefaultZero()
                        ).toDisplayNumber()
                    )
                } else {
                    binding.edtSource.setText(it.tokenSource.currentBalance.toDisplayNumber())
                }

            }

        }

        viewModel.saveSendCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSendState.Loading)
                when (state) {
                    is SaveSendState.Success -> {
                        navigator.navigateToSendConfirmationScreen(wallet)
                    }
                    is SaveSendState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

    }

    private fun sendToContact(contact: Contact) {
        val send = binding.send?.copy(contact = contact)
        binding.send = send
        currentSelection = contact
        binding.edtAddress.setText(contact.nameAddressDisplay)
    }

    private val hasPendingTransaction: Boolean
        get() = (activity as MainActivity).pendingTransactions.size > 0

    private fun getSelectedGasPrice(gas: Gas, id: Int?): String {
        return when (id) {
            R.id.rbSuperFast -> gas.superFast
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        viewModel.compositeDisposable.clear()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showAlertWithoutIcon(message = getString(R.string.message_cancelled))
            } else {

                val contact = contacts.find {
                    it.address.toLowerCase() == result.contents.toString().toLowerCase()
                }
                if (contact != null) {
                    currentSelection = contact
                    binding.edtAddress.setText(contact.nameAddressDisplay)
                } else {
                    binding.edtAddress.setText(result.contents.toString())
                }

                if (!result.contents.toString().isContact()) {
                    val error = getString(R.string.invalid_contact_address)
                    showError(message = error)
                    binding.ilAddress.error = error
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun onlyAddress(fullAddress: String): String {
        val index = fullAddress.indexOf("0x")
        return if (index >= 0) {
            val prefix = fullAddress.substring(0, fullAddress.indexOf("0x"))
            fullAddress.removePrefix(prefix).trim()
        } else {
            fullAddress
        }

    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            SendFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
