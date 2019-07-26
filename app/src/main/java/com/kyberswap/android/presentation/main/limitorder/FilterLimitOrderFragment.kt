package com.kyberswap.android.presentation.main.limitorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLimitOrderFilterBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_limit_order_filter.*
import javax.inject.Inject


class FilterLimitOrderFragment : BaseFragment() {

    private lateinit var binding: FragmentLimitOrderFilterBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var filterSetting: FilterSetting? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(FilterViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
        wallet?.let { viewModel.getFilterSettings() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLimitOrderFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tokenPairAdapter = FilterItemAdapter(appExecutors) {



        binding.rvTokenPair.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvTokenPair.adapter = tokenPairAdapter

        val addressAdapter = FilterItemAdapter(appExecutors) {



        binding.rvAddress.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvAddress.adapter = addressAdapter

        val statusAdapter = FilterItemAdapter(appExecutors) {



        binding.rvStatus.layoutManager = GridLayoutManager(
            activity,
            2
        )

        binding.rvStatus.adapter = statusAdapter

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()


        viewModel.getFilterSettingCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetFilterSettingState.Success -> {
                        this.filterSetting = state.filterSetting
                        rgOrder.check(if (state.filterSetting.asc) R.id.rbOldest else R.id.rbLatest)
                        tokenPairAdapter.submitList(state.filterSetting.pairs)
                        addressAdapter.submitList(state.filterSetting.address)
                        statusAdapter.submitList(toDisplayStatus(state.filterSetting.status))

            
                    is GetFilterSettingState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        binding.tvApply.setOnClickListener {
            filterSetting?.orderFilter?.apply {
                addresses = addressAdapter.getData().filter {
                    it.isSelected
        .map {
                    it.name
        
                pairs = tokenPairAdapter.getData().filter {
                    it.isSelected
        .map {
                    val pair = it.name.split(OrderFilter.TOKEN_PAIR_SEPARATOR)
                    pair.first() to pair.last()
        .toMap()

                status = statusAdapter.getData().filter {
                    it.isSelected
        .map {
                    it.name
        

                oldest = binding.rbOldest.isChecked
    

            filterSetting?.orderFilter?.let {
                viewModel.saveOrderFilter(it)
    


        binding.tvReset.setOnClickListener {
            addressAdapter.resetFilter()
            statusAdapter.resetFilter()
            tokenPairAdapter.resetFilter()


        viewModel.saveFilterStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveFilterState.Success -> {
                        onSaveFilterSuccess()

            
                    is SaveFilterState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

    }

    private fun toDisplayStatus(status: List<FilterItem>): List<FilterItem> {
        return status.map {
            it.copy(
                displayName = when (it.name) {
                    Order.Status.OPEN.value -> getString(R.string.order_status_open)
                    Order.Status.INVALIDATED.value -> getString(R.string.order_status_invalidated)
                    Order.Status.CANCELLED.value -> getString(R.string.order_status_cancelled)
                    Order.Status.FILLED.value -> getString(R.string.order_status_filled)
                    Order.Status.IN_PROGRESS.value -> getString(R.string.order_status_in_progress)
                    else -> getString(R.string.order_status_unknown)
        
            )


    }

    private fun onSaveFilterSuccess() {
        activity?.onBackPressed()
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            FilterLimitOrderFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
        
    
    }
}
