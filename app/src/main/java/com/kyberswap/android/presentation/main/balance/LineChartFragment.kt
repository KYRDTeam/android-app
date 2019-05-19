package com.kyberswap.android.presentation.main.balance


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentLineChartBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class LineChartFragment : BaseFragment() {

    private lateinit var binding: FragmentLineChartBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var token: Token? = null

    private var chartType: ChartType? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LineChartViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = arguments!!.getParcelable(TOKEN_PARAM)
        chartType = arguments!!.getParcelable(CHART_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLineChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getChartData(token, chartType)
        viewModel.getChartCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetChartState.Success -> {

                    }

                    is GetChartState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })
    }

    companion object {
        private const val TOKEN_PARAM = "token_param"
        private const val CHART_TYPE = "chart_type"
        fun newInstance(token: Token?, type: ChartType) =
            LineChartFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TOKEN_PARAM, token)
                    putParcelable(CHART_TYPE, type)
                }
            }
    }


}
