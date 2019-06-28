package com.kyberswap.android.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
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
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.main.balance.GetPendingTransactionState
import com.kyberswap.android.presentation.main.balance.WalletAdapter
import com.kyberswap.android.presentation.main.limitorder.LimitOrderFragment
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.profile.kyc.PassportFragment
import com.kyberswap.android.presentation.main.profile.kyc.PersonalInfoFragment
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer.*
import kotlinx.android.synthetic.main.layout_drawer.view.*
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
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

    private var user: UserInfo? = null

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var currentFragment: Fragment? = null

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

    var adapter: MainPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WalletManager.storage = this
        WalletManager.scanWallets()
        user = intent.getParcelableExtra(USER_PARAM)
        mainViewModel.getWalletStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        wallet = state.wallet
            
                    is GetWalletState.ShowError -> {
                        Toast.makeText(
                            this,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)
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
            binding.vpNavigation.setCurrentItem(position, true)
            return@setOnTabSelectedListener true


        val adapter = MainPagerAdapter(
            supportFragmentManager,
            wallet,
            user
        )

        binding.vpNavigation.adapter = adapter
        binding.vpNavigation.offscreenPageLimit = 4
        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

    

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

    

            override fun onPageSelected(position: Int) {
                currentFragment = adapter.getRegisteredFragment(position)
                if (currentFragment is LimitOrderFragment) {
                    (currentFragment as LimitOrderFragment).getLoginStatus()
        
    


        binding.vpNavigation.addOnPageChangeListener(listener)

        binding.vpNavigation.post {
            listener.onPageSelected(MainPagerAdapter.SWAP)

        bottomNavigation.currentItem = MainPagerAdapter.SWAP
        binding.vpNavigation.currentItem = MainPagerAdapter.SWAP

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
                        mainViewModel.updateSelectedWallet(it)
            , 250
                )
    
        binding.navView.rvWallet.adapter = walletAdapter

        mainViewModel.getWallets()
        mainViewModel.getAllWalletStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAllWalletState.Success -> {
                        val selectedWallet = state.wallets.find { it.isSelected }
                        if (wallet?.address != selectedWallet?.address) {
                            walletAdapter.submitList(listOf())
                            walletAdapter.submitList(state.wallets)
                
            
                    is GetAllWalletState.ShowError -> {
                        navigator.navigateToLandingPage()
            
        
    
)

        imgClose.setOnClickListener {
            showDrawer(false)


        tvTransaction.setOnClickListener {

            wallet?.let {
                navigator.navigateToTransactionScreen(
                    currentFragment,
                    it
                )
    
            showDrawer(false)


        wallet?.let {
            mainViewModel.getPendingTransaction(it)


        mainViewModel.getPendingTransactionStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetPendingTransactionState.Success -> {
                        val txList = state.transactions.filter {
                            it.blockNumber.isNotEmpty()
                

                        txList.forEach {
                            showAlert(String.format(getString(R.string.transaction_mined), it.hash))
                

                        val pending = state.transactions.filter { it.blockNumber.isEmpty() }
                        pendingTransactions.clear()
                        pendingTransactions.addAll(pending)
                        setPendingTransaction(pending.size)
            
                    is GetPendingTransactionState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        imgAdd.setOnClickListener {
            showDrawer(false)
            dialogHelper.showBottomSheetDialog(
                {
                    dialogHelper.showConfirmation {
                        mainViewModel.createWallet()
            

        ,
                {
                    navigator.navigateToImportWalletPage()

        
            )


        mainViewModel.createWalletCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CreateWalletState.Loading)
                when (state) {
                    is CreateWalletState.Success -> {
                        showAlert(getString(R.string.create_wallet_success)) {

                

            
                    is CreateWalletState.ShowError -> {
                        Toast.makeText(
                            this,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)

        tvSend.setOnClickListener {
            showDrawer(false)
            wallet?.let {
                navigator.navigateToSendScreen(
                    currentFragment, it
                )
    



    }

    private fun setPendingTransaction(numOfPendingTransaction: Int) {
        tvPendingTransaction.visibility =
            if (numOfPendingTransaction > 0) View.VISIBLE else View.INVISIBLE
        tvPendingTransaction.text = numOfPendingTransaction.toString()
    }

    fun getCurrentFragment(): Fragment? {
        return currentFragment
    }

    fun showDrawer(show: Boolean) {
        if (show) {
            binding.drawerLayout.openDrawer(GravityCompat.END)
 else {
            binding.drawerLayout.closeDrawer(GravityCompat.END)

    }

    override fun onBackPressed() {
        if (currentFragment != null && currentFragment!!.childFragmentManager.backStackEntryCount > 0) {
            currentFragment!!.childFragmentManager.popBackStack()
 else {
            super.onBackPressed()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val allFragments = supportFragmentManager.fragments
        allFragments.forEach {
            if (it is ProfileFragment) {
                it.onActivityResult(requestCode, resultCode, data)
    


        currentFragment?.childFragmentManager?.fragments?.forEach {
            if (it is PersonalInfoFragment || it is PassportFragment) {
                it.onActivityResult(requestCode, resultCode, data)
    

    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val USER_PARAM = "user_param"
        fun newIntent(context: Context, wallet: Wallet?, user: UserInfo?) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
                putExtra(USER_PARAM, user)
    
    }
}
