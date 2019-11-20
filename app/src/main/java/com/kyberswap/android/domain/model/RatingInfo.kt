package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.BuildConfig
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "ratings")
@Parcelize
data class RatingInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val updatedAt: Long = 0L,
    val count: Int = 0,
    val isNotNow: Boolean = false,
    val isFinished: Boolean = false
) : Parcelable {

    val isShowAlert: Boolean
        get() = !isFinished && count > NUMBER_TO_SHOW_ALERT

    val reShowAlert: Boolean
        get() = !isFinished && count > NUMBER_TO_SHOW_ALERT &&
            if (BuildConfig.FLAVOR == "dev" || BuildConfig.FLAVOR == "stg") (System.currentTimeMillis() / 1000L - updatedAt) / 60f > 2 else
                (System.currentTimeMillis() / 1000L - updatedAt) / 60 / 60 / 24 / 30f > 1


    companion object {
        private val NUMBER_TO_SHOW_ALERT =
            if (BuildConfig.FLAVOR == "dev" || BuildConfig.FLAVOR == "stg") 3 else 11
    }
}