package com.kyberswap.android.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.main.balance.WalletAdapter
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

    private var currentFragment: Fragment? = null

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WalletManager.storage = this

        wallet = intent.getParcelableExtra(WALLET_PARAM)

        binding.viewModel = mainViewModel
        val adapter = MainPagerAdapter(
            supportFragmentManager,
            wallet
        )
        binding.vpNavigation.offscreenPageLimit = 4
        binding.vpNavigation.adapter = adapter

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
        bottomNavigation.setOnTabSelectedListener { position, wasSelected ->
            binding.vpNavigation.setCurrentItem(position, true)
            return@setOnTabSelectedListener true


        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

    

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

    

            override fun onPageSelected(position: Int) {
                currentFragment = adapter.getRegisteredFragment(position)
    



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
            WalletAdapter(appExecutors)
        binding.navView.rvWallet.adapter = walletAdapter

        mainViewModel.getWallets()
        mainViewModel.getAllWalletStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAllWalletState.Success -> {
                        walletAdapter.submitList(state.wallets)
            
                    is GetAllWalletState.ShowError -> {
                        navigator.navigateToLandingPage()
            
        
    
)

        imgClose.setOnClickListener {
            showDrawer(false)


        tvTransaction.setOnClickListener {
            navigator.navigateToTransactionScreen(
                currentFragment,
                wallet
            )
            showDrawer(false)

    }

    fun getCurrentFragment(): Fragment? {
        return currentFragment
    }

    fun showDrawer(isShown: Boolean) {
        if (isShown) {
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

    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newIntent(context: Context, wallet: Wallet?) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(WALLET_PARAM, wallet)
    
    }
}
