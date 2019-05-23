package com.kyberswap.android.presentation.main.balance.other

import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.GetBalanceUseCase
import javax.inject.Inject

class OtherViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase
) : ViewModel()