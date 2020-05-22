package com.kyberswap.android.presentation.main.setting


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freshchat.consumer.sdk.ConversationOptions
import com.freshchat.consumer.sdk.Freshchat
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.BuildConfig
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSettingBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.setting.PassCodeLockActivity
import com.kyberswap.android.util.ST_USER_CLICK_FAQ
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.openUrl
import com.kyberswap.android.util.ext.shareUrl
import kotlinx.android.synthetic.main.fragment_setting.*
import javax.inject.Inject


class SettingFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingBinding

    @Inject
    lateinit var navigator: Navigator

    private var hasUserInfo: Boolean = false

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private var isCounterStop: Boolean = false

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SettingViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun getLoginStatus() {
        viewModel.getLoginStatus()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getLoginStatus()
        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        hasUserInfo = state.userInfo != null && state.userInfo.uid > 0
                    }
                    is UserInfoState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
        binding.lnManageWallet.setOnClickListener {
            navigator.navigateToManageWalletFragment(
                currentFragment
            )
        }

        binding.lnManageAlert.setOnClickListener {
            if (hasUserInfo) {
                navigator.navigateToManageAlert(
                    currentFragment
                )
            } else {
                showAlertWithoutIcon(
                    message = getString(R.string.sign_in_required)
                )
            }
        }

        binding.lnAlertMethod.setOnClickListener {
            if (hasUserInfo) {
                navigator.navigateToAlertMethod(
                    currentFragment
                )
            } else {
                showAlertWithoutIcon(
                    message = getString(R.string.sign_in_required)
                )
            }
        }

        binding.lnContact.setOnClickListener {
            navigator.navigateToContactScreen(currentFragment)
        }

        binding.lnSupport.setOnClickListener {
            val emailIntent =
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@kyberswap.com"))

            startActivity(Intent.createChooser(emailIntent, "Chooser Title"))
        }

        binding.lnChangePin.setOnClickListener {
            startActivity(activity?.let { context ->
                PassCodeLockActivity.newIntent(
                    context,
                    PassCodeLockActivity.PASS_CODE_LOCK_TYPE_CHANGE
                )
            })

        }

        binding.lnAbount.setOnClickListener {
            openUrl(getString(R.string.setting_about_url))
        }

        binding.lnCommunity.setOnClickListener {
            openUrl(getString(R.string.setting_faq_url))
            analytics.logEvent(
                ST_USER_CLICK_FAQ,
                Bundle().createEvent()
            )
        }

        binding.imgTelegram.setOnClickListener {
            openUrl(getString(R.string.setting_kyber_network_url))
        }

//        binding.imgTelegramDeveloper.setOnClickListener {
//            openUrl(getString(R.string.setting_kyber_network_developer_url))
//        }

        binding.imgGithub.setOnClickListener {
            openUrl(getString(R.string.setting_github_url))
        }

        binding.imgTwitter.setOnClickListener {
            openUrl(getString(R.string.setting_twitter_url))
        }

        binding.imgFacebook.setOnClickListener {
            openUrl(getString(R.string.setting_facebook_url))
        }

        binding.imgMedium.setOnClickListener {
            openUrl(getString(R.string.setting_medium_url))
        }

        binding.imgLinkedin.setOnClickListener {
            openUrl(getString(R.string.setting_linkedin_url))
        }

        binding.tvShare.setOnClickListener {
            shareUrl(getString(R.string.setting_share_info))
        }

        binding.lnRateMyApp.setOnClickListener {
            openUrl(getString(R.string.setting_rate_my_app))
        }

        binding.tvVersion.text =
            String.format(getString(R.string.app_version), BuildConfig.VERSION_NAME)


        registerBroadcastReceiver()

        context?.applicationContext?.let {
            Freshchat.getInstance(it)
                .getUnreadCountAsync { _, unreadCount ->
                    showBadge(unreadCount)
                }
        }

        binding.fabChat.setOnClickListener {
            val lConvOptions = ConversationOptions()
            lConvOptions.filterByTags(listOf("conversations"), "")
            Freshchat.showConversations(it.context, lConvOptions)
            (context?.applicationContext as KyberSwapApplication).stopCounter()
            isCounterStop = true
        }
    }

    private fun getLocalBroadcastManager(): LocalBroadcastManager? {
        return activity?.applicationContext?.let { LocalBroadcastManager.getInstance(it) }
    }


    override fun onResume() {
        if (isCounterStop) {
            isCounterStop = false
            (context?.applicationContext as KyberSwapApplication).startCounter()
        }
        super.onResume()
    }

    private fun registerBroadcastReceiver() {
        val intentFilterUnreadMessagCount =
            IntentFilter(Freshchat.FRESHCHAT_UNREAD_MESSAGE_COUNT_CHANGED)
        getLocalBroadcastManager()?.registerReceiver(
            unreadCountChangeReceiver,
            intentFilterUnreadMessagCount
        )
    }

    private var unreadCountChangeReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                activity?.applicationContext?.let {
                    Freshchat.getInstance(it)
                        .getUnreadCountAsync { _, unreadCount ->
                            showBadge(unreadCount)
                        }
                }
            }
        }

    private fun showBadge(unreadCount: Int) {
        if (unreadCount > 0) {
            tvBadge.visibility = View.VISIBLE
            tvBadge.text = unreadCount.toString()
        } else {
            tvBadge.text = ""
            tvBadge.visibility = View.GONE
        }
    }

    companion object {
        fun newInstance() =
            SettingFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
