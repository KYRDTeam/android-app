package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import com.kyberswap.android.presentation.main.profile.TermConditionActivity
import dagger.Binds
import dagger.Module

@Module
interface TermConditionActivityModule {

    @Binds
    fun bindsAppCompatActivity(termConditionActivity: TermConditionActivity): AppCompatActivity

}