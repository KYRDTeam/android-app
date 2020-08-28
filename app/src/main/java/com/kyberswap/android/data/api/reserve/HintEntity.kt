package com.kyberswap.android.data.api.reserve

import com.google.gson.annotations.SerializedName

data class HintEntity(
    @SerializedName("error")
    val error: String? = "",
    @SerializedName("hint")
    val hint: String? = "",
    @SerializedName("success")
    val success: Boolean = false
)