package com.kyberswap.android.presentation.main.alert


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
import com.kyberswap.android.databinding.FragmentLeaderBoardBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.alert.GetLeaderBoardState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class LeaderBoardFragment : BaseFragment() {

    private lateinit var binding: FragmentLeaderBoardBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val handler by lazy {
        Handler()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LeaderBoardViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getLeaderBoard()

        binding.rvLeaderBoard.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )


        val adapter = LeaderBoardAlertAdapter(appExecutors)

        binding.rvLeaderBoard.adapter = adapter

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetLeaderBoardState.Loading)
                when (state) {
                    is GetLeaderBoardState.Success -> {
                        adapter.submitAlerts(state.alerts)
                    }
                    is GetLeaderBoardState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })


        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            LeaderBoardFragment()
    }


}
