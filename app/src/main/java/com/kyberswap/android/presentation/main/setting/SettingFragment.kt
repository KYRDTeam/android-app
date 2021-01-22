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
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.databinding.FragmentSettingBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.setting.PassCodeLockActivity
import com.kyberswap.android.util.COMMUNITY_ICON
import com.kyberswap.android.util.SETTING_ALERT_METHOD
import com.kyberswap.android.util.SETTING_CHANGE_PIN
import com.kyberswap.android.util.SETTING_COMMUNITY
import com.kyberswap.android.util.SETTING_CONTACT
import com.kyberswap.android.util.SETTING_GET_STARTED
import com.kyberswap.android.util.SETTING_LIVECHAT
import com.kyberswap.android.util.SETTING_MANAGE_ALERT
import com.kyberswap.android.util.SETTING_MANAGE_WALLET
import com.kyberswap.android.util.SETTING_SUPPORT
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

    @Inject
    lateinit var mediator: StorageMediator

    @Inject
    lateinit var dialogHelper: DialogHelper

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
            analytics.logEvent(SETTING_MANAGE_WALLET, Bundle().createEvent())
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

            analytics.logEvent(SETTING_MANAGE_ALERT, Bundle().createEvent())
        }

        binding.lnGasWarning.setOnClickListener {
            dialogHelper.showGasWarningDialog(mediator.getGasPriceWarningValue(), {
                mediator.setGasPriceWarningValue(it)
            }, {
                showError("Please input valid gas price")
            })
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
            analytics.logEvent(SETTING_ALERT_METHOD, Bundle().createEvent())
        }

        binding.lnContact.setOnClickListener {
            navigator.navigateToContactScreen(currentFragment)
            analytics.logEvent(SETTING_CONTACT, Bundle().createEvent())
        }

        binding.lnSupport.setOnClickListener {
            val emailIntent =
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@kyberswap.com"))

            startActivity(Intent.createChooser(emailIntent, "Chooser Title"))
            analytics.logEvent(SETTING_SUPPORT, Bundle().createEvent())
        }

        binding.lnChangePin.setOnClickListener {
            startActivity(activity?.let { context ->
                PassCodeLockActivity.newIntent(
                    context,
                    PassCodeLockActivity.PASS_CODE_LOCK_TYPE_CHANGE
                )
            })
            analytics.logEvent(SETTING_CHANGE_PIN, Bundle().createEvent())

        }

        binding.lnAbount.setOnClickListener {
            openUrl(getString(R.string.setting_about_url))
            analytics.logEvent(SETTING_GET_STARTED, Bundle().createEvent())
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
            analytics.logEvent(
                SETTING_COMMUNITY,
                Bundle().createEvent(COMMUNITY_ICON, "telegram group")
            )
        }

//        binding.imgTelegramDeveloper.setOnClickListener {
//            openUrl(getString(R.string.setting_kyber_network_developer_url))
//        }

        binding.imgGithub.setOnClickListener {
            openUrl(getString(R.string.setting_github_url))
            analytics.logEvent(
                SETTING_COMMUNITY,
                Bundle().createEvent(COMMUNITY_ICON, "github")
            )
        }

        binding.imgTwitter.setOnClickListener {
            openUrl(getString(R.string.setting_twitter_url))
            analytics.logEvent(
                SETTING_COMMUNITY,
                Bundle().createEvent(COMMUNITY_ICON, "twitter")
            )
        }

        binding.imgFacebook.setOnClickListener {
            openUrl(getString(R.string.setting_facebook_url))
            analytics.logEvent(
                SETTING_COMMUNITY,
                Bundle().createEvent(COMMUNITY_ICON, "facebook")
            )
        }

        binding.imgMedium.setOnClickListener {
            openUrl(getString(R.string.setting_medium_url))
            analytics.logEvent(
                SETTING_COMMUNITY,
                Bundle().createEvent(COMMUNITY_ICON, "medium")
            )
        }

        binding.imgLinkedin.setOnClickListener {
            openUrl(getString(R.string.setting_linkedin_url))
            analytics.logEvent(
                SETTING_COMMUNITY,
                Bundle().createEvent(COMMUNITY_ICON, "linkedin")
            )
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
            analytics.logEvent(
                SETTING_LIVECHAT,
                Bundle().createEvent()
            )
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
        if (activity != null && isAdded) {
            if (unreadCount > 0) {
                tvBadge.visibility = View.VISIBLE
                tvBadge.text = unreadCount.toString()
            } else {
                tvBadge.visibility = View.GONE
                tvBadge.text = ""
            }
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
