package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.balance.send.SendConfirmActivity
import com.kyberswap.android.presentation.main.balance.send.SendConfirmViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SendConfirmActivityModule {

    @Binds
    fun bindsAppCompatActivity(sendConfirmActivity: SendConfirmActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(SendConfirmViewModel::class)
    fun bindSendConfirmViewModel(
        sendConfirmViewModel: SendConfirmViewModel
    ): ViewModel
}