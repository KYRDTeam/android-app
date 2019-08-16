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
import com.kyberswap.android.databinding.FragmentManageAlertBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class ManageAlertFragment : BaseFragment(), LoginState {

    private lateinit var binding: FragmentManageAlertBinding

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

    private var userInfo: UserInfo? = null

    private val handler by lazy {
        Handler()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MangeAlertViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentManageAlertBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun getLoginStatus() {
        viewModel.getLoginStatus()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getLoginStatus()
        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        this.userInfo = state.userInfo
                        if (!(state.userInfo != null && state.userInfo.uid > 0)) {
                            activity?.onBackPressed()
                        }
                    }
                    is UserInfoState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })
        viewModel.getAlert()
        binding.rvAlert.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val alertAdapter =
            ManageAlertAdapter(appExecutors, handler,
                {
                    navigator.navigateToPriceAlertScreen(
                        currentFragment, it
                    )
                }, {
                    navigator.navigateToPriceAlertScreen(
                        currentFragment, it
                    )

                }, {
                    dialogHelper.showConfirmDeleteAlert(
                        {
                            viewModel.deleteAlert(it)
                        }
                    )

                })
        binding.rvAlert.adapter = alertAdapter

        binding.rvTrigger.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )


        val triggerAlertAdapter = ManageTriggerAlertAdapter(appExecutors, handler) {
            dialogHelper.showConfirmDeleteAlert(
                {
                    viewModel.deleteAlert(it)
                }
            )
        }

        viewModel.deleteAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DeleteAlertsState.Loading)
                when (state) {
                    is DeleteAlertsState.Success -> {
                        showAlert(getString(R.string.delete_alert_success))
                    }
                    is DeleteAlertsState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.rvTrigger.adapter = triggerAlertAdapter

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertsState.Success -> {
                        binding.isEmpty = state.alerts.isEmpty()
                        alertAdapter.submitAlerts(state.alerts.filter {
                            !it.isFilled
                        })

                        val triggerList = state.alerts.filter {
                            it.isFilled
                        }

                        binding.isEmptyTrigger = triggerList.isEmpty()

                        triggerAlertAdapter.submitAlerts(triggerList)
                    }
                    is GetAlertsState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.tvAdd.setOnClickListener {
            if (alertAdapter.getData().size >= 10) {
                dialogHelper.showDialogInfo(
                    title = getString(R.string.alert_limit_exceed), content = getString(
                        R.string.alert_limit_exceeds_instruction
                    )
                )
            } else {
                navigator.navigateToPriceAlertScreen(
                    currentFragment
                )
            }
        }


        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.imgAdd.setOnClickListener {
            if (alertAdapter.getData().size >= 10) {
                dialogHelper.showDialogInfo(
                    title = getString(R.string.alert_limit_exceed), content = getString(
                        R.string.alert_limit_exceeds_instruction
                    )
                )
            } else {
                navigator.navigateToPriceAlertScreen(
                    currentFragment
                )
            }

        }

        binding.imgLeaderBoard.setOnClickListener {
            navigator.navigateToLeaderBoard(
                currentFragment,
                userInfo
            )
        }

        binding.imgSetting.setOnClickListener {
            navigator.navigateToAlertMethod(currentFragment)
        }


    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            ManageAlertFragment()
    }


}
