package com.kyberswap.android.presentation.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityVerifyBackupWordBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.activity_backup_wallet.btnNext
import kotlinx.android.synthetic.main.activity_verify_backup_word.*
import kotlinx.android.synthetic.main.toolbar_with_back.*
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"
private const val WALLET_PARAM = "wallet_param"

class VerifyBackupWordActivity : BaseActivity() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var numberOfTry = 0

    private lateinit var disposable: CompositeDisposable

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityVerifyBackupWordBinding>(
            this,
            R.layout.activity_verify_backup_word
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val words = intent.getParcelableArrayListExtra<Word>(ARG_PARAM)
        val wallet = intent.getParcelableExtra<Wallet>(WALLET_PARAM)
        binding.title = getString(R.string.test_wallet_title)
        imgBack.setOnClickListener {
            onBackPressed()

        var first = words.random()
        var second: Word
        do {
            second = words.random()
 while (second == first)

        if (first.position > second.position) {
            first = second.also { second = first }

        disposable = CompositeDisposable()
        binding.word1 = first
        binding.word2 = second
        btnNext.setOnClickListener {
            if (first.content == edtFirst.text.trim().toString() &&
                second.content == edtSecond.text.trim().toString()
            ) {
                navigator.navigateToHome(wallet)
     else {
                dialogHelper.showWrongBackup(numberOfTry, {
        , {
        )
                numberOfTry = numberOfTry.inc()
    

        val firstWordObservable = binding.edtFirst.textChanges().skip(1).map {
            it.toString()

        val secondWordObservable = binding.edtSecond.textChanges().skip(1).map {
            it.toString()


        disposable.add(
            Observables.combineLatest(
                firstWordObservable,
                secondWordObservable
            ) { f, s ->
                {
                    f.isNotEmpty() && s.isNotEmpty()
        
    .subscribe {
                binding.btnNext.isEnabled = true
    )
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context, words: List<Word>, wallet: Wallet) =
            Intent(context, VerifyBackupWordActivity::class.java).apply {
                putParcelableArrayListExtra(ARG_PARAM, ArrayList(words))
                putExtra(WALLET_PARAM, wallet)
    
    }
}
