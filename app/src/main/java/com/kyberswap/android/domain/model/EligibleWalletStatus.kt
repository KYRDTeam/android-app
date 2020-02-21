package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.wallet.EligibleWalletStatusEntity


data class EligibleWalletStatus(
    val success: Boolean = false,
    val eligible: Boolean = false,
    val message: String = ""
) {
    constructor(entity: EligibleWalletStatusEntity) : this(
        entity.success ?: false,
        entity.eligible ?: false,
        entity.message ?: ""
    )
}