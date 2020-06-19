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
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
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
import com.kyberswap.android.presentation.common.CustomContactAdapter
import com.kyberswap.android.presentation.common.DEFAULT_ENS_ADDRESS
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.main.swap.DeleteContactState
import com.kyberswap.android.presentation.main.swap.GetContactState
import com.kyberswap.android.presentation.main.swap.GetENSAddressState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetMaxPriceState
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.GAS_OPTION
import com.kyberswap.android.util.GAS_OPTIONS_FAST
import com.kyberswap.android.util.GAS_OPTIONS_REGULAR
import com.kyberswap.android.util.GAS_OPTIONS_SLOW
import com.kyberswap.android.util.GAS_OPTIONS_SUPER_FAST
import com.kyberswap.android.util.GAS_VALUE
import com.kyberswap.android.util.TRANSFER_ADVANCED
import com.kyberswap.android.util.TRANSFER_TOKEN_SELECT
import com.kyberswap.android.util.TRANSFER_TRANSFERNOW_TAPPED
import com.kyberswap.android.util.USER_CLICK_ADD_CONTACT_EVENT
import com.kyberswap.android.util.USER_CLICK_DELETE_CONTACT_EVENT
import com.kyberswap.android.util.USER_CLICK_EDIT_CONTACT_EVENT
import com.kyberswap.android.util.USER_CLICK_ITEM_SUGGESTION_EVENT
import com.kyberswap.android.util.USER_CLICK_MORE_CONTACT_EVENT
import com.kyberswap.android.util.USER_CLICK_RECENT_CONTACT_EVENT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.ensAddress
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.isContact
import com.kyberswap.android.util.ext.isENSAddress
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.isSomethingWrongError
import com.kyberswap.android.util.ext.onlyAddress
import com.kyberswap.android.util.ext.rounding
import com.kyberswap.android.util.ext.setAmount
import com.kyberswap.android.util.ext.setViewEnable
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.synthetic.main.fragment_send.*
import net.cachapa.expandablelayout.ExpandableLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.util.Locale
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
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var maxGasPrice: String = ""

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SendViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    private var currentSelection: Contact? = null

    private var selectedGasFeeView: CompoundButton? = null

    private val contacts = mutableListOf<Contact>()

    private var helperText: String? = null

    private var hasGasLimit: Boolean = false

    private val contactAddress: String
        get() = if (ilAddress.helperText.toString()
                .isContact()
        ) ilAddress.helperText.toString() else edtAddress.text.toString().onlyAddress()

    private val isENSAddress: Boolean
        get() = edtAddress.text.toString().isENSAddress() && !edtAddress.text.toString()
            .onlyAddress().isContact()

    private val isContactExist: Boolean
        get() = contacts.find { ct ->
            ct.address.equals(currentSelection?.address, true)
                || ct.address.equals(edtAddress.text.toString().onlyAddress(), true)
                || ct.address.equals(ilAddress.helperText?.toString(), true)
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

    private val currentActivity by lazy {
        activity as MainActivity
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

        viewModel.getSelectedWallet()
        viewModel.getContact()
        binding.tvContinue.setViewEnable(true)
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


        viewModel.getSendCallback.observe(viewLifecycleOwner, Observer {
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
                            ilAddress.helperText = helperText
                        }

                        val lastAddedFragment =
                            currentFragment.childFragmentManager.fragments.lastOrNull()
                        if ((lastAddedFragment is SendFragment) ||
                            !hasGasLimit
                        ) {
                            viewModel.getGasLimit(
                                state.send,
                                wallet
                            )
                        }

                        viewModel.getGasPrice()
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
                            gas = state.gas.copy(maxGasPrice = maxGasPrice)
                        )
                        binding.send = send
                    }
                    is GetGasPriceState.ShowError -> {
                        val err = state.message ?: getString(R.string.something_wrong)
                        if (isNetworkAvailable() && !isSomethingWrongError(err)) {
                            showError(err)
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

            analytics.logEvent(TRANSFER_TOKEN_SELECT, Bundle().createEvent())
        }

        listOf(rbSuperFast, rbFast, rbRegular, rbSlow).forEach {
            it.setOnCheckedChangeListener { rb, isChecked ->
                if (isChecked) {
                    if (rb != selectedGasFeeView) {
                        selectedGasFeeView?.isChecked = false
                        rb.isSelected = true
                        selectedGasFeeView = rb
                        saveSend()
                    }
                }

            }
        }

        binding.tvAddContact.setOnClickListener {
            analytics.logEvent(
                if (isContactExist) USER_CLICK_EDIT_CONTACT_EVENT else USER_CLICK_ADD_CONTACT_EVENT,
                Bundle().createEvent()
            )

            saveSend()
            navigator.navigateToAddContactScreen(
                currentFragment,
                wallet,
                if (ilAddress.helperText?.toString()
                        ?.isContact() == true
                ) ilAddress.helperText.toString() else
                    edtAddress.text.toString().onlyAddress(),
                currentSelection
            )
        }

        binding.tvMore.setOnClickListener {
            analytics.logEvent(USER_CLICK_MORE_CONTACT_EVENT, Bundle().createEvent())
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
            hideKeyboard()
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
                    if (!binding.edtAddress.text.isENSAddress() && binding.edtAddress.text.isNotEmpty()) {
                        currentSelection?.let {
                            binding.edtAddress.setText(it.address)
                        }
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
                .debounce(
                    250,
                    TimeUnit.MILLISECONDS
                )
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    ilAddress.error = null
                    ilAddress.helperText = null
                    if (isENSAddress) {
                        currentSelection = null
                        it.ensAddress()?.let { it1 -> viewModel.resolve(it1) }
                    } else {
                        updateContactAction()

                        if (it.toString().onlyAddress().isContact()) {
                            binding.send?.let { send ->
                                viewModel.getGasLimit(
                                    send.copy(
                                        contact = send.contact.copy(
                                            address = it.toString().onlyAddress()
                                        )
                                    ), wallet
                                )
                            }
                        }
                    }

                })

        val contactAdapter =
            ContactAdapter(
                appExecutors, handler,
                {
                    analytics.logEvent(USER_CLICK_RECENT_CONTACT_EVENT, Bundle().createEvent())
                    sendToContact(it)
                },
                {
                    analytics.logEvent(USER_CLICK_RECENT_CONTACT_EVENT, Bundle().createEvent("2"))
                    sendToContact(it)
                    wallet?.let { wallet ->
                        viewModel.saveSendContact(wallet.address, it)
                    }
                }, {
                    analytics.logEvent(USER_CLICK_EDIT_CONTACT_EVENT, Bundle().createEvent())
                    saveSend()
                    navigator.navigateToAddContactScreen(
                        currentFragment,
                        wallet,
                        it.address,
                        it
                    )
                }, {
                    analytics.logEvent(USER_CLICK_DELETE_CONTACT_EVENT, Bundle().createEvent())
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
                        currentSelection = null
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
        binding.edtAddress.threshold = 1

        viewModel.getContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetContactState.Success -> {
                        contacts.clear()
                        contacts.addAll(state.contacts)

                        if (currentSelection == null) {
                            currentSelection = contacts.find { ct ->
                                ct.address.equals(
                                    edtAddress.text.toString().onlyAddress(), true
                                )
                            }
                        }

                        currentSelection?.let {
                            currentSelection = contacts.find { ct ->
                                ct.address.equals(it.address, true)
                            }

                            currentSelection?.let { it1 -> sendToContact(it1) }

                        }

                        updateContactAction()

                        contactAdapter.submitList(state.contacts.take(2))
                        if (context != null) {
                            val adapter = CustomContactAdapter(
                                context!!,
                                R.layout.item_contact_autocomplete,
                                state.contacts
                            )
                            binding.edtAddress.setAdapter(adapter)
                        }
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

                        if (binding.send != send) {
                            hasGasLimit = true
                            binding.send = send
                            binding.executePendingBindings()
                        }
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

        viewModel.getGetENSCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetENSAddressState.Success -> {
                        if (DEFAULT_ENS_ADDRESS == state.address) {
                            val error = getString(R.string.not_attach_address)
                            ilAddress.error = error
                            if (state.isFromContinue) {
                                showError(error)
                            }
                        } else {
                            helperText = state.address
                            ilAddress.helperText = state.address
                            binding.send?.let { send ->

                                val updateSend = send.copy(
                                    contact = send.contact.copy(
                                        address = state.address,
                                        name = state.ensName
                                    )
                                )
                                viewModel.getGasLimit(updateSend, wallet)
                                if (updateSend != send) {
                                    binding.send = updateSend
                                }
                            }

                            val contact = contacts.find {
                                it.address.equals(state.address, true)
                            }

                            currentSelection =
                                contact?.copy(name = if (contact.name.isNotEmpty()) contact.name else state.ensName)
                                    ?: Contact(
                                        name = state.ensName,
                                        address = state.address
                                    )

                            updateContactAction()

                            if (state.isFromContinue) {
                                saveSend(
                                    contactAddress
                                )
                            }
                        }
                        showProgress(false)
                    }
                    is GetENSAddressState.ShowError -> {
                        showProgress(false)
                        ilAddress.setErrorTextAppearance(R.style.error_appearance)
                        ilAddress.error = getString(R.string.enter_address_is_invalid)
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
                    binding.edtSource.text.toString()
                        .toBigDecimalOrDefaultZero() > binding.send?.tokenSource?.currentBalance -> {
                        showAlertWithoutIcon(
                            title = getString(R.string.title_amount_too_big),
                            message = getString(R.string.exceed_balance)
                        )
                    }
                    !(edtAddress.text.toString().onlyAddress().isContact() ||
                        ilAddress.helperText.toString().isContact() ||
                        edtAddress.text.toString().isENSAddress()) -> showAlertWithoutIcon(
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
                        wallet?.let {
                            showProgress(true)
                            viewModel.checkEligibleWallet(it)
                        }
                    }
                }
            }

            analytics.logEvent(TRANSFER_TRANSFERNOW_TAPPED, Bundle().createEvent())
            binding.send?.let {
                analytics.logEvent(
                    TRANSFER_ADVANCED, Bundle().createEvent(
                        listOf(
                            GAS_OPTION, GAS_VALUE
                        ),
                        listOf(
                            getGasPriceOption(selectedGasFeeView?.id),
                            getSelectedGasPrice(it.gas, selectedGasFeeView?.id)
                        )
                    )
                )
            }

        }

        viewModel.checkEligibleWalletCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is CheckEligibleWalletState.Success -> {
                        if (state.eligibleWalletStatus.success && !state.eligibleWalletStatus.eligible) {
                            showProgress(false)
                            binding.tvContinue.setViewEnable(false)
                            showError(state.eligibleWalletStatus.message)
                        } else {
                            onVerifyWalletComplete()
                        }
                    }
                    is CheckEligibleWalletState.ShowError -> {
                        onVerifyWalletComplete()
                    }
                }
            }
        })

        listOf(binding.tvTokenBalance, binding.tvBalanceDetail).forEach { tv ->
            tv.setOnClickListener {
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
                        binding.edtSource.setText(
                            it.tokenSource.currentBalance.rounding().toDisplayNumber()
                        )
                    }

                }
            }

        }

        currentActivity.mainViewModel.getMaxPriceCallback.observe(
            viewLifecycleOwner,
            Observer { event ->
                event?.peekContent()?.let { state ->
                    when (state) {
                        is GetMaxPriceState.Success -> {
                            maxGasPrice = Convert.fromWei(
                                state.data,
                                Convert.Unit.GWEI
                            ).toDisplayNumber()
                            val currentSend = binding.send
                            if (currentSend != null) {
                                val send = currentSend.copy(
                                    gas = currentSend.gas.copy(maxGasPrice = maxGasPrice)
                                )
                                binding.send = send
                                binding.executePendingBindings()
                            }
                        }
                        is GetMaxPriceState.ShowError -> {

                        }
                    }
                }
            })

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
                                isContactExist || ilAddress.helperText.toString().isContact()
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

        binding.edtAddress.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(position)
                if (item is Contact) {
                    analytics.logEvent(
                        USER_CLICK_ITEM_SUGGESTION_EVENT, Bundle().createEvent(
                            item.name
                        )
                    )
                    binding.edtAddress.setText(item.nameAddressDisplay)
                    binding.edtAddress.clearFocus()
                    hideKeyboard()
                }
            }
        }

        currentActivity.mainViewModel.checkEligibleWalletCallback.observe(
            viewLifecycleOwner,
            Observer { event ->
                event?.peekContent()?.let { state ->
                    when (state) {
                        is CheckEligibleWalletState.Success -> {
                            if (state.eligibleWalletStatus.success && !state.eligibleWalletStatus.eligible) {
                                if (isAdded)
                                    binding.tvContinue.setViewEnable(false)
                                showError(state.eligibleWalletStatus.message)
                            } else {
                                binding.tvContinue.setViewEnable(true)
                            }
                        }
                        is CheckEligibleWalletState.ShowError -> {

                        }
                    }
                }
            })

        binding.edtSource.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }
            false
        }

        binding.edtAddress.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }
            false
        }
    }

    private fun getGasPriceOption(id: Int?): String {
        return when (id) {
            R.id.rbSuperFast -> GAS_OPTIONS_SUPER_FAST
            R.id.rbRegular -> GAS_OPTIONS_REGULAR
            R.id.rbSlow -> GAS_OPTIONS_SLOW
            else -> GAS_OPTIONS_FAST
        }
    }

    private fun onVerifyWalletComplete() {
        binding.tvContinue.setViewEnable(true)
        if (isENSAddress) {
            viewModel.resolve(edtAddress.text.toString(), true)
        } else {
            showProgress(false)
            saveSend(contactAddress)
        }
    }


    private fun saveSend(address: String = "") {
        binding.send?.let { send ->
            viewModel.saveSend(
                send.copy(
                    gasPrice = getSelectedGasPrice(
                        send.gas,
                        selectedGasFeeView?.id
                    ),
                    contact = send.contact.copy(
                        address = if (ilAddress.helperText.toString().isContact())
                            ilAddress.helperText.toString()
                        else edtAddress.text.toString().onlyAddress(),
                        name = if (send.contact.name.isNotEmpty()) send.contact.name
                        else currentSelection?.name ?: ""
                    ),
                    sourceAmount = edtSource.text.toString()
                ), address
            )
        }
    }

    private fun updateContactAction() {
        if (isContactExist) {
            tvAddContact.text = getString(R.string.edit_contact)
        } else {
            tvAddContact.text = getString(R.string.add_contact)
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
        ilAddress.error = null
        ilAddress.helperText = null
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
        get() = (activity as MainActivity).pendingTransactions.any { !it.isCancel }

    private fun getSelectedGasPrice(gas: Gas, id: Int?): String {
        val value = when (id) {
            R.id.rbSuperFast -> gas.superFast
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast
        }

        return if (value.isBlank()) {
            gas.superFast
        } else value
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        hasGasLimit = false
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val content =
                    result.contents.toString()

                val resultContent = if (content.isENSAddress()) content else content.onlyAddress()
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

                if (!(resultContent.isContact() || resultContent.isENSAddress())) {
                    val error = getString(R.string.invalid_contact_address)
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_contact_address_title),
                        message = error
                    )
                    ilAddress.setErrorTextAppearance(R.style.error_appearance)
                    binding.ilAddress.error = error
                }
            }
        } else if (requestCode == SEND_CONFIRM && resultCode == Activity.RESULT_OK && data != null) {
            showBroadcast(data.getStringExtra(HASH_PARAM) ?: "")
            handler.postDelayed(
                {
                    edtSource.setText("")
                    edtSource.clearFocus()
                    saveSend()
                }, 250
            )
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showBroadcast(hash: String) {
        val context = activity
        if (context is MainActivity) {

            context.showDialog(
                CustomAlertActivity.DIALOG_TYPE_BROADCASTED,
                Transaction(
                    type = Transaction.TransactionType.SEND,
                    hash = hash
                )
            )
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
