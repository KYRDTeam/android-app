package com.kyberswap.android.data.api.promo


import com.google.gson.annotations.SerializedName

data class PromoEntity(
    @SerializedName("private_key")
    val privateKey: String? = "",
    @SerializedName("expired_date")
    val expiredDate: String? = "",
    @SerializedName("destination_token")
    val destinationToken: String? = "",
    @SerializedName("description")
    val description: String? = "",
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("receive_address")
    val receiveAddress: String? = "",
    @SerializedName("error")
    var error: String? = ""
)