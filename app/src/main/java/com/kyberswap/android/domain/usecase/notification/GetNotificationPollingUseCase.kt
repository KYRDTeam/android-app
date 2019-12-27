package com.kyberswap.android.domain.usecase.notification

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetNotificationPollingUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val userRepository: UserRepository
) : FlowableUseCase<String?, List<Notification>>(
    schedulerProvider
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String?): Flowable<List<Notification>> {
        return userRepository.getNotifications()
            .repeatWhen {
                it.delay(60, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }
}
