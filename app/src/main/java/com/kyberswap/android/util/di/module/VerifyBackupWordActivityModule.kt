package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.wallet.VerifyBackupWordActivity
import com.kyberswap.android.presentation.wallet.VerifyBackupWordViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface VerifyBackupWordActivityModule {

    @Binds
    fun bindsAppCompatActivity(verifyBackupWordActivity: VerifyBackupWordActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(VerifyBackupWordViewModel::class)
    fun bindVerifyBackupWordViewModel(
        verifyBackupWordViewModel: VerifyBackupWordViewModel
    ): ViewModel
}