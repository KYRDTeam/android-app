package com.kyberswap.android.domain.model


import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.alert.EmailEntity

data class Email(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("active")
    val active: Boolean = false
) {
    constructor(entity: EmailEntity) : this(entity.id, entity.active)
}