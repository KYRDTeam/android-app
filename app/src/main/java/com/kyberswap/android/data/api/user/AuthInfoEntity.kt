package com.kyberswap.android.data.api.user


import com.google.gson.annotations.SerializedName

data class AuthInfoEntity(
    @SerializedName("auth_token")
    val authToken: String = "",
    @SerializedName("expiration_time")
    val expirationTime: String = "",
    @SerializedName("refresh_token")
    val refreshToken: String = ""
)