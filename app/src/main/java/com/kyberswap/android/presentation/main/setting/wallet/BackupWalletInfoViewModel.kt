package com.kyberswap.android.presentation.main.setting.wallet

import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import javax.inject.Inject

class BackupWalletInfoViewModel @Inject constructor(
    getSelectedWalletUseCase: GetSelectedWalletUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase)