package com.kyberswap.android.util.di.module

import com.kyberswap.android.presentation.common.AlertActivity
import com.kyberswap.android.presentation.common.AlertWithoutIconActivity
import com.kyberswap.android.presentation.common.CustomAlertActivity
import com.kyberswap.android.presentation.landing.LandingActivity
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.send.SendConfirmActivity
import com.kyberswap.android.presentation.main.profile.TermConditionActivity
import com.kyberswap.android.presentation.main.swap.PromoPaymentConfirmActivity
import com.kyberswap.android.presentation.main.swap.PromoSwapConfirmActivity
import com.kyberswap.android.presentation.main.swap.SwapConfirmActivity
import com.kyberswap.android.presentation.setting.PassCodeLockActivity
import com.kyberswap.android.presentation.splash.SplashActivity
import com.kyberswap.android.presentation.wallet.BackupWalletActivity
import com.kyberswap.android.presentation.wallet.ImportWalletActivity
import com.kyberswap.android.presentation.wallet.VerifyBackupWordActivity
import com.kyberswap.android.util.di.scope.ActivityScoped
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            MainActivityModule::class
        ]
    )
    fun contributeMainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SplashActivityModule::class
        ]
    )
    fun contributeSplashActivity(): SplashActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            LandingActivityModule::class
        ]
    )
    fun contributeLandingActivity(): LandingActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            BackupWalletActivityModule::class
        ]
    )
    fun contributeBackupWalletActivity(): BackupWalletActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            VerifyBackupWordActivityModule::class
        ]
    )
    fun contributeVerifyBackupWordActivity(): VerifyBackupWordActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            ImportWalletActivityModule::class
        ]
    )
    fun contributeImportWalletActivity(): ImportWalletActivity

    @ActivityScoped
    @ContributesAndroidInjector()
    fun contributeAlertActivity(): AlertActivity

    @ActivityScoped
    @ContributesAndroidInjector()
    fun contributeInsufficientAlertActivity(): AlertWithoutIconActivity


    @ActivityScoped
    @ContributesAndroidInjector()
    fun contributeCustomAlertActivity(): CustomAlertActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SwapConfirmActivityModule::class
        ]
    )
    fun contributeSwapConfirmActivity(): SwapConfirmActivity


    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            SendConfirmActivityModule::class
        ]
    )
    fun contributeSendConfirmActivity(): SendConfirmActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            TermConditionActivityModule::class
        ]
    )
    fun contributeTermConditionActivity(): TermConditionActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            PromoPaymentActivityModule::class
        ]
    )
    fun contributePromoPaymentConfirmActivity(): PromoPaymentConfirmActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            PromoSwapActivityModule::class
        ]
    )
    fun contributePromoSwapConfirmActivity(): PromoSwapConfirmActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            PassCodeLockActivityModule::class
        ]
    )
    fun contributePassCodeLockActivity(): PassCodeLockActivity
}
