package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.LeaderBoard
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.usecase.alert.CreateOrUpdateAlertUseCase
import com.kyberswap.android.domain.usecase.alert.DeleteAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.DeleteAllAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.GetAlertUseCase
import com.kyberswap.android.domain.usecase.alert.GetCurrentAlertUseCase
import com.kyberswap.android.domain.usecase.alert.SaveAlertTokenUseCase
import com.kyberswap.android.domain.usecase.alert.UpdateCurrentAlertUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.Response

interface AlertRepository {

    fun saveAlertToken(param: SaveAlertTokenUseCase.Param): Completable

    fun getCurrentAlert(param: GetCurrentAlertUseCase.Param): Flowable<Alert>

    fun createOrUpdateAlert(param: CreateOrUpdateAlertUseCase.Param): Single<Alert>

    fun deleteAlert(param: DeleteAlertsUseCase.Param): Single<Response<Void>>

    fun getLeaderBoardAlert(): Single<LeaderBoard>

    fun getCampaignResult(): Single<LeaderBoard>

    fun getAlert(param: GetAlertUseCase.Param): Single<Alert>

    fun updateCurrentAlert(param: UpdateCurrentAlertUseCase.Param): Completable

    fun deleteAllTriggerAlerts(param: DeleteAllAlertsUseCase.Param): Single<ResponseStatus>
}