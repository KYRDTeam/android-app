package com.kyberswap.android.presentation.main.profile


import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSignupBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.validPassword
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.models.User
import kotlinx.android.synthetic.main.fragment_signup.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class SignUpFragment : BaseFragment() {

    private lateinit var binding: FragmentSignupBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val gso by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.server_client_id))
            .build()
    }

    private val callbackManager by lazy {
        CallbackManager.Factory.create()
    }

    private val twitterAuthClient by lazy {
        TwitterAuthClient()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SignUpViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.btnRegister.setOnClickListener {

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

                binding.edtDisplayName.text.toString().isBlank() -> {
                    val errorMessage = getString(R.string.register_display_name_required)
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error), message = errorMessage
                    )
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

                !binding.edtPassword.text.toString().validPassword() -> {
                    val error = getString(R.string.register_invalid_password)
                    showAlertWithoutIcon(
                        title = getString(R.string.title_error),
                        message = error
                    )
                    binding.ilPassword.error = error
                }

                !binding.cbTermCondition.isChecked -> {
                    showAlert(getString(R.string.term_condition_notification))
                }

                else -> {
                    viewModel.signUp(
                        edtEmail.text.toString(),
                        edtDisplayName.text.toString(),
                        edtPassword.text.toString(),
                        cbSubscription.isChecked
                    )
                }
            }
        }

        viewModel.compositeDisposable.add(
            binding.edtEmail.textChanges()
                .skipInitialValue()
                .subscribe {
                    binding.ilEmail.error = null
                })

        viewModel.compositeDisposable.add(
            binding.edtDisplayName.textChanges()
                .skipInitialValue()
                .subscribe {
                    binding.ilDisplayName.error = null
                })

        viewModel.compositeDisposable.add(
            binding.edtPassword.textChanges()
                .skipInitialValue()
                .subscribe {
                    binding.ilPassword.error = null
                })

        viewModel.signUpCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SignUpState.Loading)
                when (state) {
                    is SignUpState.Success -> {
                        showAlertWithoutIcon(
                            title = getString(R.string.title_success),
                            message = getString(R.string.active_your_account),
                            timeInSecond = 10
                        )
                        navigator.navigateToSignInScreen(
                            currentFragment
                        )
                    }
                    is SignUpState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.imgBack.setOnClickListener {
            navigator.navigateToSignUpScreen(
                currentFragment
            )
        }


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
                }
            })

        binding.tvLogin.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

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

        binding.tvTermAndCondition.setOnClickListener {
            navigator.navigateToTermAndCondition()
        }

        viewModel.loginCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == LoginState.Loading)
                when (state) {
                    is LoginState.Success -> {
                        if (state.login.success) {
                            if (state.login.confirmSignUpRequired) {
                                navigator.navigateToSignUpConfirmScreen(
                                    (activity as MainActivity).getCurrentFragment(),
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
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })
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
        super.onDestroyView()
    }


    companion object {
        private const val RC_SIGN_IN = 1000
        fun newInstance() = SignUpFragment()
    }


}
