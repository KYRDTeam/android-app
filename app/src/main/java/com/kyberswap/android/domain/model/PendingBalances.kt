package com.kyberswap.android.domain.model


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.limitorder.PendingBalancesEntity
import com.kyberswap.android.data.db.PendingBalancesConverter
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Entity(tableName = "pending_balances")
@Parcelize
data class PendingBalances(
    val success: Boolean = false,
    @TypeConverters(PendingBalancesConverter::class)
    val `data`: Map<String, BigDecimal> = HashMap(),
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) : Parcelable {
    constructor(entity: PendingBalancesEntity) : this(
        entity.success,
        entity.data
    )
}