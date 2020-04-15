package com.kyberswap.android.data.api.limitorder

import com.google.gson.annotations.SerializedName

data class FavoritePairsEntity(
    @SerializedName("favorite_pairs")
    val favoritePairs: List<FavoritePair> = listOf(),
    @SerializedName("success")
    val success: Boolean = false
)