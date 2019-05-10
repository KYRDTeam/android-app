package com.kyberswap.android.domain.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import com.kyberswap.android.data.api.rate.RateEntity

@Entity(
    tableName = "rates",
    primaryKeys = ["source", "dest"]
)
data class Rate(
    @NonNull
    var source: String = "",
    @NonNull
    var dest: String = "",
    var rate: String = "",
    var minRate: String = ""
) {
    @Ignore
    constructor(entity: RateEntity) : this(
        entity.source,
        entity.dest,
        entity.rate,
        entity.minRate
    )
}