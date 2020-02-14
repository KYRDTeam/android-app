package com.kyberswap.android.presentation.main.profile


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookAuthorizationException
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentProfileBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_DISMISS
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_NO
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_NO_CONTINUE
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_YES
import com.kyberswap.android.util.USER_TRANSFER_DATA_FORCE_LOGOUT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.models.User
import kotlinx.android.synthetic.main.fragment_profile.*
import timber.log.Timber
import java.util.Arrays
import javax.inject.Inject


class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding

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

    private val handler by lazy { Handler() }

    private var resetPasswordDialog: AlertDialog? = null

    private val fromLimitOrder: Boolean
        get() = if (activity is MainActivity) (activity as MainActivity).fromLimitOrder else false

    private val gso by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.server_client_id))
            .build()
    }

    private val callbackManager by lazy {
        CallbackManager.Factory.create()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)
    }

    private val twitterAuthClient by lazy {
        TwitterAuthClient()
    }

    private var twoFADialog: AlertDialog? = null

    @Inject
    lateinit var analytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getLoginStatus()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.accessToken?.let { meRequest(it) }
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    Timber.e(error?.localizedMessage)
                    error?.printStackTrace()
                    if (error is FacebookAuthorizationException) {
                        if (AccessToken.getCurrentAccessToken() != null) {
                            LoginManager.getInstance().logOut()
                            loginWithReadPermission()
                        }
                    }
                }
            })

        binding.tvSignUp.setOnClickListener {
            navigator.navigateToSignUpScreen(
                (activity as MainActivity).getCurrentFragment()
            )
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
                            } else if (state.login.twoFaRequired) {
                                dialogHelper.show2FaDialog { token, dialog ->
                                    this.twoFADialog = dialog
                                    if (token.isEmpty()) {
                                        showAlertWithoutIcon(
                                            title = getString(R.string.title_error),
                                            message = getString(R.string.two_fa_empty_code)
                                        )
                                        return@show2FaDialog
                                    }

                                    if (!state.socialInfo.isNormalLogin) {
                                        viewModel.login(state.socialInfo.copy(twoFa = token))
                                        dialog.dismiss()
                                    } else {
                                        viewModel.login(
                                            edtEmail.text.toString(),
                                            edtPassword.text.toString(),
                                            token
                                        )
                                        dialog.dismiss()
                                    }

                                }
                            } else {

//                                if (state.login.userInfo.transferPermission.equals(
//                                        getString(R.string.undecided_transfer_data),
//                                        false
//                                    )
//                                ) {
//                                    dialogHelper.showDataTransformationDialog(
//                                        {
//
//                                        }, {
//
//                                        }, {
//                                            onLoginSuccess(state.login.userInfo)
//                                        }
//                                    )
//                                } else {
//
//                                    onLoginSuccess(state.login.userInfo)
//                                }

                                showDataTransferDialog(state.login.userInfo)
                            }
                        } else {
                            twoFADialog?.show()
                            showError(state.login.message)
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


        viewModel.resetPasswordCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == ResetPasswordState.Loading)
                when (state) {
                    is ResetPasswordState.Success -> {
                        if (state.status.success) {
                            resetPasswordDialog?.dismiss()
                            resetPasswordDialog = null
                            showAlertWithoutIcon(message = state.status.message)
                        } else {
                            showAlertWithoutIcon(
                                title = getString(R.string.title_error),
                                message = state.status.message
                            )
                        }
                    }
                    is ResetPasswordState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        if (state.userInfo != null && state.userInfo.uid > 0) {

//                            if (state.userInfo.transferPermission.equals(
//                                    getString(R.string.undecided_transfer_data),
//                                    false
//                                )
//                            ) {
//                                dialogHelper.showDataTransformationDialog(
//                                    {
//
//                                    }, {
//
//                                    }, {
//                                        navigator.navigateToProfileDetail(
//                                            profileFragment
//                                        )
//                                    }
//                                )
//                            } else {
//                                navigator.navigateToProfileDetail(
//                                    profileFragment
//                                )
//                            }
                            navigateToProfileDetail(profileFragment)
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

        binding.imgFacebook.setOnClickListener {
            stopCounter()
            LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))
        }

        binding.imgGooglePlus.setOnClickListener {
            stopCounter()
            val googleSignInClient = GoogleSignIn.getClient(this.activity!!, gso)
            val account = GoogleSignIn.getLastSignedInAccount(this.activity)
            if (account == null) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            } else {
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            }
        }

        binding.imgTwitter.setOnClickListener {
            stopCounter()

            val twitterSession = TwitterCore.getInstance().sessionManager.activeSession
            if (twitterSession != null) {
                TwitterCore.getInstance().sessionManager.clearActiveSession()
            }
            twitterAuthClient.authorize(activity, object : Callback<TwitterSession>() {
                override fun success(result: com.twitter.sdk.android.core.Result<TwitterSession>?) {
                    getTwitterUserProfileWthTwitterCoreApi(result?.data)
                }

                override fun failure(exception: TwitterException?) {
                    exception?.printStackTrace()
                    Toast.makeText(
                        context,
                        exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        binding.tvForgotPassword.setOnClickListener {
            dialogHelper.showResetPassword { email, dialog ->
                resetPasswordDialog = dialog
                if (email.isEmpty()) {
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error),
                        message = getString(R.string.login_email_address_required)
                    )
                } else {
                    viewModel.resetPassword(email)
                }
            }
        }

        binding.btnLogin.setOnClickListener {

            when {
                binding.edtEmail.text.toString().isBlank() -> {
                    val errorMessage = getString(
                        R.string.login_email_address_required
                    )
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error), message = errorMessage
                    )

                    binding.ilEmail.error = errorMessage
                }

                !Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text).matches() -> {

                    val errorMessage = getString(
                        R.string.login_invalid_email_address
                    )
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error), message = errorMessage
                    )

                    binding.ilEmail.error = errorMessage
                }

                binding.edtPassword.text.toString().isBlank() -> {
                    val errorMessage = getString(
                        R.string.login_password_required
                    )
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error), message = errorMessage
                    )
                    binding.ilPassword.error = errorMessage
                }

                else -> {
                    viewModel.login(edtEmail.text.toString(), edtPassword.text.toString())
                }

            }

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

        viewModel.compositeDisposable.add(
            binding.edtEmail.textChanges()
                .skipInitialValue()
                .subscribe {
                    binding.ilEmail.error = null
                })

        viewModel.compositeDisposable.add(
            binding.edtPassword.textChanges()
                .skipInitialValue()
                .subscribe {
                    binding.ilPassword.error = null
                })
    }

    private fun onTransferDataCompleted(userInfo: UserInfo) {
        if (userInfo.transferPermission.equals(
                getString(R.string.transfer_action_no),
                true
            )
            && userInfo.forceLogout
        ) {
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
                        Bundle().createEvent(ProfileFragment::class.java.simpleName)
                    )
                }, {
                    analytics.logEvent(
                        USER_CLICK_DATA_TRANSFER_NO,
                        Bundle().createEvent(ProfileFragment::class.java.simpleName)
                    )
                    dialogHelper.showConfirmDataTransfer({
                        viewModel.transfer(getString(R.string.transfer_action_no), userInfo)
                        analytics.logEvent(
                            USER_CLICK_DATA_TRANSFER_NO_CONTINUE,
                            Bundle().createEvent(ProfileFragment::class.java.simpleName)
                        )
                    }, {
                        it.show()
                    }, {
                        it.show()
                    })
                }, {
                    analytics.logEvent(
                        USER_CLICK_DATA_TRANSFER_DISMISS,
                        Bundle().createEvent(ProfileFragment::class.java.simpleName)
                    )
                    if (userInfo.forceLogout) {
                        viewModel.logout()
                        analytics.logEvent(
                            USER_TRANSFER_DATA_FORCE_LOGOUT,
                            Bundle().createEvent(ProfileFragment::class.java.simpleName)
                        )
                    } else {
                        onLoginSuccess(userInfo)
                    }
                }
            )
        } else {
            onLoginSuccess(userInfo)
        }
    }

    private fun onLoginSuccess(userInfo: UserInfo) {
        twoFADialog = null
        showAlertWithoutIcon(
            message = String.format(
                getString(R.string.wellcome_back),
                userInfo.name
            )
        )

        if (fromLimitOrder) {
            moveToLimitOrder()
        }

        navigateToProfileDetail(currentFragment)
    }

    private fun loginWithReadPermission() {
        LoginManager.getInstance()
            .logInWithReadPermissions(
                this,
                Arrays.asList("email", "public_profile")
            )
    }

    private fun moveToLimitOrder() {
        (activity as? MainActivity)?.moveToTab(MainPagerAdapter.LIMIT_ORDER)
    }

    private fun navigateToProfileDetail(currentFragment: Fragment?) {
        navigator.navigateToProfileDetail(
            currentFragment
        )
    }

    private fun meRequest(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(
            accessToken
        ) { me, response ->
            if (response?.error != null) {
                showAlert(response.error.errorMessage)
            } else {
                val email = me?.optString("email")
                val name = me?.optString("name")
                val id = me?.optString("id")
                val profileUrl = "https://graph.facebook.com/$id/picture?type=large"
                val socialInfo = SocialInfo(
                    LoginType.FACEBOOK,
                    name,
                    accessToken.token,
                    profileUrl,
                    email
                )
                viewModel.login(socialInfo)
            }
        }

        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id, name, email, gender, birthday, picture.type(large)"
        )
        request.parameters = parameters
        request.executeAsync()
    }


    private fun getTwitterUserProfileWthTwitterCoreApi(
        session: TwitterSession?
    ) {
        TwitterCore.getInstance().getApiClient(session).accountService
            .verifyCredentials(true, true, false)
            .enqueue(object : Callback<User>() {
                override fun success(result: com.twitter.sdk.android.core.Result<User>?) {
                    val name = result?.data?.name

                    val profileImageUrl = result?.data?.profileImageUrl?.replace("_normal", "")
                    val socialInfo = SocialInfo(
                        LoginType.TWITTER,
                        name,
                        session?.authToken?.token,
                        profileImageUrl,
                        null,
                        false,
                        session?.authToken?.token,
                        session?.authToken?.secret
                    )


                    viewModel.login(socialInfo)
                }

                override fun failure(exception: TwitterException) {
                    Toast.makeText(context, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        startCounter()
        callbackManager.onActivityResult(requestCode, resultCode, data)
        twitterAuthClient.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                val socialInfo = SocialInfo(
                    LoginType.GOOGLE,
                    account.displayName,
                    account.idToken,
                    account.photoUrl.toString(),
                    account.email
                )
                viewModel.login(socialInfo)
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            Timber.e(e.localizedMessage)
        }
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        private const val RC_SIGN_IN = 1000
        fun newInstance() =
            ProfileFragment()
    }
}
