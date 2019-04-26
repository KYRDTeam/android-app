package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import com.kyberswap.android.presentation.wallet.BackupWalletActivity
import com.kyberswap.android.presentation.wallet.BackupWalletFragment
import com.kyberswap.android.presentation.wallet.BackupWalletFragmentNext
import com.kyberswap.android.util.di.scope.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface BackupWalletActivityModule {

    @Binds
    fun bindsAppCompatActivity(backupWalletActivity: BackupWalletActivity): AppCompatActivity

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeBackupWalletFragment(): BackupWalletFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeBackupWalletFragmentNext(): BackupWalletFragmentNext
}