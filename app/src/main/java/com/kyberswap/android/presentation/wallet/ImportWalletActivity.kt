package com.kyberswap.android.presentation.wallet

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.view.View
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityImportWalletBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import org.consenlabs.tokencore.wallet.KeystoreStorage
import org.consenlabs.tokencore.wallet.WalletManager
import java.io.File
import javax.inject.Inject


class ImportWalletActivity : BaseActivity(), KeystoreStorage {
    override fun getKeystoreDir(): File {
        return this.filesDir
    }

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    private lateinit var options: List<View>

    var currentSelectedView: View? = null

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityImportWalletBinding>(
            this,
            R.layout.activity_import_wallet
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WalletManager.storage = this
        WalletManager.scanWallets()

        binding.title = getString(R.string.import_wallet_title)

        options = listOf(binding.vJson, binding.vPrivateKey, binding.vSeed)

        binding.vJson.isSelected = true
        currentSelectedView = binding.vJson

        options.forEachIndexed { index, view ->
            view.setOnClickListener {
                setSelectedOption(it)
                binding.vpImportOption.currentItem = index

                when {
                    view.id == R.id.vJson -> {

            
                    view.id == R.id.vPrivateKey -> {

            
                    else -> {

            
        
    


        val adapter = ImportWalletPagerAdapter(supportFragmentManager)
        binding.vpImportOption.adapter = adapter
        binding.vpImportOption.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
    

            override fun onPageSelected(position: Int) {
                setSelectedOption(options[position])
    
)

    }

    private fun setSelectedOption(view: View) {
        currentSelectedView?.isSelected = false
        view.isSelected = true
        currentSelectedView = view
    }


    companion object {
        fun newIntent(context: Context) =
            Intent(context, ImportWalletActivity::class.java)
    }

}