package com.kyberswap.android.presentation.main.profile


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentProfileDetailBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.UserStatusChangeEvent
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.alert.ManageAlertAdapter
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.util.USER_TRANSFER_DATA_FORCE_LOGOUT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.isNetworkAvailable
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


class ProfileDetailFragment : BaseFragment(), LoginState {

    private lateinit var binding: FragmentProfileDetailBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val alerts = mutableListOf<Alert>()


    private val handler by lazy {
        Handler()
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ProfileDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getLoginStatus()
        viewModel.getUserInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        if (binding.user != state.userInfo) {
                            binding.user = state.userInfo
                        }
                    }
                    is UserInfoState.ShowError -> {
                        if (currentFragment is ProfileFragment && state.message.equals(
                                getString(R.string.not_authenticated_message), true
                            )
                        ) {
                            analytics.logEvent(
                                USER_TRANSFER_DATA_FORCE_LOGOUT,
                                Bundle().createEvent(ProfileDetailFragment::class.java.simpleName)
                            )
                            viewModel.logout()
                        }
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

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertsState.Success -> {
                        this.alerts.clear()
                        this.alerts.addAll(state.alerts)
                        binding.isEmpty = state.alerts.isEmpty()
                        alertAdapter.submitAlerts(
                            state.alerts.filter { !it.isFilled }.take(
                                2
                            )
                        )
                    }
                    is GetAlertsState.ShowError -> {
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
                    }
                }
            }
        })

        viewModel.deleteAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DeleteAlertsState.Loading)
                when (state) {
                    is DeleteAlertsState.Success -> {
                        showAlert(getString(R.string.delete_alert_success))
                    }
                    is DeleteAlertsState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.logoutCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == LogoutState.Loading)
                when (state) {
                    is LogoutState.Success -> {
                        EventBus.getDefault().post(UserStatusChangeEvent())
                        if (currentFragment is ProfileFragment) {
                            navigator.navigateToSignInScreen(currentFragment)
                        }
                    }
                    is LogoutState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        binding.tvLogout.setOnClickListener {
            dialogHelper.showConfirmation(
                getString(R.string.log_out), getString(R.string.lout_out_confirmation)
                , {
                    viewModel.logout()
                }, {

                })
        }

        binding.imgCreateAlert.setOnClickListener {
            if (alerts.size >= 10) {
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
                binding.user
            )
        }

        binding.tvMoreAlert.setOnClickListener {
            navigator.navigateToManageAlert(
                currentFragment
            )
        }
    }

    companion object {
        fun newInstance() =
            ProfileDetailFragment()
    }

    override fun getLoginStatus() {
        viewModel.getLoginStatus()
    }
}
