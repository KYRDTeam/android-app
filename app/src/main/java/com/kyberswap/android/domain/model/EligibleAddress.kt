package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.limitorder.EligibleAddressEntity


data class EligibleAddress(
    val success: Boolean = false,
    val eligibleAddress: Boolean = false,
    val account: String = ""
) {
    constructor(eligibleAddressEntity: EligibleAddressEntity) : this(
        eligibleAddressEntity.success,
        eligibleAddressEntity.eligibleAddress,
        eligibleAddressEntity.account ?: ""
    )
}