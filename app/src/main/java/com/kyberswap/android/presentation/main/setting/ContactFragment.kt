package com.kyberswap.android.presentation.main.setting


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
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentContactBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.send.ContactAdapter
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
                wallet?.let {
                    viewModel.saveSendContact(it.address, contact)
                }

            },
                {

                },
                {

                },
                {

                })
        binding.rvContact.adapter = contactAdapter

        viewModel.saveContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveContactState.Loading)
                when (state) {
                    is SaveContactState.Success -> {
                        activity!!.onBackPressed()
                    }
                    is SaveContactState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
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
                        showAlert(state.message ?: getString(R.string.something_wrong))
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
        fun newInstance() =
            ContactFragment()
    }


}
