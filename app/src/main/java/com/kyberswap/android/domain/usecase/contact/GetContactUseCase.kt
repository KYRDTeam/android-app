package com.kyberswap.android.domain.usecase.contact

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.repository.ContactRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetContactUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val contactRepository: ContactRepository
) : FlowableUseCase<GetContactUseCase.Param, List<Contact>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<List<Contact>> {
        return contactRepository.getContacts(param)
    }

    class Param(val walletAddress: String)
}
