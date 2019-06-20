package com.kyberswap.android.presentation.main.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentBalanceBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.kyberlist.KyberListViewModel
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.showDrawer
import com.kyberswap.android.util.ext.showKeyboard
import kotlinx.android.synthetic.main.fragment_balance.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class BalanceFragment : BaseFragment() {

    private lateinit var binding: FragmentBalanceBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    var currentSelectedView: View? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(KyberListViewModel::class.java)
    }

    private val options by lazy {
        listOf(binding.tvKyberList, binding.tvOther)
    }

    private val balanceAddress by lazy { listOf(binding.tvAddress, binding.tvQr) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.wallet = wallet


        val adapter = BalancePagerAdapter(
            childFragmentManager,
            wallet
        )
        binding.vpBalance.adapter = adapter
        binding.vpBalance.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
            }
        })

        wallet?.address?.let {
            viewModel.getWallet(it)
        }

        viewModel.getWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        this.wallet = state.wallet
                        tvUnit.text = state.wallet.unit
                        tvBalance.text = state.wallet.balance
                    }

                    is GetWalletState.ShowError -> {
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })

        binding.tvKyberList.isSelected = true
        currentSelectedView = binding.tvKyberList

        options.forEachIndexed { index, view ->
            view.setOnClickListener {
                setSelectedOption(it)
                binding.vpBalance.currentItem = index
            }
        }

        balanceAddress.forEach { view ->
            view.setOnClickListener {
                (activity as MainActivity).getCurrentFragment()
                navigator.navigateToBalanceAddressScreen(
                    (activity as MainActivity).getCurrentFragment(),
                    wallet
                )
            }

        }

        binding.imgMenu.setOnClickListener {
            showDrawer(true)
        }

        listOf(binding.edtSearch, binding.imgSearch).forEach {
            it.setOnClickListener {
                binding.edtSearch.requestFocus()
                it.showKeyboard()
            }

        }

        viewModel.compositeDisposable.add(
            binding.edtSearch.textChanges().skipInitialValue().debounce(
                250,
                TimeUnit.MILLISECONDS
            )
                .map {
                    return@map it.trim().toString().toLowerCase()
                }.observeOn(schedulerProvider.ui())
                .subscribe {
                    viewModel.updateSearchKeyword(it)
                })

        binding.edtSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.edtSearch.clearFocus()
            }
            false
        }
    }

    private fun setSelectedOption(view: View) {
        currentSelectedView?.isSelected = false
        view.isSelected = true
        currentSelectedView = view
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            BalanceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
