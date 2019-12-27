package com.kyberswap.android.data.api.notification


import com.google.gson.annotations.SerializedName

data class NotificationsResponseEntity(
    @SerializedName("paging_info")
    val pagingInfo: PagingInfoEntity = PagingInfoEntity(),
    @SerializedName("data")
    val `data`: List<NotificationEntity> = listOf()
)