package com.kyberswap.android.presentation.main.balance.send

import android.animation.ObjectAnimator
import android.app.Activity
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
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletChangeEvent
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.CustomAlertActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.swap.DeleteContactState
import com.kyberswap.android.presentation.main.swap.GetContactState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.isContact
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.fragment_send.*
import net.cachapa.expandablelayout.ExpandableLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.util.Locale
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

    private val isContactExist: Boolean
        get() = contacts.find { ct ->
            ct.address.toLowerCase(Locale.getDefault()) == currentSelection?.address?.toLowerCase(
                Locale.getDefault()
            ) || ct.address.toLowerCase(
                Locale.getDefault()
            ) == onlyAddress(
                edtAddress.text.toString()
            ).toLowerCase(Locale.getDefault())
        } != null

    private val availableAmount: BigDecimal
        get() = binding.send?.let {
            it.availableAmountForTransfer(
                it.tokenSource.currentBalance,
                Token.TRANSFER_ETH_GAS_LIMIT_DEFAULT.toBigDecimal(),
                getSelectedGasPrice(
                    it.gas, selectedGasFeeView?.id
                ).toBigDecimalOrDefaultZero()
            )
        } ?: BigDecimal.ZERO

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

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

        viewModel.getSelectedWallet()
        viewModel.getContact()

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        wallet = state.wallet
                        wallet?.let {
                            viewModel.getSendInfo(it)
                        }
                        binding.walletName = wallet?.name
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })


        viewModel.getSendCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSendState.Success -> {
                        if (!state.send.isSameTokenPair(binding.send)) {
                            binding.send = state.send
                            binding.executePendingBindings()
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
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
                    currentFragment,
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
                                    ),
                                    contact = send.contact.copy(address = onlyAddress(edtAddress.text.toString())),
                                    sourceAmount = edtSource.text.toString()
                                )
                            )
                        }
                    }
                }

            }
        }

        binding.tvAddContact.setOnClickListener {
            saveSend()
            navigator.navigateToAddContactScreen(
                currentFragment,
                wallet,
                onlyAddress(edtAddress.text.toString()),
                currentSelection
            )
        }

        binding.tvMore.setOnClickListener {
            saveSend()
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
                        if (isContactExist) {
                            binding.edtAddress.setText(it.nameAddressDisplay)
                        }
                    }
                }
            })

        viewModel.compositeDisposable.add(
            binding.edtAddress.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    ilAddress.error = null
                    if (isContactExist) {
                        tvAddContact.text = getString(R.string.edit_contact)
                    } else {
                        tvAddContact.text = getString(R.string.add_contact)
                    }

                    if (onlyAddress(it.toString()).isContact()) {
                        binding.send?.let { send ->
                            viewModel.getGasLimit(
                                send.copy(
                                    contact = send.contact.copy(
                                        address = onlyAddress(
                                            it.toString()
                                        )
                                    )
                                ), wallet
                            )
                        }
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
                    saveSend()
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        binding.rvContact.adapter = contactAdapter


        viewModel.getContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetContactState.Success -> {
                        contacts.clear()
                        contacts.addAll(state.contacts)

                        if (currentSelection == null) {
                            currentSelection = contacts.find { ct ->
                                ct.address.toLowerCase(Locale.getDefault()) == onlyAddress(
                                    edtAddress.text.toString()
                                ).toLowerCase(
                                    Locale.getDefault()
                                )
                            }
                        }

                        currentSelection?.let {
                            currentSelection = contacts.find { ct ->
                                ct.address.toLowerCase(Locale.getDefault()) == it.address.toLowerCase(
                                    Locale.getDefault()
                                )
                            }

                            currentSelection?.let { it1 -> sendToContact(it1) }

                        }

                        contactAdapter.submitList(state.contacts.take(2))
                    }
                    is GetContactState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
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
                if (copy != binding.send) {
                    binding.send = copy
                    binding.executePendingBindings()
                }
                viewModel.getGasLimit(
                    copy,
                    wallet
                )
            })

        binding.tvContinue.setOnClickListener {

            binding.send?.let { send ->
                when {
                    edtSource.text.isNullOrEmpty() -> showAlertWithoutIcon(
                        title = getString(R.string.invalid_amount),
                        message = getString(R.string.specify_amount)
                    )
                    edtAddress.text.isNullOrEmpty() -> showAlertWithoutIcon(
                        title = getString(R.string.invalid_contact_address_title),
                        message = getString(R.string.specify_contact_address)
                    )
                    binding.edtSource.text.toString().toBigDecimalOrDefaultZero() > binding.send?.tokenSource?.currentBalance -> {
                        showAlertWithoutIcon(
                            title = getString(R.string.title_amount_too_big),
                            message = getString(R.string.exceed_balance)
                        )
                    }
                    !onlyAddress(edtAddress.text.toString()).isContact() -> showAlertWithoutIcon(
                        title = getString(R.string.invalid_contact_address_title),
                        message = getString(R.string.specify_contact_address)
                    )

                    send.copy(
                        gasPrice = getSelectedGasPrice(
                            send.gas,
                            selectedGasFeeView?.id
                        )
                    ).insufficientEthBalance -> showAlertWithoutIcon(
                        getString(R.string.insufficient_eth),
                        getString(R.string.not_enough_eth_blance)
                    )

                    send.tokenSource.isETH &&
                        availableAmount < edtSource.toBigDecimalOrDefaultZero() -> {
                        showAlertWithoutIcon(
                            getString(R.string.insufficient_eth),
                            getString(R.string.not_enough_eth_blance)
                        )
                    }

                    hasPendingTransaction -> showAlertWithoutIcon(message = getString(R.string.pending_transaction))
                    else -> {
                        viewModel.saveSend(
                            send.copy(
                                sourceAmount = edtSource.text.toString(),
                                gasPrice = getSelectedGasPrice(send.gas, selectedGasFeeView?.id)
                            ),

                            onlyAddress(edtAddress.text.toString())
                        )
                    }
                }
            }

        }

        listOf(binding.tvTokenBalance, binding.tvBalanceDetail).forEach {
            it.setOnClickListener {
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

        }

        binding.tv25Percent.setOnClickListener {
            hideKeyboard()
            binding.edtSource.setAmount(
                tvBalanceDetail.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.25.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv50Percent.setOnClickListener {
            hideKeyboard()
            binding.edtSource.setAmount(
                tvBalanceDetail.text.toString().toBigDecimalOrDefaultZero().multiply(
                    0.5.toBigDecimal()
                ).toDisplayNumber()
            )
        }

        binding.tv100Percent.setOnClickListener {
            hideKeyboard()
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

                        startActivityForResult(activity?.let { it1 ->
                            SendConfirmActivity.newIntent(
                                it1,
                                wallet,
                                isContactExist
                            )
                        }, SEND_CONFIRM)
                    }
                    is SaveSendState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        binding.rbFast.isChecked = true
        binding.rbFast.jumpDrawablesToCurrentState()
    }

    private fun saveSend() {
        binding.send?.let { send ->
            viewModel.saveSend(
                send.copy(
                    gasPrice = getSelectedGasPrice(
                        send.gas,
                        selectedGasFeeView?.id
                    ),
                    contact = send.contact.copy(address = onlyAddress(edtAddress.text.toString())),
                    sourceAmount = edtSource.text.toString()
                )
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: WalletChangeEvent) {
        wallet?.let {
            viewModel.getSendInfo(it)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun sendToContact(contact: Contact) {
        val send = binding.send?.copy(contact = contact)
        binding.send = send
        currentSelection = contact
        if (isContactExist) {
            binding.edtAddress.setText(contact.nameAddressDisplay)
        } else {
            binding.edtAddress.setText(contact.address)
        }
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
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val resultContent = result.contents.toString()
                val contact = contacts.find {
                    it.address.toLowerCase(Locale.getDefault()) == resultContent.toLowerCase(Locale.getDefault())
                }
                if (contact != null) {
                    currentSelection = contact
                    binding.edtAddress.setText(contact.nameAddressDisplay)
                } else {
                    currentSelection = null
                    binding.edtAddress.setText(resultContent)
                }

                if (!resultContent.isContact()) {
                    val error = getString(R.string.invalid_contact_address)
                    showAlertWithoutIcon(title = "Invalid Address", message = error)
                    binding.ilAddress.error = error
                }
            }
        } else if (requestCode == SEND_CONFIRM && resultCode == Activity.RESULT_OK && data != null) {
            showBroadcast(data.getStringExtra(HASH_PARAM) ?: "")
            handler.postDelayed(
                {
                    edtSource.setText("")
                    edtSource.clearFocus()
                }, 500
            )
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showBroadcast(hash: String) {
        val context = activity
        if (context is MainActivity) {
            context.showBroadcastAlert(
                CustomAlertActivity.DIALOG_TYPE_BROADCASTED,
                Transaction(
                    type = Transaction.TransactionType.SEND,
                    hash = hash
                )
            )
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
