package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.main.swap.PromoPaymentConfirmActivity
import com.kyberswap.android.presentation.main.swap.SwapConfirmViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface PromoPaymentActivityModule {

    @Binds
    fun bindsAppCompatActivity(promoPaymentActivityModule: PromoPaymentConfirmActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(SwapConfirmViewModel::class)
    fun bindSwapConfirmViewModel(
        swapConfirmViewModel: SwapConfirmViewModel
    ): ViewModel

}