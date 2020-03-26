package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateSubscribedNotificationRequestBody(
    @SerializedName("list_token_symbol") val listTokenSymbol: List<String>
) : Parcelable