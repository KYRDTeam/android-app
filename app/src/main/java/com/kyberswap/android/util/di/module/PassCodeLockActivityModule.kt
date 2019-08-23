package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.setting.PassCodeLockActivity
import com.kyberswap.android.presentation.setting.PassCodeLockViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface PassCodeLockActivityModule {

    @Binds
    fun bindsAppCompatActivity(landingActivity: PassCodeLockActivity): AppCompatActivity


    @Binds
    @IntoMap
    @ViewModelKey(PassCodeLockViewModel::class)
    fun bindPassCodeLockViewModel(
        passCodeLockViewModel: PassCodeLockViewModel
    ): ViewModel
}