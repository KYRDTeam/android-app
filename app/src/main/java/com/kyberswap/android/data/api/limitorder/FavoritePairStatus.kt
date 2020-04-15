package com.kyberswap.android.data.api.limitorder

import com.google.gson.annotations.SerializedName

data class FavoritePairStatus(
    @SerializedName("base")
    val base: String = "",
    @SerializedName("quote")
    val quote: String = "",
    @SerializedName("status")
    val status: Boolean = false
)