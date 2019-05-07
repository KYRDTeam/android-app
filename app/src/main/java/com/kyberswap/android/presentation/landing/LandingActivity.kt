package com.kyberswap.android.presentation.landing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityLandingBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.enable
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
                it.enable(false)
                viewModel.createWallet()
            }
        }

        binding.btnImportWallet.setOnClickListener {
            it.enable(false)
            navigator.navigateToImportWalletPage()
        }

        viewModel.getMnemonicCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetMnemonicState.Loading)
                when (state) {
                    is GetMnemonicState.Success -> {
                        showAlert(getString(R.string.create_wallet_success)) {
                            binding.btnCreateWallet.enable(true)
                            navigator.navigateToBackupWalletPage(state.words, state.wallet)
                        }

                    }
                    is GetMnemonicState.ShowError -> {
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

    override fun onResume() {
        super.onResume()
        if (!binding.btnImportWallet.isEnabled) {
            binding.btnImportWallet.enable(true)
        }
    }


    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, LandingActivity::class.java)
    }
}
