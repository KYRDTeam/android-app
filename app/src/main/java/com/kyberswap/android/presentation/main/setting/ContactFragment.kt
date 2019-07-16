package com.kyberswap.android.presentation.main.setting


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentContactBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.send.ContactAdapter
import com.kyberswap.android.presentation.main.swap.DeleteContactState
import com.kyberswap.android.presentation.main.swap.GetContactState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class ContactFragment : BaseFragment() {

    private lateinit var binding: FragmentContactBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var wallet: Wallet? = null

    @Inject
    lateinit var appExecutors: AppExecutors


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ContactViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    private var fromSetting: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromSetting = arguments?.getBoolean(FROM_SETTING_PARAM, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getSelectedWallet()
        viewModel.getSelectedWalletCallback.observe(parentFragment!!.viewLifecycleOwner, Observer {
            it?.peekContent()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (this.wallet?.address != state.wallet.address) {
                            this.wallet = state.wallet
                            wallet?.let {
                                viewModel.getContact(it.address)
                            }
                        }
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        binding.imgAddContact.setOnClickListener {
            navigator.navigateToAddContactScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }


        binding.rvContact.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        val contactAdapter =
            ContactAdapter(appExecutors, handler, { contact ->
                if (fromSetting == true) {
                    navigator.navigateToAddContactScreen(currentFragment, contact = contact)
                } else {
                    wallet?.let {
                        viewModel.saveSendContact(it.address, contact)
                    }
                }
            },
                { contact ->
                    wallet?.let {
                        viewModel.saveSendContact(it.address, contact)
                    }
                },
                { contact ->
                    navigator.navigateToAddContactScreen(
                        currentFragment,
                        wallet,
                        contact.address,
                        contact
                    )
                },
                { contact ->

                    dialogHelper.showConfirmation(
                        getString(R.string.title_delete),
                        getString(R.string.contact_confirm_delete),
                        {
                            viewModel.deleteContact(contact)
                        })

                })
        binding.rvContact.adapter = contactAdapter

        viewModel.saveContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveContactState.Loading)
                when (state) {
                    is SaveContactState.Success -> {
                        navigator.navigateToSendScreen(
                            currentFragment, wallet
                        )
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

        viewModel.getContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetContactState.Success -> {
                        contactAdapter.submitList(state.contacts)
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

    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        private const val FROM_SETTING_PARAM = "from_setting_param"
        fun newInstance(fromSetting: Boolean = false) =
            ContactFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(FROM_SETTING_PARAM, fromSetting)
                }
            }
    }


}
