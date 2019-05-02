package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.*
import com.kyberswap.android.util.di.ViewModelKey
import com.kyberswap.android.util.di.scope.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface MainActivityModule {

    @Binds
    fun providesAppCompatActivity(mainActivity: MainActivity): AppCompatActivity

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeBalanceFragment(): BalanceFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeKyberListFragment(): KyberListFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeOtherFragment(): OtherFragment

    @Binds
    @IntoMap
    @ViewModelKey(BalanceViewModel::class)
    fun bindBalanceViewModel(
        mainViewModel: BalanceViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(
        mainViewModel: MainViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(KyberListViewModel::class)
    fun bindKyberListViewModel(
        kyberListViewModel: KyberListViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OtherViewModel::class)
    fun bindOtherViewModel(
        otherViewModel: OtherViewModel
    ): ViewModel
}
