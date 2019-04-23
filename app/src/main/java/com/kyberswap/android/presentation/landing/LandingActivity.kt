package com.kyberswap.android.presentation.landing

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.Toast
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityLandingBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import java.io.File
import javax.inject.Inject

class LandingActivity : BaseActivity(), KeystoreStorage {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityLandingBinding>(this, R.layout.activity_landing)
    }

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LandingActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vpLanding.adapter = LandingPagerAdapter(supportFragmentManager)
        binding.indicator.setViewPager(binding.vpLanding)
        WalletManager.storage = this
        WalletManager.scanWallets()

        binding.btnCreateWallet.setOnClickListener {
            dialogHelper.showConfirmation {
                viewModel.createWallet()
            }
        }

        binding.btnImportWallet.setOnClickListener {
            navigator.navigateToImportWalletPage()
        }

        viewModel.createWalletCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CreateWalletState.Loading)
                when (state) {
                    is CreateWalletState.Success -> {
                        navigator.navigateToBackupWalletPage(state.words)
                    }
                    is CreateWalletState.ShowError -> {
                        Toast.makeText(
                            this,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        })

    }

    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, LandingActivity::class.java)
    }
}
