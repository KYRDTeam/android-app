package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.alert.CampaignInfoEntity
import java.text.SimpleDateFormat
import java.util.*


data class CampaignInfo(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val rewardUnit: String = "",
    val reward: String = "",
    val eligibleTokens: String = ""
) {
    constructor(entity: CampaignInfoEntity) : this(
        entity.id,
        entity.title,
        entity.description,
        entity.startTime,
        entity.endTime,
        entity.rewardUnit,
        entity.reward,
        entity.eligibleTokens
    )

    val displayStartTime: String
        get() = try {
            Alert.displayFormat.format(Alert.fullFormat.parse(startTime))
        } catch (ex: Exception) {
            ex.printStackTrace()
            startTime
        }

    val displayEndTime: String
        get() = try {
            Alert.displayFormat.format(Alert.fullFormat.parse(endTime))
        } catch (ex: Exception) {
            ex.printStackTrace()
            endTime
        }

    companion object {
        val displayFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US)
        val fullFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    }
}