package com.kyberswap.android.presentation.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityBackupWalletBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import kotlinx.android.synthetic.main.activity_backup_wallet.*
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"
private const val WALLET_PARAM = "wallet_param"
private const val SETTING_PARAM = "setting_param"

class BackupWalletActivity : BaseActivity() {
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var dialogHelper: DialogHelper

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityBackupWalletBinding>(
            this,
            R.layout.activity_backup_wallet
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val words = intent.getParcelableArrayListExtra<Word>(ARG_PARAM)
        val wallet = intent.getParcelableExtra<Wallet>(WALLET_PARAM)
        val fromSetting = intent.getBooleanExtra(SETTING_PARAM, false)

        binding.title = getString(R.string.backup_wallet_title)
        val adapter = BackupWalletPagerAdapter(supportFragmentManager, words)
        binding.vpBackupWallet.adapter = adapter
        btnNext.setOnClickListener {
            if (vpBackupWallet.currentItem == adapter.count - 1) {
                navigator.navigateVerifyBackupWordPage(words, wallet)
     else {
                vpBackupWallet.currentItem += 1
    


        binding.tvSkip.setOnClickListener {

            dialogHelper.showSkipBackupPhraseDialog({
                if (fromSetting) {
                    onBackPressed()
         else {
                    navigator.navigateToHome()
        
    )



    }

    companion object {
        fun newIntent(context: Context, words: List<Word>, wallet: Wallet, fromSetting: Boolean) =
            Intent(context, BackupWalletActivity::class.java).apply {
                putParcelableArrayListExtra(ARG_PARAM, ArrayList(words))
                putExtra(WALLET_PARAM, wallet)
                putExtra(SETTING_PARAM, fromSetting)
    
    }
}
