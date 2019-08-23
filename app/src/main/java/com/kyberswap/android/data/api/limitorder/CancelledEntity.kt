package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.SerializedName

data class CancelledEntity(
    @SerializedName("cancelled")
    val cancelled: Boolean = false
)