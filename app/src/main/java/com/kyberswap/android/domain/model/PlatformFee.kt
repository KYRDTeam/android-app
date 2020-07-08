package com.kyberswap.android.domain.model


import android.os.Parcelable
import com.kyberswap.android.data.api.fee.PlatformFeeEntity
import com.kyberswap.android.presentation.common.PLATFORM_FEE_BPS
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlatformFee(
    val success: Boolean = false,
    val fee: Int = 8
) : Parcelable {
    constructor(entity: PlatformFeeEntity) : this(
        entity.success ?: false,
        if (entity.success == true) entity.fee ?: PLATFORM_FEE_BPS else PLATFORM_FEE_BPS
    )
}

