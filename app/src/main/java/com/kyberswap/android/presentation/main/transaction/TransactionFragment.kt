package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentTransactionBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_transaction.*
import javax.inject.Inject


class TransactionFragment : BaseFragment() {

    private lateinit var binding: FragmentTransactionBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    var selectedIndex: Int = 0

    val status = mutableListOf<View>()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(TransactionViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        status.addAll(listOf(binding.tvPending, binding.tvMined))


        viewModel.getSelectedWallet()

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (state.wallet.address != wallet?.address) {
                            wallet = state.wallet
                            binding.wallet = wallet
                
            
                    is GetWalletState.ShowError -> {

            
        
    
)

        setupAdapter()


        binding.imgFilter.setOnClickListener {
            navigator.navigateToTransactionFilterScreen(
                (activity as MainActivity).getCurrentFragment(),
                wallet
            )



        setSelectedOption(selectedIndex)

        status.forEachIndexed { index, view ->
            view.setOnClickListener {
                setSelectedOption(index)
                binding.vpTransaction.currentItem = index
    


        imgBack.setOnClickListener {
            activity?.onBackPressed()

    }

    private fun setupAdapter() {
        val adapter = TransactionPagerAdapter(
            childFragmentManager
        )
        binding.vpTransaction.adapter = adapter
        binding.vpTransaction.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
    

            override fun onPageSelected(position: Int) {
                setSelectedOption(position)
    
)
    }
    private fun setSelectedOption(index: Int) {
        if (index != selectedIndex) {
            status[selectedIndex].isSelected = false
            selectedIndex = index

        status[selectedIndex].isSelected = true
    }

    override fun onDestroyView() {
        status.clear()
        super.onDestroyView()
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            TransactionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
