package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.wallet.*
import com.kyberswap.android.util.di.ViewModelKey
import com.kyberswap.android.util.di.scope.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface ImportWalletActivityModule {

    @Binds
    fun bindsAppCompatActivity(importWalletActivity: ImportWalletActivity): AppCompatActivity

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeImportJsonFragment(): ImportJsonFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeImportPrivateKeyFragment(): ImportPrivateKeyFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeImportSeedFragment(): ImportSeedFragment

    @Binds
    @IntoMap
    @ViewModelKey(ImportJsonViewModel::class)
    fun bindImportJsonViewModel(
        importJsonViewModel: ImportJsonViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ImportPrivateKeyViewModel::class)
    fun bindImportPrivateKeyViewModel(
        importImportPrivateKeyViewModel: ImportPrivateKeyViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ImportSeedViewModel::class)
    fun bindImportSeedViewModel(
        importImportSeedViewModel: ImportSeedViewModel
    ): ViewModel
}