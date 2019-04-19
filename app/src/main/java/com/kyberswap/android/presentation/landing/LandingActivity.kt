package com.kyberswap.android.presentation.landing

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityLandingBinding
import com.kyberswap.android.presentation.base.BaseActivity
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import java.io.File

class LandingActivity : BaseActivity(), KeystoreStorage {
    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityLandingBinding>(this, R.layout.activity_landing)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vpLanding.adapter = LandingPagerAdapter(supportFragmentManager)
        binding.indicator.setViewPager(binding.vpLanding)
        WalletManager.storage = this
        WalletManager.scanWallets()

    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, LandingActivity::class.java)
    }
}
