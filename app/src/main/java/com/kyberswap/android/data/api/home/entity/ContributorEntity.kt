package com.kyberswap.android.data.api.home.entity

import com.google.gson.annotations.SerializedName

data class ContributorEntity(
    @SerializedName("id") val id: String = "",
    @SerializedName("affiliation") val affiliation: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("job_title") val jobTitle: String = "",
    @SerializedName("name") val name: String = ""
)
