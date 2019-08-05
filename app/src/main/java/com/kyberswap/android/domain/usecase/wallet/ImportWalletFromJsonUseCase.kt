package com.kyberswap.android.domain.usecase.wallet

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class ImportWalletFromJsonUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : SequentialUseCase<ImportWalletFromJsonUseCase.Param, Pair<Wallet, List<Token>>>(
    schedulerProvider
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<Pair<Wallet, List<Token>>> {
        return walletRepository.importWallet(param)
    }

    class Param(val uri: Uri, val password: String, val walletName: String)
}
