package com.kyberswap.android.presentation.main.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentChartBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class ChartFragment : BaseFragment() {

    private lateinit var binding: FragmentChartBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var token: Token? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ChartViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = arguments!!.getParcelable(TOKEN_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()


        binding.token = token

        val chartPagerAdapter = ChartPagerAdapter(childFragmentManager)

        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(token),
            getString(R.string.tab_day)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(token),
            getString(R.string.tab_week)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(token),
            getString(R.string.tab_month)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(token),
            getString(R.string.tab_year)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(token),
            getString(R.string.tab_all)
        )

        binding.vpChart.adapter = chartPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.vpChart)
    }

    companion object {
        private const val TOKEN_PARAM = "token_param"
        fun newInstance(token: Token?) =
            ChartFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TOKEN_PARAM, token)
        
    
    }
}
