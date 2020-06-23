package com.kyberswap.android.presentation.main.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentConfirmSignupBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_DISMISS
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_NO
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_NO_CONTINUE
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_YES
import com.kyberswap.android.util.USER_TRANSFER_DATA_FORCE_LOGOUT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.isNetworkAvailable
import javax.inject.Inject


class SignUpConfirmFragment : BaseFragment() {

    private lateinit var binding: FragmentConfirmSignupBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var socialInfo: SocialInfo? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SignUpViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
        socialInfo = arguments!!.getParcelable(SOCIAL_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfirmSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onLoginSuccess(userInfo: UserInfo) {
        val fm = (activity as MainActivity).getCurrentFragment()?.childFragmentManager
        if (fm != null)
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
        navigator.navigateToProfileDetail(
            (activity as MainActivity).getCurrentFragment()
        )
    }

    private fun showDataTransferDialog(userInfo: UserInfo) {
        if (userInfo.transferPermission.equals(
                getString(R.string.undecided_transfer_data),
                true
            )
        ) {
            dialogHelper.showDataTransformationDialog(
                {
                    viewModel.transfer(getString(R.string.transfer_action_yes), userInfo)
                    analytics.logEvent(
                        USER_CLICK_DATA_TRANSFER_YES,
                        Bundle().createEvent(SignUpConfirmFragment::class.java.simpleName)
                    )
                }, {
                    analytics.logEvent(
                        USER_CLICK_DATA_TRANSFER_NO,
                        Bundle().createEvent(SignUpConfirmFragment::class.java.simpleName)
                    )
                    dialogHelper.showConfirmDataTransfer({
                        analytics.logEvent(
                            USER_CLICK_DATA_TRANSFER_NO_CONTINUE,
                            Bundle().createEvent(SignUpConfirmFragment::class.java.simpleName)
                        )
                        viewModel.transfer(getString(R.string.transfer_action_no), userInfo)
                    }, {
                        it.show()
                    }, {
                        it.show()
                    })
                }, {
                    analytics.logEvent(
                        USER_CLICK_DATA_TRANSFER_DISMISS,
                        Bundle().createEvent(SignUpConfirmFragment::class.java.simpleName)
                    )
                    if (userInfo.forceLogout) {
                        analytics.logEvent(
                            USER_TRANSFER_DATA_FORCE_LOGOUT,
                            Bundle().createEvent(SignUpConfirmFragment::class.java.simpleName)
                        )
                        viewModel.logout()
                    } else {
                        onLoginSuccess(userInfo)
                    }
                }
            )
        } else {
            onLoginSuccess(userInfo)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.social = socialInfo

        binding.btnRegister.setOnClickListener {
            socialInfo?.let { info ->
                viewModel.login(info.copy(subscription = binding.cbSubscription.isChecked), true)
            }
        }


        viewModel.loginCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == LoginState.Loading)
                when (state) {
                    is LoginState.Success -> {
                        if (state.login.success) {
                            if (state.login.confirmSignUpRequired) {
                                navigator.navigateToSignUpConfirmScreen(
                                    currentFragment,
                                    state.socialInfo
                                )
                            } else {
                                showDataTransferDialog(state.login.userInfo)
                            }
                        } else {
                            showAlert(state.login.message)
                        }
                    }
                    is LoginState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.signUpCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SignUpState.Loading)
                when (state) {
                    is SignUpState.Success -> {
                        if (state.registerStatus.success) {
                            showAlert(state.registerStatus.message)
                        } else {
                            showAlertWithoutIcon(
                                title = getString(R.string.title_error),
                                message = state.registerStatus.message,
                                timeInSecond = 10
                            )
                        }
                    }
                    is SignUpState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })


        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.tvLogin.setOnClickListener {
            navigator.navigateToSignInScreen(
                currentFragment
            )
        }

        viewModel.dataTransferCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {

                    is DataTransferState.Success -> {
                        if (state.userInfo != null) {
                            onTransferDataCompleted(state.userInfo)
                        }
                    }
                    is DataTransferState.ShowError -> {
                        if (state.userInfo != null) {
                            onTransferDataCompleted(state.userInfo)
                        }
                        if (isNetworkAvailable()) {
                            showError(
                                state.message ?: getString(R.string.something_wrong)
                            )
                        }
                    }
                }
            }
        })
    }

    private fun onTransferDataCompleted(userInfo: UserInfo) {
        if (userInfo.transferPermission.equals(
                getString(R.string.transfer_action_no),
                true
            )
            && userInfo.forceLogout
        ) {
            analytics.logEvent(
                USER_TRANSFER_DATA_FORCE_LOGOUT,
                Bundle().createEvent(SignUpConfirmFragment::class.java.simpleName)
            )
            viewModel.logout()
        } else {
            onLoginSuccess(userInfo)
            if (userInfo.transferPermission.equals(getString(R.string.transfer_action_yes), true)) {
                showAlert(
                    getString(R.string.transfer_data_success),
                    R.drawable.ic_check
                )
            }
        }
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val SOCIAL_PARAM = "social_param"
        fun newInstance(socialInfo: SocialInfo?) =
            SignUpConfirmFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SOCIAL_PARAM, socialInfo)
                }
            }
    }
}
