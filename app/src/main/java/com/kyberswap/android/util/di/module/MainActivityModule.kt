package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainViewModel
import com.kyberswap.android.presentation.main.alert.AlertMethodFragment
import com.kyberswap.android.presentation.main.alert.AlertMethodViewModel
import com.kyberswap.android.presentation.main.alert.LeaderBoardFragment
import com.kyberswap.android.presentation.main.alert.LeaderBoardViewModel
import com.kyberswap.android.presentation.main.alert.ManageAlertFragment
import com.kyberswap.android.presentation.main.alert.MangeAlertViewModel
import com.kyberswap.android.presentation.main.alert.PriceAlertFragment
import com.kyberswap.android.presentation.main.alert.PriceAlertTokenSearchFragment
import com.kyberswap.android.presentation.main.alert.PriceAlertTokenSearchViewModel
import com.kyberswap.android.presentation.main.alert.PriceAlertViewModel
import com.kyberswap.android.presentation.main.balance.BalanceFragment
import com.kyberswap.android.presentation.main.balance.BalanceViewModel
import com.kyberswap.android.presentation.main.balance.address.BalanceAddressFragment
import com.kyberswap.android.presentation.main.balance.address.BalanceAddressViewModel
import com.kyberswap.android.presentation.main.balance.chart.CandleStickChartFragment
import com.kyberswap.android.presentation.main.balance.chart.CandleStickChartViewModel
import com.kyberswap.android.presentation.main.balance.chart.ChartFragment
import com.kyberswap.android.presentation.main.balance.chart.ChartViewModel
import com.kyberswap.android.presentation.main.balance.chart.LineChartFragment
import com.kyberswap.android.presentation.main.balance.chart.LineChartViewModel
import com.kyberswap.android.presentation.main.balance.send.SendFragment
import com.kyberswap.android.presentation.main.balance.send.SendViewModel
import com.kyberswap.android.presentation.main.explore.CampaignFragment
import com.kyberswap.android.presentation.main.explore.CampaignViewModel
import com.kyberswap.android.presentation.main.explore.ExploreFragment
import com.kyberswap.android.presentation.main.explore.ExploreViewModel
import com.kyberswap.android.presentation.main.kybercode.KyberCodeFragment
import com.kyberswap.android.presentation.main.kybercode.KyberCodeViewModel
import com.kyberswap.android.presentation.main.limitorder.CancelOrderFragment
import com.kyberswap.android.presentation.main.limitorder.ConvertFragment
import com.kyberswap.android.presentation.main.limitorder.FilterLimitOrderFragment
import com.kyberswap.android.presentation.main.limitorder.FilterViewModel
import com.kyberswap.android.presentation.main.limitorder.LimitOrderFragment
import com.kyberswap.android.presentation.main.limitorder.LimitOrderTokenSearchFragment
import com.kyberswap.android.presentation.main.limitorder.LimitOrderTokenSearchViewModel
import com.kyberswap.android.presentation.main.limitorder.LimitOrderV2Fragment
import com.kyberswap.android.presentation.main.limitorder.LimitOrderV2ViewModel
import com.kyberswap.android.presentation.main.limitorder.LimitOrderViewModel
import com.kyberswap.android.presentation.main.limitorder.ManageOrderFragment
import com.kyberswap.android.presentation.main.limitorder.ManageOrderViewModel
import com.kyberswap.android.presentation.main.limitorder.MarketFragment
import com.kyberswap.android.presentation.main.limitorder.MarketViewModel
import com.kyberswap.android.presentation.main.limitorder.OrderConfirmFragment
import com.kyberswap.android.presentation.main.limitorder.OrderConfirmV2Fragment
import com.kyberswap.android.presentation.main.notification.NotificationFragment
import com.kyberswap.android.presentation.main.notification.NotificationSettingFragment
import com.kyberswap.android.presentation.main.notification.NotificationSettingViewModel
import com.kyberswap.android.presentation.main.notification.NotificationViewModel
import com.kyberswap.android.presentation.main.profile.ProfileDetailFragment
import com.kyberswap.android.presentation.main.profile.ProfileDetailViewModel
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.profile.ProfileViewModel
import com.kyberswap.android.presentation.main.profile.SignUpConfirmFragment
import com.kyberswap.android.presentation.main.profile.SignUpFragment
import com.kyberswap.android.presentation.main.profile.SignUpViewModel
import com.kyberswap.android.presentation.main.setting.AddContactFragment
import com.kyberswap.android.presentation.main.setting.AddContactViewModel
import com.kyberswap.android.presentation.main.setting.ContactFragment
import com.kyberswap.android.presentation.main.setting.ContactViewModel
import com.kyberswap.android.presentation.main.setting.SettingFragment
import com.kyberswap.android.presentation.main.setting.SettingViewModel
import com.kyberswap.android.presentation.main.setting.wallet.BackupWalletInfoFragment
import com.kyberswap.android.presentation.main.setting.wallet.BackupWalletInfoViewModel
import com.kyberswap.android.presentation.main.setting.wallet.EditWalletFragment
import com.kyberswap.android.presentation.main.setting.wallet.EditWalletViewModel
import com.kyberswap.android.presentation.main.setting.wallet.ManageWalletFragment
import com.kyberswap.android.presentation.main.setting.wallet.ManageWalletViewModel
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.presentation.main.swap.SwapViewModel
import com.kyberswap.android.presentation.main.swap.TokenSearchFragment
import com.kyberswap.android.presentation.main.swap.TokenSearchViewModel
import com.kyberswap.android.presentation.main.transaction.CustomizeGasFragment
import com.kyberswap.android.presentation.main.transaction.CustomizeGasViewModel
import com.kyberswap.android.presentation.main.transaction.TransactionDetailReceiveFragment
import com.kyberswap.android.presentation.main.transaction.TransactionDetailSendFragment
import com.kyberswap.android.presentation.main.transaction.TransactionDetailSwapFragment
import com.kyberswap.android.presentation.main.transaction.TransactionDetailViewModel
import com.kyberswap.android.presentation.main.transaction.TransactionFilterFragment
import com.kyberswap.android.presentation.main.transaction.TransactionFilterViewModel
import com.kyberswap.android.presentation.main.transaction.TransactionFragment
import com.kyberswap.android.presentation.main.transaction.TransactionStatusFragment
import com.kyberswap.android.presentation.main.transaction.TransactionStatusViewModel
import com.kyberswap.android.presentation.main.transaction.TransactionViewModel
import com.kyberswap.android.presentation.main.walletconnect.WalletConnectFragment
import com.kyberswap.android.presentation.main.walletconnect.WalletConnectViewModel
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
    fun contributeBalanceAddressFragment(): BalanceAddressFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeTokenSearchFragment(): TokenSearchFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeSwapFragment(): SwapFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeLimitOrderFragment(): LimitOrderFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeLimitOrderV2Fragment(): LimitOrderV2Fragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeMarketFragment(): MarketFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeLimitOrderTokenSearchFragment(): LimitOrderTokenSearchFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeManageOrderFragment(): ManageOrderFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeFilterLimitOrderFragment(): FilterLimitOrderFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeOrderConfirmFragment(): OrderConfirmFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeOrderConfirmV2Fragment(): OrderConfirmV2Fragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeConvertFragment(): ConvertFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeCancelOrderFragment(): CancelOrderFragment

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
    fun contributeCandleStickChartFragment(): CandleStickChartFragment

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
    fun contributeExploreFragment(): ExploreFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeCampaignFragment(): CampaignFragment

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

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeProfileDetailFragment(): ProfileDetailFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributePriceAlertFragment(): PriceAlertFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributePriceAlertSearchFragment(): PriceAlertTokenSearchFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeManageAlertFragment(): ManageAlertFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeLeaderBoardFragment(): LeaderBoardFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeKyberCodeFragment(): KyberCodeFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeManageWalletFragment(): ManageWalletFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeEditWalletFragment(): EditWalletFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeBackupWalletInfoFragment(): BackupWalletInfoFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeAlertMethodFragment(): AlertMethodFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeWalletConnectFragment(): WalletConnectFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeNotificationFragment(): NotificationFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeNotificationSettingFragment(): NotificationSettingFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeCustomizeGasFragment(): CustomizeGasFragment

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
    @ViewModelKey(LimitOrderTokenSearchViewModel::class)
    fun bindLimitOrderTokenSearchViewModel(
        imitOrderTokenSearchViewModel: LimitOrderTokenSearchViewModel
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
    @ViewModelKey(CandleStickChartViewModel::class)
    fun bindCandleStickChartViewModel(
        chartViewModel: CandleStickChartViewModel
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
    @ViewModelKey(TransactionDetailViewModel::class)
    fun bindTransactionDetailViewModel(
        transactionDetailViewModel: TransactionDetailViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionFilterViewModel::class)
    fun bindTransactionFilterViewModel(
        transactionFilterViewModel: TransactionFilterViewModel
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

    @Binds
    @IntoMap
    @ViewModelKey(LimitOrderViewModel::class)
    fun bindLimitOrderViewModel(
        limitOrderViewModel: LimitOrderViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageOrderViewModel::class)
    fun bindManageOrderViewModel(
        manageOrderViewModel: ManageOrderViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FilterViewModel::class)
    fun bindFilterViewModel(
        filterViewModel: FilterViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileDetailViewModel::class)
    fun bindProfileDetailViewModel(
        filterViewModel: ProfileDetailViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PriceAlertViewModel::class)
    fun bindPriceAlertViewModel(
        priceAlertViewModel: PriceAlertViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PriceAlertTokenSearchViewModel::class)
    fun bindPriceAlertTokenSearchViewModel(
        priceAlertTokenSearchViewModel: PriceAlertTokenSearchViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MangeAlertViewModel::class)
    fun bindMangeAlertViewModel(
        mangeAlertViewModel: MangeAlertViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LeaderBoardViewModel::class)
    fun bindLeaderBoardViewModel(
        leaderBoardViewModel: LeaderBoardViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(KyberCodeViewModel::class)
    fun bindKyberCodeViewModel(
        kyberCodeViewModel: KyberCodeViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageWalletViewModel::class)
    fun bindManageWalletViewModel(
        manageWalletViewModel: ManageWalletViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditWalletViewModel::class)
    fun bindEditWalletViewModel(
        editWalletViewModel: EditWalletViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BackupWalletInfoViewModel::class)
    fun bindBackupWalletInfoViewModel(
        backupWalletInfoViewModel: BackupWalletInfoViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AlertMethodViewModel::class)
    fun bindAlertMethodViewModel(
        alertMethodViewModel: AlertMethodViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WalletConnectViewModel::class)
    fun bindWalletConnectViewModel(
        walletConnectViewModel: WalletConnectViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    fun bindNotificationViewModel(
        notificationViewModel: NotificationViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationSettingViewModel::class)
    fun bindNotificationSettingViewModel(
        notificationSettingViewModel: NotificationSettingViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CustomizeGasViewModel::class)
    fun bindCustomizeGasViewModel(
        customizeGasViewModel: CustomizeGasViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MarketViewModel::class)
    fun bindMarketViewModel(
        marketViewModel: MarketViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LimitOrderV2ViewModel::class)
    fun bindLimitOrderV2ViewModel(
        limitOrderV2ViewModel: LimitOrderV2ViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExploreViewModel::class)
    fun bindExploreViewModel(
        exploreViewModel: ExploreViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CampaignViewModel::class)
    fun bindCampaignViewModel(
        campaignViewModel: CampaignViewModel
    ): ViewModel
}
