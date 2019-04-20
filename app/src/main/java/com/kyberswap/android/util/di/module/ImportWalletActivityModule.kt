package com.kyberswap.android.util.di.module

import android.support.v7.app.AppCompatActivity
import com.kyberswap.android.presentation.wallet.ImportWalletActivity
import dagger.Binds
import dagger.Module

@Module
interface ImportWalletActivityModule {

    @Binds
    fun bindsAppCompatActivity(importWalletActivity: ImportWalletActivity): AppCompatActivity

}