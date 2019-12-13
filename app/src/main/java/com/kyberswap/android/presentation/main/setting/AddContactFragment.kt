package com.kyberswap.android.presentation.main.setting


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentAddContactBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.DEFAULT_ENS_ADDRESS
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.DeleteContactState
import com.kyberswap.android.presentation.main.swap.GetENSAddressState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.ensAddress
import com.kyberswap.android.util.ext.hideKeyboard
import com.kyberswap.android.util.ext.isContact
import com.kyberswap.android.util.ext.isENSAddress
import com.kyberswap.android.util.ext.onlyAddress
import kotlinx.android.synthetic.main.fragment_send.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AddContactFragment : BaseFragment() {

    private lateinit var binding: FragmentAddContactBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private var wallet: Wallet? = null
    private var address: String? = null
    private var contact: Contact? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AddContactViewModel::class.java)
    }

    private val isEmptyOrDefaultName: Boolean
        get() = binding.edtName.text.toString().isEmpty() || binding.edtName.text.toString().equals(
            getString(R.string.default_wallet_name), true
        )

    private val isENSAddress: Boolean
        get() = binding.edtAddress.text.toString().isENSAddress() && !binding.edtAddress.text.toString().onlyAddress().isContact()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments?.getParcelable(WALLET_PARAM)
        address = arguments?.getString(ADDRESS_PARAM)
        contact = arguments?.getParcelable(CONTACT_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imgQRCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()
        }
        binding.edtAddress.setText(address)
        binding.imgBack.setOnClickListener {
            activity!!.onBackPressed()
        }

        if (contact != null) {
            binding.title = getString(R.string.edit_contact)
            binding.contact = contact
            binding.edtAddress.setText(contact?.address)
            binding.executePendingBindings()
        } else {
            binding.title = getString(R.string.add_contact)
            binding.edtAddress.setText(address)
        }

        viewModel.compositeDisposable.add(binding.edtAddress.textChanges()
            .skipInitialValue()
            .debounce(
                250,
                TimeUnit.MILLISECONDS
            )
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.ilAddress.error = null
                if (isENSAddress) {
                    it.ensAddress()?.let { it1 -> viewModel.resolve(it1) }
                } else if (isEmptyOrDefaultName) {
                    viewModel.revertResolve(it.toString())
                }

            })


        binding.lnDelete.visibility = if (contact == null) View.GONE else View.VISIBLE

        binding.lnSend.setOnClickListener {
            hideKeyboard()
            when {
                binding.edtAddress.text.isNullOrEmpty() -> showError(getString(R.string.provide_receive_address))
                !binding.edtAddress.text.toString().isContact() -> showError(getString(R.string.invalid_contact_address))
                else -> {
                    wallet?.let {
                        if (contact == null) contact = Contact(
                            it.address,
                            binding.edtAddress.text.toString(),
                            binding.edtName.text.toString(),
                            System.currentTimeMillis() / 1000
                        )

                        contact?.let { contact ->
                            viewModel.saveSendContact(
                                it.address,
                                contact,
                                true
                            )
                        }
                    }
                }
            }

        }

        binding.lnDelete.setOnClickListener {
            contact?.let {
                dialogHelper.showConfirmation(
                    getString(R.string.title_delete),
                    getString(R.string.contact_confirm_delete),
                    {
                        viewModel.deleteContact(it)
                    })
            }
        }

        binding.imgDone.setOnClickListener {
            when {
                binding.edtAddress.text.isNullOrEmpty() -> showAlert(getString(R.string.provide_receive_address))
                !binding.edtAddress.text.toString().isContact() -> showError(getString(R.string.invalid_contact_address))
                else -> {
                    hideKeyboard()
                    wallet?.address?.let { address ->
                        viewModel.save(
                            address,
                            binding.edtName.text.toString(),
                            binding.edtAddress.text.toString()
                        )
                    }
                }
            }
        }

        viewModel.saveContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveContactState.Loading)
                when (state) {
                    is SaveContactState.Success -> {
                        if (state.isSend) {
                            navigator.navigateToSendScreen(
                                currentFragment, wallet
                            )
                        } else {
                            onSuccess()
                        }
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
                showProgress(state == DeleteContactState.Loading)
                when (state) {
                    is DeleteContactState.Success -> {
                        onSuccess()
                    }
                    is DeleteContactState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.getGetENSCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetENSAddressState.Success -> {
                        if (DEFAULT_ENS_ADDRESS != state.address) {
                            binding.edtName.setText(state.ensName)
                            binding.edtAddress.setText(state.address)
                        }
                    }
                    is GetENSAddressState.ShowError -> {

                    }
                }
            }
        })


        viewModel.getGetRevertENSCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetENSAddressState.Loading)
                when (state) {
                    is GetENSAddressState.Success -> {
                        binding.edtName.setText(state.ensName)
                        binding.edtAddress.setText(state.address)
                    }
                    is GetENSAddressState.ShowError -> {
                    }
                }
            }
        })
    }

    private fun onSuccess() {
        activity?.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showAlert(getString(R.string.message_cancelled))
            } else {

                val content =
                    result.contents.toString()

                val resultContent = if (content.isENSAddress()) content else content.onlyAddress()
                binding.edtAddress.setText(resultContent)
                if (!(resultContent.isContact() || resultContent.isENSAddress())) {
                    val error = getString(R.string.invalid_contact_address)
                    showAlertWithoutIcon(title = "Invalid Address", message = error)
                    ilAddress.setErrorTextAppearance(R.style.error_appearance)
                    binding.ilAddress.error = error
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        super.onDestroyView()
    }

    companion object {
        private const val WALLET_PARAM = "param_wallet"
        private const val ADDRESS_PARAM = "param_address"
        private const val CONTACT_PARAM = "param_contact"
        fun newInstance(wallet: Wallet?, address: String, contact: Contact?) =
            AddContactFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putString(ADDRESS_PARAM, address)
                    putParcelable(CONTACT_PARAM, contact)
                }
            }
    }
}
