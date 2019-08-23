package com.kyberswap.android.data.api.alert


import com.google.gson.annotations.SerializedName

data class EmailEntity(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("active")
    val active: Boolean = false
)