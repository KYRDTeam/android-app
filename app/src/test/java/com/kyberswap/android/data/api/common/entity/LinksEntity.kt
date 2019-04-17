package com.kyberswap.android.data.api.common.entity

import com.google.gson.annotations.SerializedName

data class LinksEntity(
    @SerializedName("next") val next: NextEntity = NextEntity(),
    @SerializedName("prev") val prev: PrevEntity = PrevEntity(),
    @SerializedName("self") val self: SelfEntity = SelfEntity()
)
