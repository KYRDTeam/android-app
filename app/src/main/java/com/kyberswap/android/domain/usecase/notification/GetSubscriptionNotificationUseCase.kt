package com.kyberswap.android.domain.usecase.notification

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.SubscriptionNotification
import com.kyberswap.android.domain.model.SubscriptionNotificationData
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetSubscriptionNotificationUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val userRepository: UserRepository
) : SequentialUseCase<String?, SubscriptionNotification>(
    schedulerProvider
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: String?): Single<SubscriptionNotification> {
        return userRepository.getSubscriptionNotifications()
    }
}
