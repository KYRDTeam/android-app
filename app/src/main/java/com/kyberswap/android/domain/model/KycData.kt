package com.kyberswap.android.domain.model


import com.google.gson.annotations.SerializedName

data class KycData(
    @SerializedName("nationalities")
    val nationalities: List<String> = listOf(),
    @SerializedName("countries")
    val countries: List<String> = listOf(),
    @SerializedName("proof_address")
    val proofAddress: List<String> = listOf(),
    @SerializedName("source_funds")
    val sourceFunds: List<String> = listOf()
)