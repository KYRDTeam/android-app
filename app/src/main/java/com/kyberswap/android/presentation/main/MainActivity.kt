package com.kyberswap.android.presentation.main

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityMainBinding
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.UserStatusChangeEvent
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletChangeEvent
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.common.AlertDialogFragment
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.BalanceFragment
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.main.balance.GetPendingTransactionState
import com.kyberswap.android.presentation.main.balance.GetRatingInfoState
import com.kyberswap.android.presentation.main.balance.WalletAdapter
import com.kyberswap.android.presentation.main.balance.send.SendFragment
import com.kyberswap.android.presentation.main.limitorder.LimitOrderFragment
import com.kyberswap.android.presentation.main.limitorder.LimitOrderV2Fragment
import com.kyberswap.android.presentation.main.limitorder.OrderConfirmFragment
import com.kyberswap.android.presentation.main.notification.GetUnReadNotificationsState
import com.kyberswap.android.presentation.main.profile.DataTransferState
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.profile.kyc.PassportFragment
import com.kyberswap.android.presentation.main.profile.kyc.PersonalInfoFragment
import com.kyberswap.android.presentation.main.profile.kyc.SubmitFragment
import com.kyberswap.android.presentation.main.setting.SettingFragment
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.presentation.main.walletconnect.WalletConnectFragment
import com.kyberswap.android.presentation.wallet.UpdateWalletState
import com.kyberswap.android.util.CLICK_WALLET_CONNECT_EVENT
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_DISMISS
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_NO
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_NO_CONTINUE
import com.kyberswap.android.util.USER_CLICK_DATA_TRANSFER_YES
import com.kyberswap.android.util.USER_TRANSFER_DATA_FORCE_LOGOUT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.openUrl
import com.kyberswap.android.util.ext.toLongSafe
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer.*
import kotlinx.android.synthetic.main.layout_drawer.view.*
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MainActivity : BaseActivity(), KeystoreStorage, AlertDialogFragment.Callback,
    ForceUpdateChecker.OnUpdateNeededListener {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var alert: NotificationAlert? = null

    private var limitOrder: NotificationLimitOrder? = null

    private var notification: Notification? = null

    private var isPromoCode: Boolean = false

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var currentFragment: Fragment? = null

    private var hasPendingTransaction: Boolean? = false

    private var hasPendingNotification: Boolean? = false

    private val showPendingNotification: Boolean
        get() = hasPendingNotification == true && isLoggedIn

    private var listener: ViewPager.OnPageChangeListener? = null

    var fromLimitOrder: Boolean = false

    private var currentDialogFragment: DialogFragment? = null

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private var hasDone = false

    val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    private var _isLoggedIn = false

    val isLoggedIn: Boolean
        get() = _isLoggedIn

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val walletConnect by lazy {
        listOf(binding.drawerLayout.tvWalletConnect, binding.drawerLayout.imgWalletConnect)
    }

    private val isBigSwing: Boolean
        get() = notification?.isBigSwing == true

    private val isNewListing: Boolean
        get() = notification?.isNewListing == true

    private val isAlert: Boolean
        get() = alert != null

    private val isLimitOrder: Boolean
        get() = limitOrder != null

    private val isPromotion: Boolean
        get() = notification?.isPromotion == true

    private val isOther: Boolean
        get() = notification?.isOther == true

    private val isGeneralNotification: Boolean
        get() = notification?.id == 0L

    val pendingTransactions = mutableListOf<Transaction>()

    private var adapter: MainPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WalletManager.storage = this
        WalletManager.scanWallets()

        ForceUpdateChecker.with(this).onUpdateNeeded(this).check()

        alert = intent.getParcelableExtra(ALERT_PARAM)
        limitOrder = intent.getParcelableExtra(LIMIT_ORDER_PARAM)
        isPromoCode = intent.getBooleanExtra(IS_PROMO_CODE_PARAM, false)
        notification = intent.getParcelableExtra(NOTIFICATION_PARAM)

        binding.viewModel = mainViewModel
        val tabColors =
            applicationContext.resources.getIntArray(R.array.tab_colors)
        val navigationAdapter = AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu)
        navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors)

        bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW

        bottomNavigation.isForceTint = false
        bottomNavigation.accentColor =
            ContextCompat.getColor(this, R.color.bottom_item_color_active)
        bottomNavigation.inactiveColor =
            ContextCompat.getColor(this, R.color.bottom_item_color_normal)
        bottomNavigation.setOnTabSelectedListener { position, _ ->
            handler.post {
                binding.vpNavigation.setCurrentItem(position, true)
            }

        }

        adapter = MainPagerAdapter(
            supportFragmentManager,
            alert,
            limitOrder,
            if (isBigSwing || isNewListing) notification else null
        )

        binding.vpNavigation.adapter = adapter
        binding.vpNavigation.offscreenPageLimit = 4
        listener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentFragment = adapter?.getRegisteredFragment(position)
                currentFragment?.let {
                    if (!isNetworkAvailable()) {
                        showNetworkUnAvailable()
                    }
                }
                showPendingIndicator()
                when (currentFragment) {
                    is BalanceFragment -> {
                        (currentFragment as BalanceFragment).scrollToTop()
                        updateLoginStatus()
                    }
                    is LimitOrderFragment -> {
                        with((currentFragment as LimitOrderFragment)) {
                            getLimitOrder()
                            getLoginStatus()
                            checkEligibleAddress()
                            verifyEligibleWallet()
                        }
                        updateLoginStatus()
                    }

                    is LimitOrderV2Fragment -> {
                        with((currentFragment as LimitOrderV2Fragment)) {
                            getLimitOrder()
                            getLoginStatus()
                            checkEligibleAddress()
                            verifyEligibleWallet()
                        }

                        updateLoginStatus()
                    }

                    is SwapFragment -> {
                        with((currentFragment as SwapFragment)) {
                            getSwap()
                            getKyberEnable()
                            verifyEligibleWallet()
                        }
                        updateLoginStatus()
                    }
                    is SettingFragment -> {
                        (currentFragment as SettingFragment).getLoginStatus()
                        updateLoginStatus()
                    }

                    is ProfileFragment -> {
                        updateLoginStatus()
                    }

                }
            }
        }

        listener?.let {
            binding.vpNavigation.addOnPageChangeListener(it)
        }

        val initial =
            if (isLimitOrder) {
                MainPagerAdapter.LIMIT_ORDER
            } else if (isAlert || isPromoCode || isBigSwing || isNewListing) {
                MainPagerAdapter.SWAP
            } else {
                MainPagerAdapter.BALANCE
            }

        binding.vpNavigation.post {
            listener?.onPageSelected(initial)
        }

        if (initial != MainPagerAdapter.BALANCE) {
            bottomNavigation.currentItem = initial
            binding.vpNavigation.currentItem = initial
        }

        walletConnect.forEach {
            it.setOnClickListener {
                showDrawer(false)
                if (!isNetworkAvailable()) {
                    showNetworkUnAvailable()
                    return@setOnClickListener
                }
                firebaseAnalytics.logEvent(
                    CLICK_WALLET_CONNECT_EVENT, Bundle().createEvent("1")
                )
                if (!isWalletConnectOpen) {
                    IntentIntegrator(this)
                        .setBeepEnabled(false)
                        .initiateScan()
                }

            }
        }

        binding.navView.rvWallet.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )

        val walletAdapter =
            WalletAdapter(appExecutors) {

                showDrawer(false)
                handler.postDelayed(
                    {

                        mainViewModel.updateSelectedWallet(it.copy(isSelected = true))
                    }, 250
                )
            }
        binding.navView.rvWallet.adapter = walletAdapter

        mainViewModel.getWallets()
        mainViewModel.getAllWalletStateCallback.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAllWalletState.Success -> {
                        val selectedWallet = state.wallets.find { it.isSelected }
                        if (wallet?.address != selectedWallet?.address) {
                            selectedWallet?.let {
                                handler.postDelayed(
                                    {
                                        mainViewModel.pollingTokenBalance(
                                            state.wallets,
                                            it
                                        )
                                    }, 250

                                )

                            }
                            wallet = selectedWallet
                            wallet?.let {
                                mainViewModel.getPendingTransaction(it)
                                mainViewModel.getTransactionPeriodically(it)
                                mainViewModel.checkEligibleWallet(it)

                            }
                        }
                        walletAdapter.submitList(listOf())
                        walletAdapter.submitList(state.wallets)
                    }
                    is GetAllWalletState.ShowError -> {
                        navigator.navigateToLandingPage()
                    }
                }
            }
        })

        mainViewModel.getMaxGasPrice()

        mainViewModel.switchWalletCompleteCallback.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                showProgress(state == UpdateWalletState.Loading)
                when (state) {
                    is UpdateWalletState.Success -> {
                        if (state.isWalletChangeEvent) {
                            EventBus.getDefault().post(WalletChangeEvent(state.wallet.address))
                        }
                    }
                    is UpdateWalletState.ShowError -> {

                    }
                }
            }
        })

        mainViewModel.getLoginStatus()
        mainViewModel.getLoginStatusCallback.observe(this, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->

                when (state) {
                    is UserInfoState.Success -> {
                        val userId = state.userInfo?.uid ?: 0
                        if (_isLoggedIn != (userId > 0)) {
                            _isLoggedIn = userId > 0
                            mainViewModel.getNotifications()
                        }
                    }
                    is UserInfoState.ShowError -> {
                        _isLoggedIn = false
                    }
                }
            }
        })

        mainViewModel.getPendingTransactionStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPendingTransactionState.Success -> {
                        val txList = state.transactions.filter {
                            it.blockNumber.isNotEmpty() && it.blockNumber.toLongSafe() != Transaction.DEFAULT_DROPPED_BLOCK_NUMBER
                        }

                        txList.forEach { transaction ->
                            hasDone = true
                            showDialog(
                                AlertDialogFragment.DIALOG_TYPE_DONE,
                                transaction.copy(isCancel = state.transactions.filter { it.blockNumber.isEmpty() }
                                    .any { tx -> transaction.nonce == tx.nonce })
                            )
                        }

                        val pendingList =
                            state.transactions.filter { it.blockNumber.isEmpty() && !it.isCancel }

                        Timber.e(
                            Gson().toJson(txList)
                        )
                        if (currentDialogFragment != null) {
                            handler.postDelayed(
                                {
                                    if (pendingList.isEmpty() && txList.isEmpty() && hasDone) {
                                        if (currentDialogFragment is AlertDialogFragment) {
                                            if (!(currentDialogFragment as AlertDialogFragment).isDone) {
                                                currentDialogFragment?.dismissAllowingStateLoss()
                                                hasDone = false
                                                currentDialogFragment = null
                                            }
                                        }
                                    }
                                }, 500
                            )
                        }

                        pendingTransactions.clear()
                        pendingTransactions.addAll(pendingList)
                        setPendingTransaction(pendingList.size)
                    }
                    is GetPendingTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })

        imgClose.setOnClickListener {
            showDrawer(false)
        }

        tvNotification.setOnClickListener {
            showDrawer(false)
            handler.postDelayed({
                navigator.navigateToNotificationScreen(currentFragment)
            }, 250)

        }

        tvTransaction.setOnClickListener {
            showDrawer(false)
            handler.postDelayed(
                {
                    wallet?.let {
                        navigator.navigateToTransactionScreen(
                            currentFragment,
                            it
                        )
                    }
                }, 250
            )
        }

        tvKyberCode.setOnClickListener {
            showDrawer(false)
            handler.postDelayed(
                {
                    wallet?.let {
                        navigator.navigateToKyberCode(
                            currentFragment
                        )
                    }
                }, 250
            )
        }

        imgAdd.setOnClickListener {
            showDrawer(false)
            handler.postDelayed(
                {
                    dialogHelper.showBottomSheetDialog(
                        {
                            dialogHelper.showConfirmation {
                                mainViewModel.createWallet()
                            }

                        },
                        {
                            navigator.navigateToImportWalletPage()

                        }
                    )
                }, 250
            )
        }

        mainViewModel.createWalletCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CreateWalletState.Loading)
                when (state) {
                    is CreateWalletState.Success -> {
                        showAlert(getString(R.string.create_wallet_success)) {
                            navigator.navigateToBackupWalletPage(state.words, state.wallet, true)

                        }
                    }
                    is CreateWalletState.ShowError -> {
                        showError(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        mainViewModel.getNotificationsCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetUnReadNotificationsState.Success -> {
                        setPendingNotification(state.notifications)
                    }
                    is GetUnReadNotificationsState.ShowError -> {
                        hasPendingNotification = false
                        setPendingNotification(0)
                    }
                }
            }
        })



        mainViewModel.getRatingInfoCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetRatingInfoState.Success -> {
                        dialogHelper.showRatingDialog({
                            showAlertWithoutIcon(message = getString(R.string.rating_thank_you))
                        }, {
                            mainViewModel.saveRatingFinish()
                        },
                            {
                                mainViewModel.saveNotNow()
                            })
                    }
                    is GetRatingInfoState.ShowError -> {

                    }
                }
            }
        })

        tvSend.setOnClickListener {
            showDrawer(false)
            handler.postDelayed(
                {
                    wallet?.let {
                        navigator.navigateToSendScreen(
                            currentFragment, it
                        )
                    }
                }, 250
            )
        }


        handler.postDelayed({
            mainViewModel.getRatingInfo()
        }, 5000)

