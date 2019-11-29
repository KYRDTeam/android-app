package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.walletconnect.WalletConnectActivity
import com.kyberswap.android.presentation.main.walletconnect.WalletConnectViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface WalletConnectActivityModule {

    @Binds
    fun bindsAppCompatActivity(walletConnectActivity: WalletConnectActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(WalletConnectViewModel::class)
    fun bindWalletConnectViewModel(
        walletConnectViewModel: WalletConnectViewModel
    ): ViewModel
}