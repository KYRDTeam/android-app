package com.kyberswap.android.data.api.wallet


import com.google.gson.annotations.SerializedName

data class EligibleWalletStatusEntity(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("eligible")
    val eligible: Boolean?,
    @SerializedName("message")
    val message: String?
)