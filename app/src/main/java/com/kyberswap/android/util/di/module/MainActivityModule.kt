package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainViewModel
import com.kyberswap.android.presentation.main.balance.*
import com.kyberswap.android.presentation.main.setting.AddContactFragment
import com.kyberswap.android.presentation.main.setting.AddContactViewModel
import com.kyberswap.android.presentation.main.setting.SettingFragment
import com.kyberswap.android.presentation.main.setting.SettingViewModel
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.presentation.main.swap.SwapViewModel
import com.kyberswap.android.presentation.main.swap.TokenSearchFragment
import com.kyberswap.android.presentation.main.swap.TokenSearchViewModel
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

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeBalanceAddressFragment(): BalanceAddressFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeTokenSearchFragment(): TokenSearchFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSwapFragment(): SwapFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSendFragment(): SendFragment


    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeChartFragment(): ChartFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeLineChartFragment(): LineChartFragment


    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSettingFragment(): SettingFragment


    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeAddContactFragment(): AddContactFragment


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

    @Binds
    @IntoMap
    @ViewModelKey(BalanceAddressViewModel::class)
    fun bindBalanceAddressViewModel(
        otherViewModel: BalanceAddressViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SwapViewModel::class)
    fun bindSwapViewModel(
        swapViewModel: SwapViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SendViewModel::class)
    fun bindSendViewModel(
        sendViewModel: SendViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TokenSearchViewModel::class)
    fun bindTokenSearchViewModel(
        tokenSearchViewModel: TokenSearchViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChartViewModel::class)
    fun bindChartViewModel(
        chartViewModel: ChartViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LineChartViewModel::class)
    fun bindLineChartViewModel(
        chartViewModel: LineChartViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    fun bindSettingViewModel(
        settingViewModel: SettingViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddContactViewModel::class)
    fun bindAddContactViewModel(
        addContactViewModel: AddContactViewModel
    ): ViewModel
}
