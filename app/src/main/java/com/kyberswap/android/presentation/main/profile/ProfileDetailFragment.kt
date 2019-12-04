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
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentProfileDetailBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.KycInfo
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.UserInfo.Companion.KYC_STEP_PERSONAL_INFO
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.alert.ManageAlertAdapter
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.presentation.main.profile.kyc.ReSubmitState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.underline
import java.util.Locale
import javax.inject.Inject


class ProfileDetailFragment : BaseFragment() {

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
        binding.tvPDPAUpdate.underline(getString(R.string.about_pdpa_update))
        viewModel.getLoginStatus()
        viewModel.getUserInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        handlePDPA(binding.user?.kycInfo)
                        binding.tvKycTitle.visibility =
                            if (UserInfo.PENDING == state.userInfo?.kycStatus ||
                                UserInfo.BLOCKED == state.userInfo?.kycStatus ||
                                binding.tvKycStatus.text == getString(R.string.kyc_status_unverified)
                            ) View.VISIBLE else View.GONE
                        if (binding.user != state.userInfo) {
                            binding.user = state.userInfo

                            binding.lnVerify.visibility =
                                if (UserInfo.PENDING == state.userInfo?.kycStatus ||
                                    UserInfo.APPROVED == state.userInfo?.kycStatus
                                ) View.GONE else View.VISIBLE

                            binding.tvKycVerification.text =
                                if (UserInfo.REJECT == state.userInfo?.kycStatus) getString(R.string.profile_rejected) else getString(
                                    R.string.profile_verification_notification
                                )
                            binding.tvAction.text =
                                if (UserInfo.REJECT == state.userInfo?.kycStatus) getString(R.string.kyc_edit) else getString(
                                    R.string.profile_verify
                                )
                            binding.tvAction.visibility =
                                if (UserInfo.BLOCKED == state.userInfo?.kycStatus) View.GONE else View.VISIBLE
                            if (UserInfo.BLOCKED == state.userInfo?.kycStatus) {
                                binding.tvKycTitle.text = getString(R.string.profile_blocked)
                                binding.tvKycVerification.text = state.userInfo.blockReason
                            }
                        }
                    }
                    is UserInfoState.ShowError -> {
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
                    }
                }
            }
        })

        viewModel.refreshKycStatus.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == UserInfoState.Loading)
                when (state) {
                    is UserInfoState.Success -> {
                        navigateToKyc()
                    }
                    is UserInfoState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertsState.Success -> {
                        binding.isEmpty = state.alerts.isEmpty()
                        this.alerts.clear()
                        this.alerts.addAll(state.alerts)
                        alertAdapter.submitAlerts(state.alerts.take(2).sortedByDescending { it.status })
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

//        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
//            it?.getContentIfNotHandled()?.let { state ->
//                when (state) {
//                    is GetAlertsState.Success -> {
//                        alertAdapter.submitAlerts(state.alerts.take(2))
//                    }
//                    is GetAlertsState.ShowError -> {
//                        showAlert(
//                            state.message ?: getString(R.string.something_wrong),
//                            R.drawable.ic_info_error
//                        )
//                    }
//                }
//            }
//        })

        viewModel.logoutCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == LogoutState.Loading)
                when (state) {
                    is LogoutState.Success -> {
                        navigator.navigateToSignInScreen(currentFragment)
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

        binding.tvAction.setOnClickListener {
            viewModel.refreshKycStatus()

        }

        binding.tvPDPAUpdate.setOnClickListener {
            dialogHelper.showPDPAUpdate()
        }

        viewModel.reSubmitKycCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == ReSubmitState.Loading)
                when (state) {
                    is ReSubmitState.Success -> {
                        navigator.navigateToKYC(
                            currentFragment, KYC_STEP_PERSONAL_INFO
                        )
                    }
                    is ReSubmitState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private fun handlePDPA(kycInfo: KycInfo?) {
        val isFromSingapore =
            kycInfo?.country?.toLowerCase(Locale.getDefault()) == getString(R.string.country_singapore).toLowerCase(
                Locale.getDefault()
            ) ||
                kycInfo?.nationality?.toLowerCase(Locale.getDefault()) == getString(R.string.nationality_singaporean).toLowerCase(
                Locale.getDefault()
            ) ||
                kycInfo?.taxResidencyCountry?.toLowerCase(Locale.getDefault()) == getString(R.string.country_singapore).toLowerCase(
                Locale.getDefault()
            )

        binding.tvPDPAUpdate.visibility = if (isFromSingapore) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        viewModel.cancelPolling()
        super.onDestroyView()
    }

    private fun navigateToKyc() {
        binding.user?.let {
            if (it.isKycReject) {
                viewModel.reSubmit(it)
            } else {
                navigator.navigateToKYC(
                    currentFragment, it.kycStep
                )
            }
        }
    }

    companion object {
        fun newInstance() =
            ProfileDetailFragment()
    }
}
