package com.kyberswap.android.domain.usecase.contact

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.repository.ContactRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class DeleteContactUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val contactRepository: ContactRepository
) : CompletableUseCase<DeleteContactUseCase.Param, Any?>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return contactRepository.deleteContact(param)
    }

    class Param(val contact: Contact)
}
