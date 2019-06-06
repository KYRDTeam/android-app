package com.kyberswap.android.data.api.user


import com.google.gson.annotations.SerializedName

data class UserInfoEntity(
    @SerializedName("active_wallets")
    val activeWallets: List<String> = listOf(),
    @SerializedName("avatar_url")
    val avatarUrl: String = "",
    @SerializedName("contact_id")
    val contactId: String = "",
    @SerializedName("contact_type")
    val contactType: String = "",
    @SerializedName("kyc_status")
    val kycStatus: String = "",
    @SerializedName("kyc_step")
    val kycStep: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("uid")
    val uid: Long = 0
)