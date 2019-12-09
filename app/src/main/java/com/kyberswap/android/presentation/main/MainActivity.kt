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
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityMainBinding
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.domain.model.Transaction
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
import com.kyberswap.android.presentation.main.limitorder.OrderConfirmFragment
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.profile.kyc.PassportFragment
import com.kyberswap.android.presentation.main.profile.kyc.PersonalInfoFragment
import com.kyberswap.android.presentation.main.profile.kyc.SubmitFragment
import com.kyberswap.android.presentation.main.setting.SettingFragment
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.presentation.main.walletconnect.WalletConnectFragment
import com.kyberswap.android.presentation.wallet.UpdateWalletState
import com.kyberswap.android.util.CLICK_WALLET_CONNECT_EVENT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.isNetworkAvailable
import com.kyberswap.android.util.ext.toLongSafe
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer.*
import kotlinx.android.synthetic.main.layout_drawer.view.*
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import org.greenrobot.eventbus.EventBus
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

    private var isPromoCode: Boolean = false

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var currentFragment: Fragment? = null

    private var hasPendingTransaction: Boolean? = false

    private var listener: ViewPager.OnPageChangeListener? = null

    var fromLimitOrder: Boolean = false

    private var currentDialogFragment: DialogFragment? = null

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val walletConnect by lazy {
        listOf(binding.drawerLayout.tvWalletConnect, binding.drawerLayout.imgWalletConnect)
    }

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
            limitOrder
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
                showPendingTransaction()
                when (currentFragment) {
                    is BalanceFragment -> {
                        (currentFragment as BalanceFragment).scrollToTop()
                    }
                    is LimitOrderFragment -> {
                        with((currentFragment as LimitOrderFragment)) {
                            getLimitOrder()
                            getLoginStatus()
                            checkEligibleAddress()
                        }

                        currentFragment?.childFragmentManager?.fragments?.forEach {
                            when (it) {
                                is LoginState -> it.getLoginStatus()
                            }
                        }
                    }

                    is SwapFragment -> {
                        with((currentFragment as SwapFragment)) {
                            getSwap()
                            getKyberEnable()
                        }
                    }
                    is SettingFragment -> {
                        (currentFragment as SettingFragment).getLoginStatus()
                        currentFragment?.childFragmentManager?.fragments?.forEach {
                            when (it) {
                                is LoginState -> it.getLoginStatus()
                            }
                        }
                    }
                }
            }
        }

        listener?.let {
            binding.vpNavigation.addOnPageChangeListener(it)
        }

        val initial = if (limitOrder != null) {
            MainPagerAdapter.LIMIT_ORDER
        } else if (alert != null || isPromoCode) {
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

        mainViewModel.getPendingTransactionStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPendingTransactionState.Success -> {
                        val txList = state.transactions.filter {
                            it.blockNumber.isNotEmpty() && it.blockNumber.toLongSafe() != Transaction.DEFAULT_DROPPED_BLOCK_NUMBER
                        }

                        txList.forEach { transaction ->
                            showDialog(
                                AlertDialogFragment.DIALOG_TYPE_DONE,
                                transaction
                            )
                        }

                        val pending = state.transactions.filter { it.blockNumber.isEmpty() }

                        if (pending.isEmpty()) {
                            if (currentDialogFragment is AlertDialogFragment) {
                                if (!(currentDialogFragment as AlertDialogFragment).isDone) {
                                    currentDialogFragment?.dismissAllowingStateLoss()
                                }
                            }
                        }

                        pendingTransactions.clear()
                        pendingTransactions.addAll(pending)
                        setPendingTransaction(pending.size)
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

        OneSignal.idsAvailable { _, _ ->
            mainViewModel.updatePushToken(
                OneSignal.getPermissionSubscriptionState().subscriptionStatus.userId,
                OneSignal.getPermissionSubscriptionState().subscriptionStatus.pushToken
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
        showPendingTransaction()
    }

    private fun showPendingTransaction() {
        if (currentFragment is PendingTransactionNotification) {
            (currentFragment as PendingTransactionNotification).showNotification(
                hasPendingTransaction == true
            )
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
                if (it is PassportFragment) {
                    it.onBackPress()
                    return
                } else if (it is SubmitFragment) {
                    it.onBackPress()
                    return
                } else if (it is OrderConfirmFragment) {
                    it.onBackPress()
                } else if (it is WalletConnectFragment) {
                    it.onBackPress()
                }
            }

            if (currentFragment is LimitOrderFragment) {
                (currentFragment as LimitOrderFragment).onRefresh()
            } else if (currentFragment is SwapFragment) {
                (currentFragment as SwapFragment).getSwap()
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
        fun newIntent(
            context: Context,
            alert: NotificationAlert? = null,
            limitOrderNotification: NotificationLimitOrder? = null,
            isPromoCode: Boolean = false
        ) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(ALERT_PARAM, alert)
                putExtra(LIMIT_ORDER_PARAM, limitOrderNotification)
                putExtra(IS_PROMO_CODE_PARAM, isPromoCode)
            }
    }
}
