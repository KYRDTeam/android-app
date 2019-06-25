package com.kyberswap.android.presentation.main.balance.address

import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import javax.inject.Inject

class BalanceAddressViewModel @Inject constructor(
    getSelectedWalletUseCase: GetSelectedWalletUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase)