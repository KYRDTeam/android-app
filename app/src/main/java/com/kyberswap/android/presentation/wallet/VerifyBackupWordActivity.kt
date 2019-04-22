package com.kyberswap.android.presentation.wallet

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityVerifyBackupWordBinding
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import kotlinx.android.synthetic.main.activity_backup_wallet.btnNext
import kotlinx.android.synthetic.main.activity_verify_backup_word.*
import javax.inject.Inject


private const val ARG_PARAM = "arg_param"

class VerifyBackupWordActivity : BaseActivity() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var numberOfTry = 0


    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityVerifyBackupWordBinding>(
            this,
            R.layout.activity_verify_backup_word
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val words = intent.getParcelableArrayListExtra<Word>(ARG_PARAM)
        binding.title = getString(R.string.test_wallet_title)
        var first = words.random()
        var second: Word
        do {
            second = words.random()
 while (second == first)

        if (first.position > second.position) {
            first = second.also { second = first }

        binding.word1 = first
        binding.word2 = second
        btnNext.setOnClickListener {
            if (first.content == edtFirst.text.trim().toString() && second.content == edtSecond.text.trim().toString()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.backup_success),
                    Snackbar.LENGTH_LONG
                ).show()
     else {
                dialogHelper.showWrongBackup(numberOfTry, {

        , {

        )
                numberOfTry = numberOfTry.inc()

    

    }


    companion object {
        fun newIntent(context: Context, words: List<Word>) =
            Intent(context, VerifyBackupWordActivity::class.java).apply {
                putParcelableArrayListExtra(ARG_PARAM, ArrayList(words))
    
    }
}
