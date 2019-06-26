package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.data.api.alert.LeaderBoardEntity
import com.kyberswap.android.data.api.user.ResponseStatusEntity
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.LeaderBoard
import com.kyberswap.android.domain.model.ResponseStatus
import javax.inject.Inject

class AlertMapper @Inject constructor() {
    fun transform(alertEntity: AlertEntity): Alert {
        return Alert(alertEntity)
    }

    fun transform(alertEntity: ResponseStatusEntity): ResponseStatus {
        return ResponseStatus(alertEntity)
    }


    fun transform(entity: LeaderBoardEntity): LeaderBoard {
        return LeaderBoard(entity)
    }
}