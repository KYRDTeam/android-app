package com.kyberswap.android.util.di.module

import android.arch.lifecycle.ViewModel
import com.kyberswap.android.presentation.home.top.FeatureFragment
import com.kyberswap.android.presentation.home.top.FeatureViewModel
import com.kyberswap.android.presentation.home.top.TopFragment
import com.kyberswap.android.presentation.home.top.TopViewModel
import com.kyberswap.android.util.di.ViewModelKey
import com.kyberswap.android.util.di.scope.ChildFragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface HomeFragmentModule {

    @ChildFragmentScoped
    @ContributesAndroidInjector
    fun contributeTopFragment(): TopFragment

    @ChildFragmentScoped
    @ContributesAndroidInjector
    fun contributeFeatureFragment(): FeatureFragment

    @Binds @IntoMap
    @ViewModelKey(TopViewModel::class)
    fun bindTopViewModel(
        topViewModel: TopViewModel
    ): ViewModel

    @Binds @IntoMap
    @ViewModelKey(FeatureViewModel::class)
    fun bindFeatureViewModel(
        featureViewModel: FeatureViewModel
    ): ViewModel
}
