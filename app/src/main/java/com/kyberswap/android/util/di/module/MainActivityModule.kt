package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainViewModel
import com.kyberswap.android.presentation.main.balance.BalanceFragment
import com.kyberswap.android.presentation.main.balance.BalanceViewModel
import com.kyberswap.android.presentation.main.balance.address.BalanceAddressFragment
import com.kyberswap.android.presentation.main.balance.address.BalanceAddressViewModel
import com.kyberswap.android.presentation.main.balance.chart.ChartFragment
import com.kyberswap.android.presentation.main.balance.chart.ChartViewModel
import com.kyberswap.android.presentation.main.balance.chart.LineChartFragment
import com.kyberswap.android.presentation.main.balance.chart.LineChartViewModel
import com.kyberswap.android.presentation.main.balance.kyberlist.KyberListFragment
import com.kyberswap.android.presentation.main.balance.kyberlist.KyberListViewModel
import com.kyberswap.android.presentation.main.balance.other.OtherFragment
import com.kyberswap.android.presentation.main.balance.other.OtherViewModel
import com.kyberswap.android.presentation.main.balance.send.SendFragment
import com.kyberswap.android.presentation.main.balance.send.SendViewModel
import com.kyberswap.android.presentation.main.profile.*
import com.kyberswap.android.presentation.main.setting.*
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.presentation.main.swap.SwapViewModel
import com.kyberswap.android.presentation.main.swap.TokenSearchFragment
import com.kyberswap.android.presentation.main.swap.TokenSearchViewModel
import com.kyberswap.android.presentation.main.transaction.*
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
    fun contributeProfileFragment(): ProfileFragment

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

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeContactFragment(): ContactFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeTransactionFragment(): TransactionFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeTransactionFilterFragment(): TransactionFilterFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeTransactionStatusFragment(): TransactionStatusFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSwapTransactionFragment(): TransactionDetailSwapFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeReceivedTransactionFragment(): TransactionDetailReceiveFragment


    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSendTransactionFragment(): TransactionDetailSendFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSignUpFragment(): SignUpFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSignUpConfirmFragment(): SignUpConfirmFragment

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

    @Binds
    @IntoMap
    @ViewModelKey(ContactViewModel::class)
    fun bindContactViewModel(
        contactViewModel: ContactViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionViewModel::class)
    fun bindTransactionViewModel(
        transactionViewModel: TransactionViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionStatusViewModel::class)
    fun bindTransactionStatusViewModel(
        transactionStatusViewModel: TransactionStatusViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    fun bindProfileViewModel(
        profileViewModel: ProfileViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    fun bindSignUpViewModel(
        signUpViewModel: SignUpViewModel
    ): ViewModel
}
