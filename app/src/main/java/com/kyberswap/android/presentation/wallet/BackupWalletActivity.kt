package com.kyberswap.android.presentation.wallet

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import kotlinx.android.synthetic.main.activity_backup_wallet.*
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"

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
        binding.title = getString(R.string.backup_wallet_title)
        val adapter = BackupWalletPagerAdapter(supportFragmentManager, words)
        binding.vpBackupWallet.adapter = adapter
        btnNext.setOnClickListener {
            if (vpBackupWallet.currentItem == adapter.count - 1) {
                navigator.navigateVerifyBackupWordPage(words)
     else {
                vpBackupWallet.currentItem += 1
    


    }

    companion object {
        fun newIntent(context: Context, words: List<Word>) =
            Intent(context, BackupWalletActivity::class.java).apply {
                putParcelableArrayListExtra(ARG_PARAM, ArrayList(words))
    
    }
}
