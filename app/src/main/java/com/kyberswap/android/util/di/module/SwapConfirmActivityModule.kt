package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.swap.SwapConfirmActivity
import com.kyberswap.android.presentation.main.swap.SwapConfirmViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SwapConfirmActivityModule {

    @Binds
    fun bindsAppCompatActivity(swapConfirmActivity: SwapConfirmActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(SwapConfirmViewModel::class)
    fun bindSwapConfirmViewModel(
        swapConfirmViewModel: SwapConfirmViewModel
    ): ViewModel
}