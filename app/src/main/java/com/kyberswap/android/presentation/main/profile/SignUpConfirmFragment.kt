package com.kyberswap.android.presentation.main.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentConfirmSignupBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.di.ViewModelFactory
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

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SignUpViewModel::class.java)
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

    private fun onLoginSuccess() {
        val fm = (activity as MainActivity).getCurrentFragment()?.childFragmentManager
        if (fm != null)
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
        navigator.navigateToProfileDetail(
            (activity as MainActivity).getCurrentFragment()
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.social = socialInfo

        binding.btnRegister.setOnClickListener {
            if (!binding.cbTermCondition.isChecked) {
                showAlert(getString(R.string.term_condition_notification))
                return@setOnClickListener
            }
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
                                onLoginSuccess()
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
                        showAlert(state.registerStatus.message)
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

        binding.tvTermAndCondition.setOnClickListener {
            navigator.navigateToTermAndCondition()
        }

        binding.tvLogin.setOnClickListener {
            navigator.navigateToSignInScreen(
                currentFragment
            )
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