//        OneSignal.idsAvailable { _, _ ->
//            mainViewModel.updatePushToken(
//                OneSignal.getPermissionSubscriptionState().subscriptionStatus.userId,
//                OneSignal.getPermissionSubscriptionState().subscriptionStatus.pushToken
//            )
//        }

        if (isPromotion || isOther || isGeneralNotification) {
            notification?.let {
                dialogHelper.showPromotionDialog(it) {
                    openUrl(it)
                }
            }
        }

        notification?.let {
            if (it.id > 0) {
                mainViewModel.readNotification(it)
            }
        }

        alert?.let {
            if (it.notificationId > 0) {
                mainViewModel.readNotification(Notification(it))
            }
        }

        limitOrder?.let {
            if (it.notificationId > 0) {
                mainViewModel.readNotification(Notification(it))
            }
        }

        mainViewModel.getDataTransferInfo()
        mainViewModel.getDataTransferCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        val userInfo = state.userInfo
                        if (userInfo != null) {
                            val userId = state.userInfo.uid
                            if (userId > 0 && state.userInfo.transferPermission.equals(
                                    getString(R.string.undecided_transfer_data),
                                    true
                                )
                            ) {

                                dialogHelper.showDataTransformationDialog(
                                    {
                                        mainViewModel.transfer(
                                            getString(R.string.transfer_action_yes),
                                            userInfo
                                        )

                                        firebaseAnalytics.logEvent(
                                            USER_CLICK_DATA_TRANSFER_YES,
                                            Bundle().createEvent(MainActivity::class.java.simpleName)
                                        )

                                    }, {

                                        firebaseAnalytics.logEvent(
                                            USER_CLICK_DATA_TRANSFER_NO,
                                            Bundle().createEvent(MainActivity::class.java.simpleName)
                                        )
                                        dialogHelper.showConfirmDataTransfer({
                                            firebaseAnalytics.logEvent(
                                                USER_CLICK_DATA_TRANSFER_NO_CONTINUE,
                                                Bundle().createEvent(MainActivity::class.java.simpleName)
                                            )
                                            mainViewModel.transfer(
                                                getString(R.string.transfer_action_no),
                                                userInfo
                                            )
                                        }, {
                                            it.show()
                                        }, {
                                            it.show()
                                        })

                                    }, {

                                        firebaseAnalytics.logEvent(
                                            USER_CLICK_DATA_TRANSFER_DISMISS,
                                            Bundle().createEvent(MainActivity::class.java.simpleName)
                                        )
                                        if (state.userInfo.forceLogout) {
                                            firebaseAnalytics.logEvent(
                                                USER_TRANSFER_DATA_FORCE_LOGOUT,
                                                Bundle().createEvent(MainActivity::class.java.simpleName)
                                            )
                                            mainViewModel.logout()
                                        }
                                    }
                                )
                            }
                        }
                    }
                    is UserInfoState.ShowError -> {

                    }
                }
            }
        })

        mainViewModel.dataTransferCallback.observe(this, Observer {
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

    private fun updateLoginStatus() {
        currentFragment?.childFragmentManager?.fragments?.forEach {
            when (it) {
                is LoginState -> it.getLoginStatus()
            }
        }
    }

    private fun onTransferDataCompleted(userInfo: UserInfo) {
        if (userInfo.transferPermission.equals(
                getString(R.string.transfer_action_no),
                true
            )
            && userInfo.forceLogout
        ) {
            firebaseAnalytics.logEvent(
                USER_TRANSFER_DATA_FORCE_LOGOUT,
                Bundle().createEvent(MainActivity::class.java.simpleName)
            )
            mainViewModel.logout()
        }

        if (userInfo.transferPermission.equals(getString(R.string.transfer_action_yes), true)) {
            showAlert(
                getString(R.string.transfer_data_success),
                R.drawable.ic_check
            )
        }
    }

    override fun onUpdateNeeded(title: String?, message: String, updateUrl: String?) {

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                R.string.ok
            ) { _, _ ->
                redirectStore(updateUrl)

            }.create()
        dialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UserStatusChangeEvent) {
        _isLoggedIn = false
        mainViewModel.getNotifications()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun redirectStore(updateUrl: String?) {
        updateUrl?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    fun navigateToSendScreen() {
        handler.postDelayed(
            {
                wallet?.let {
                    navigator.navigateToSendScreen(
                        getCurrentFragment(), it
                    )
                }
            }, 250
        )
    }

    fun showDialog(type: Int, transaction: Transaction) {
        currentDialogFragment?.dismiss()
        val fragment = AlertDialogFragment.newInstance(type, transaction, wallet?.address)
        fragment.setCallback(this)
        fragment.show(supportFragmentManager, "dialog_broadcasted")
        currentDialogFragment = fragment
    }

    private fun setPendingTransaction(numOfPendingTransaction: Int) {
        hasPendingTransaction = numOfPendingTransaction > 0
        tvPendingTransaction.visibility =
            if (numOfPendingTransaction > 0) View.VISIBLE else View.INVISIBLE
        tvPendingTransaction.text = numOfPendingTransaction.toString()
        showPendingIndicator()
    }

    private fun setPendingNotification(numOfPendingNotification: Int) {
        hasPendingNotification = numOfPendingNotification > 0
        tvPendingNotification.visibility =
            if (numOfPendingNotification > 0 && isLoggedIn) View.VISIBLE else View.INVISIBLE
        tvPendingNotification.text = numOfPendingNotification.toString()
        showPendingIndicator()
    }

    private fun showPendingIndicator() {
        if (currentFragment is PendingTransactionNotification) {
            (currentFragment as PendingTransactionNotification).apply {
                showPendingTxNotification(
                    hasPendingTransaction == true
                )
                showUnReadNotification(showPendingNotification)
            }
        }
    }

    override fun onSwap() {
        this.moveToTab(MainPagerAdapter.SWAP)
    }

    override fun onTransfer() {
        val lastAddedFragment =
            getCurrentFragment()?.childFragmentManager?.fragments?.lastOrNull()
        if (lastAddedFragment !is SendFragment) {
            if (lastAddedFragment is WalletConnectFragment) {
                getCurrentFragment()?.childFragmentManager?.popBackStack()
            }
            this.navigateToSendScreen()
        }
    }

    private val isWalletConnectOpen: Boolean
        get() {
            val lastAddedFragment =
                getCurrentFragment()?.childFragmentManager?.fragments?.lastOrNull()
            return lastAddedFragment is WalletConnectFragment
        }

    fun getCurrentFragment(): Fragment? {
        return currentFragment
    }

    val profileFragment: Fragment?
        get() = adapter?.getRegisteredFragment(MainPagerAdapter.PROFILE)

    fun showDrawer(show: Boolean) {
        if (show) {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        } else {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    override fun onBackPressed() {
        if (currentFragment != null && currentFragment!!.childFragmentManager.backStackEntryCount > 0) {

            currentFragment?.childFragmentManager?.fragments?.forEach {
                when (it) {
                    is PassportFragment -> {
                        it.onBackPress()
                        return
                    }
                    is SubmitFragment -> {
                        it.onBackPress()
                        return
                    }
                    is OrderConfirmFragment -> {
                        it.onBackPress()
                    }
                    is WalletConnectFragment -> {
                        it.onBackPress()
                    }
                }
            }

            if (currentFragment is LimitOrderFragment) {
                (currentFragment as LimitOrderFragment).onRefresh()
            } else if (currentFragment is SwapFragment) {
                (currentFragment as SwapFragment).getSwap()
            } else if (currentFragment is LimitOrderV2Fragment) {
                (currentFragment as LimitOrderV2Fragment).refresh()
            }
            currentFragment!!.childFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun moveToTab(tab: Int, fromLimitOrder: Boolean = false) {
        this.fromLimitOrder = fromLimitOrder
        if (tab == MainPagerAdapter.SWAP) {
            clearFragmentBackStack()
        }
        handler.post {
            bottomNavigation.currentItem = tab
            listener?.onPageSelected(tab)

        }
    }

    private fun clearFragmentBackStack() {
        val fm = currentFragment?.childFragmentManager
        if (fm != null) {
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val allFragments = supportFragmentManager.fragments
        allFragments.forEach {
            if (it is ProfileFragment) {
                it.onActivityResult(requestCode, resultCode, data)
            }
        }

        currentFragment?.childFragmentManager?.fragments?.forEach {
            if (it is PersonalInfoFragment || it is PassportFragment) {
                it.onActivityResult(requestCode, resultCode, data)
            }
        }

        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null && scanResult.contents != null) {
            wallet?.address?.let {

                navigator.navigateToWalletConnectScreen(
                    currentFragment,
                    wallet,
                    scanResult.contents
                )

            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    companion object {
        private const val ALERT_PARAM = "alert_param"
        private const val LIMIT_ORDER_PARAM = "limit_order_param"
        private const val IS_PROMO_CODE_PARAM = "promo_code_param"
        private const val NOTIFICATION_PARAM = "notification_param"
        fun newIntent(
            context: Context,
            alert: NotificationAlert? = null,
            limitOrderNotification: NotificationLimitOrder? = null,
            isPromoCode: Boolean = false,
            notification: Notification? = null
        ) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(ALERT_PARAM, alert)
                putExtra(LIMIT_ORDER_PARAM, limitOrderNotification)
                putExtra(IS_PROMO_CODE_PARAM, isPromoCode)
                putExtra(NOTIFICATION_PARAM, notification)
            }
    }
}
