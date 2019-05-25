package com.kyberswap.android.presentation.main.balance.chart

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentChartBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class ChartFragment : BaseFragment() {

    private lateinit var binding: FragmentChartBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var token: Token? = null

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ChartViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = arguments!!.getParcelable(TOKEN_PARAM)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
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
        }

        binding.token = token

        val chartPagerAdapter =
            ChartPagerAdapter(
                childFragmentManager
            )

        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(
                token,
                ChartType.DAY
            ),
            getString(R.string.tab_day)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(
                token,
                ChartType.WEEK
            ),
            getString(R.string.tab_week)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(
                token,
                ChartType.MONTH
            ),
            getString(R.string.tab_month)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(
                token,
                ChartType.YEAR
            ),
            getString(R.string.tab_year)
        )
        chartPagerAdapter.addFragment(
            LineChartFragment.newInstance(
                token,
                ChartType.ALL
            ),
            getString(R.string.tab_all)
        )

        binding.vpChart.adapter = chartPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.vpChart)

        binding.tvBuy.setOnClickListener {
            wallet?.let {
                token?.let {
                    viewModel.save(wallet!!.address, token!!, false)
                }
            }

        }

        binding.tvSell.setOnClickListener {
            wallet?.let {
                token?.let {
                    viewModel.save(wallet!!.address, token!!, true)
                }
            }
        }

        binding.tvSend.setOnClickListener {
            wallet?.let {
                token?.let {
                    viewModel.saveSendToken(wallet!!.address, it)
                }
            }
        }

        viewModel.callbackSaveSend.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSendState.Loading)
                when (state) {
                    is SaveSendState.Success -> {
                        navigator.navigateToSendScreen(wallet)
                    }
                    is SaveSendState.ShowError -> {
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

        viewModel.callback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapDataState.Loading)
                when (state) {
                    is SaveSwapDataState.Success -> {
                        moveToSwapTab()
                    }
                    is SaveSwapDataState.ShowError -> {
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

    }

    private fun moveToSwapTab() {
        if (activity is MainActivity) {
            handler.post {
                activity!!.bottomNavigation.currentItem = 1
            }
        }
    }

    companion object {
        private const val TOKEN_PARAM = "token_param"
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?, token: Token?) =
            ChartFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TOKEN_PARAM, token)
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }
}
