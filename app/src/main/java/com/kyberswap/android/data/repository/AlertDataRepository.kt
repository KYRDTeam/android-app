package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.db.AlertDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.mapper.AlertMapper
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.LeaderBoard
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.AlertRepository
import com.kyberswap.android.domain.usecase.alert.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject


class AlertDataRepository @Inject constructor(
    private val alertDao: AlertDao,
    private val tokenDao: TokenDao,
    private val userApi: UserApi,
    private val alertMapper: AlertMapper
) : AlertRepository {
    override fun getCampaignResult(): Single<LeaderBoard> {
        return userApi.getCampaignResult().map {
            alertMapper.transform(it)
        }
    }

    override fun getLeaderBoardAlert(): Single<LeaderBoard> {
        return userApi.getLeaderBoard().map {
            alertMapper.transform(it)
        }
    }

    override fun deleteAlert(param: DeleteAlertsUseCase.Param): Single<Response<Void>> {
        return userApi.deleteAlert(param.alert.id).doAfterSuccess {
            alertDao.deleteById(param.alert.id)
        }
    }

    override fun createOrUpdateAlert(param: CreateOrUpdateAlertUseCase.Param): Single<Alert> {
        val alert = param.alert
        return if (alert.id > 0) {
            userApi.updateAlert(
                alert.id,
                alert.baseInt,
                alert.tokenSymbol,
                alert.alertPrice,
                alert.isAbove
            ).map {
                alertMapper.transform(it)
            }
        } else {
            userApi.createAlert(
                alert.baseInt,
                alert.tokenSymbol,
                alert.alertPrice,
                alert.isAbove
            ).map {
                alertMapper.transform(it)
            }
        }
            .doAfterSuccess {
                if (it.message.isNullOrEmpty()) {
                    alertDao.updateAlert(alert)
                }
            }
    }


    override fun getCurrentAlert(param: GetCurrentAlertUseCase.Param): Flowable<Alert> {
        return Flowable.fromCallable {
            val id = if (param.alert == null) {
                Alert.LOCAL_ID
            } else {
                param.alert.id
            }

            val currentAlert = alertDao.findAlertById(id)
            val alert = if (currentAlert == null) {
                val defaultToken = tokenDao.getTokenBySymbol(Token.KNC)
                Alert(
                    id = Alert.LOCAL_ID,
                    walletAddress = param.walletAddress,
                    token = defaultToken ?: Token(),
                    state = Alert.STATE_LOCAL
                )

            } else {
                val token = tokenDao.getTokenBySymbol(currentAlert.tokenSymbol) ?: Token()
                currentAlert.copy(token = token)
            }

            if (currentAlert != alert) {
                alertDao.insertAlert(alert)
            }
            alert
        }.flatMap {
            alertDao.findAlertByIdFlowable(it.id)
        }
    }

    override fun saveAlertToken(param: SaveAlertTokenUseCase.Param): Completable {
        return Completable.fromCallable {

            val currentAlert = if (param.alert != null && param.alert.id > 0) {

                alertDao.findAlertById(param.alert.id)
            } else {
                alertDao.findLocalAlert(param.walletAddress)
            }

            val alert = currentAlert?.copy(
                token = param.token
            ) ?: Alert(walletAddress = param.walletAddress, token = param.token)
            alertDao.insertAlert(alert)
        }
    }

    override fun updateCurrentAlert(param: UpdateCurrentAlertUseCase.Param): Completable {
        return Completable.fromCallable {
            val currentAlert = alertDao.findAlertById(param.alert.id)?.copy(token = Token())
            currentAlert?.let {
                alertDao.updateAlert(currentAlert)
            }


        }
    }


    override fun getAlert(param: GetAlertUseCase.Param): Single<Alert> {
        return Single.fromCallable {
            alertDao.findAlertById(param.alertId)
        }
    }
}