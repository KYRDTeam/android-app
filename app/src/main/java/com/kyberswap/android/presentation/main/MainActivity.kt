package com.kyberswap.android.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityMainBinding
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletChangeEvent
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.common.LoginState
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.BalanceFragment
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.main.balance.GetPendingTransactionState
import com.kyberswap.android.presentation.main.balance.WalletAdapter
import com.kyberswap.android.presentation.main.limitorder.LimitOrderFragment
import com.kyberswap.android.presentation.main.limitorder.OrderConfirmFragment
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.profile.kyc.PassportFragment
import com.kyberswap.android.presentation.main.profile.kyc.PersonalInfoFragment
import com.kyberswap.android.presentation.main.profile.kyc.SubmitFragment
import com.kyberswap.android.presentation.main.setting.SettingFragment
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.presentation.wallet.UpdateWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer.*
import kotlinx.android.synthetic.main.layout_drawer.view.*
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject


class MainActivity : BaseActivity(), KeystoreStorage {
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var alert: NotificationAlert? = null

    private var limitOrder: NotificationLimitOrder? = null

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var currentFragment: Fragment? = null

    private var hasPendingTransaction: Boolean? = false

    private var listener: ViewPager.OnPageChangeListener? = null

    var fromLimitOrder: Boolean = false

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private val handler by lazy {
        Handler()
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    val pendingTransactions = mutableListOf<Transaction>()

    private var adapter: MainPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WalletManager.storage = this
        WalletManager.scanWallets()

        alert = intent.getParcelableExtra(ALERT_PARAM)
        limitOrder = intent.getParcelableExtra(LIMIT_ORDER_PARAM)

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
//                        (currentFragment as BalanceFragment).getSelectedWallet()
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
        } else {
            MainPagerAdapter.BALANCE
        }

        binding.vpNavigation.post {
            listener?.onPageSelected(initial)
        }
//        bottomNavigation.currentItem = initial
//        binding.vpNavigation.currentItem = initial

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
                                    }, 2000

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
                            it.blockNumber.isNotEmpty()
                        }

                        txList.forEach { transaction ->
                            val title: String
                            val message: String

                            val type = when {
                                transaction.from == wallet?.address -> Transaction.TransactionType.SEND
                                transaction.to == wallet?.address -> Transaction.TransactionType.RECEIVED
                                else -> transaction.type
                            }

                            when (type) {
                                Transaction.TransactionType.SEND -> {
                                    if (transaction.isTransactionFail) {
                                        title = getString(R.string.title_fail)
                                        message = String.format(
                                            getString(R.string.notification_fail_sent),
                                            transaction.displayValue,
                                            transaction.to
                                        )
                                    } else {
                                        title = getString(R.string.title_success)
                                        message = String.format(
                                            getString(R.string.notification_success_sent),
                                            transaction.displayValue,
                                            transaction.to
                                        )
                                    }
                                }
                                Transaction.TransactionType.SWAP -> {
                                    if (transaction.isTransactionFail) {
                                        title = getString(R.string.title_fail)
                                        message = String.format(
                                            getString(R.string.notification_fail_swap),
                                            transaction.displaySource, transaction.displayDest
                                        )
                                    } else {
                                        title = getString(R.string.title_success)
                                        message = String.format(
                                            getString(R.string.notification_success_swap),
                                            transaction.displaySource, transaction.displayDest
                                        )
                                    }
                                }
                                Transaction.TransactionType.RECEIVED -> {
                                    title = ""
                                    message = ""
                                }
                            }

                            if (title.isNotEmpty() && message.isNotEmpty()) {
                                showAlertWithoutIcon(
                                    title = title, message = message
                                )
                            }
                        }

                        val pending = state.transactions.filter { it.blockNumber.isEmpty() }
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
        handler.post {
            bottomNavigation.currentItem = tab
            listener?.onPageSelected(tab)

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
        fun newIntent(
            context: Context,
            alert: NotificationAlert? = null,
            limitOrderNotification: NotificationLimitOrder? = null
        ) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(ALERT_PARAM, alert)
                putExtra(LIMIT_ORDER_PARAM, limitOrderNotification)
            }
    }
}
