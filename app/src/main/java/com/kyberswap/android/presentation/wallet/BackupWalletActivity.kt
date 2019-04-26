package com.kyberswap.android.presentation.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import kotlinx.android.synthetic.main.activity_backup_wallet.*
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"
private const val WALLET_PARAM = "wallet_param"

class BackupWalletActivity : BaseActivity() {
    @Inject
    lateinit var navigator: Navigator
    private val binding by lazy {
        DataBindingUtil.setContentView<com.kyberswap.android.databinding.ActivityBackupWalletBinding>(
            this,
            R.layout.activity_backup_wallet
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val words = intent.getParcelableArrayListExtra<Word>(ARG_PARAM)
        val wallet = intent.getParcelableExtra<Wallet>(WALLET_PARAM)

        binding.title = getString(R.string.backup_wallet_title)
        val adapter = BackupWalletPagerAdapter(supportFragmentManager, words)
        binding.vpBackupWallet.adapter = adapter
        btnNext.setOnClickListener {
            if (vpBackupWallet.currentItem == adapter.count - 1) {
                navigator.navigateVerifyBackupWordPage(words, wallet)
            } else {
                vpBackupWallet.currentItem += 1
            }
        }
    }

    companion object {
        fun newIntent(context: Context, words: List<Word>, wallet: Wallet) =
            Intent(context, BackupWalletActivity::class.java).apply {
                putParcelableArrayListExtra(ARG_PARAM, ArrayList(words))
                putExtra(WALLET_PARAM, wallet)
            }
    }
}
