package com.kyberswap.android.data.api.user


import com.google.gson.annotations.SerializedName

data class RegisterStatusEnity(
    @SerializedName("message")
    val message: String = "",
    @SerializedName("success")
    val success: Boolean = false
)