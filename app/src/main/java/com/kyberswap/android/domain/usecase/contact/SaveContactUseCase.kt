package com.kyberswap.android.domain.usecase.contact

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.ContactRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveContactUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val contactRepository: ContactRepository
) : CompletableUseCase<SaveContactUseCase.Param>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return contactRepository.saveContact(param)
    }

    class Param(val walletAddress: String, val name: String, val address: String)
}
