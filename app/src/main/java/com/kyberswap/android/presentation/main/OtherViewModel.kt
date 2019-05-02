package com.kyberswap.android.presentation.main

import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.GetBalanceUseCase
import javax.inject.Inject

class OtherViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase
) : ViewModel()