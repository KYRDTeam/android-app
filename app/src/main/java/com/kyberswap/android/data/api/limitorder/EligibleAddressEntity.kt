package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.SerializedName

data class EligibleAddressEntity(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("eligible_address")
    val eligibleAddress: Boolean = false,
    @SerializedName("account")
    val account: String? = ""
)