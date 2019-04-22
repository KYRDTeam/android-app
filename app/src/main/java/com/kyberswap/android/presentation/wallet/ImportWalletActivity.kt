package com.kyberswap.android.presentation.wallet

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityImportWalletBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.common.ImportButtonView
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import timber.log.Timber
import javax.inject.Inject

class ImportWalletActivity : BaseActivity() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    var currentSelectedView: ImportButtonView? = null

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityImportWalletBinding>(
            this,
            R.layout.activity_import_wallet
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.title = getString(R.string.import_wallet_title)

        val options = listOf(binding.vJson, binding.vPrivateKey, binding.vSeed)

        binding.vJson.isSelected = true
        currentSelectedView = binding.vJson

        binding.vJson.setOnClickListener {
            Timber.e("This is a log")


        options.forEach { view ->
            view.setOnClickListener {
                currentSelectedView?.isSelected = false
                view.isSelected = true
                when {
                    view.id == R.id.vJson -> {

            
                    view.id == R.id.vPrivateKey -> {

            
                    else -> {

            
        
                currentSelectedView = view
    


    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, ImportWalletActivity::class.java)
    }

}