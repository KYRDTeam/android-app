package com.kyberswap.android.presentation.main.profile


import android.content.Intent
import android.os.Bundle
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
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentProfileBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.di.ViewModelFactory
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.models.User
import kotlinx.android.synthetic.main.fragment_profile.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

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

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)
    }

    private val twitterAuthClient by lazy {
        TwitterAuthClient()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {

                    val request = GraphRequest.newMeRequest(
                        result?.accessToken
                    ) { me, response ->
                        if (response?.error != null) {
                            // handle error
                        } else {
                            val email = me?.optString("email")
                            val name = me?.optString("name")
                            val id = me?.optString("id")
                            val profileUrl = "https://graph.facebook.com/$id/picture?type=large"
                            val socialInfo = SocialInfo(
                                LoginType.FACEBOOK,
                                name,
                                result?.accessToken?.token,
                                profileUrl,
                                email
                            )
                            viewModel.loginWithFacebook(socialInfo)
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString(
                        "fields",
                        "id, name, email, gender,birthday,picture.type(large)"
                    )
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    Timber.e(error?.localizedMessage)
                    error?.printStackTrace()
                }
            })

        binding.tvSignUp.setOnClickListener {
            navigator.navigateToSignUpScreen(
                (activity as MainActivity).getCurrentFragment(), wallet
            )
        }
        binding.btnLogin.setOnClickListener {
            viewModel.login(edtEmail.text.toString(), edtPassword.text.toString())
        }

        viewModel.loginCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == LoginState.Loading)
                when (state) {
                    is LoginState.Success -> {
                        if (state.login.success) {
                            if (state.login.confirmSignUpRequired) {
                                showAlert(state.socialInfo.type.value)
                            } else {
                                showAlert(state.login.userInfo.name)
                            }
                        } else {
                            showAlert("Login fail")
                        }
                    }
                    is LoginState.ShowError -> {
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

        binding.imgGooglePlus.setOnClickListener {
            val googleSignInClient = GoogleSignIn.getClient(this.activity!!, gso)
            val account = GoogleSignIn.getLastSignedInAccount(this.activity)
            if (account == null) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            } else {

                val socialInfo = SocialInfo(
                    LoginType.GOOGLE,
                    account.displayName,
                    account.idToken,
                    account.photoUrl.toString(),
                    account.email
                )
                viewModel.loginWithGoogle(socialInfo)
            }
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.imgFacebook.setOnClickListener {
            val accessToken = AccessToken.getCurrentAccessToken()
            val isLoggedIn = accessToken != null && !accessToken.isExpired
            if (!isLoggedIn) {
                LoginManager.getInstance()
                    .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))
            }

            LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))
        }

        binding.imgTwitter.setOnClickListener {

            val twitterSession = TwitterCore.getInstance().sessionManager.activeSession
            if (twitterSession == null) {
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

            } else {
                getTwitterUserProfileWthTwitterCoreApi(twitterSession)
            }
        }

    }


    fun getTwitterUserProfileWthTwitterCoreApi(
        session: TwitterSession?
    ) {
        TwitterCore.getInstance().getApiClient(session).accountService
            .verifyCredentials(true, true, false)
            .enqueue(object : Callback<User>() {
                override fun success(result: com.twitter.sdk.android.core.Result<User>?) {
                    val name = result?.data?.name
                    val profileImageUrl = result?.data?.profileImageUrl?.replace("_normal", "")
                    Timber.e(name)
                }


                override fun failure(exception: TwitterException) {
                    Toast.makeText(context, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                viewModel.loginWithGoogle(socialInfo)
            }

        } catch (e: ApiException) {
            e.printStackTrace()
            Timber.e(e.localizedMessage)
        }

    }

    companion object {
        private const val RC_SIGN_IN = 1000
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }


}
