package com.kyberswap.android.presentation.splash

import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivitySplashBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import org.consenlabs.tokencore.wallet.Identity
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import java.io.File
import javax.inject.Inject

class SplashActivity : BaseActivity(), KeystoreStorage {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivitySplashBinding>(this, R.layout.activity_splash)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewModel = viewModel
        WalletManager.storage = this
        WalletManager.scanWallets()
        Handler().postDelayed({
            val identity = Identity.getCurrentIdentity()
            if (identity != null && identity.wallets[0] != null) {
                navigator.navigateToHome()
     else {
                navigator.navigateToLandingPage()
    
, 2000)
    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }
}
