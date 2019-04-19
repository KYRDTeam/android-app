package com.kyberswap.android.util.di.module

import android.support.v7.app.AppCompatActivity
import com.kyberswap.android.presentation.wallet.VerifyBackupWordActivity
import dagger.Binds
import dagger.Module

@Module
interface VerifyBackupWordActivityModule {

    @Binds
    fun bindsAppCompatActivity(verifyBackupWordActivity: VerifyBackupWordActivity): AppCompatActivity

}