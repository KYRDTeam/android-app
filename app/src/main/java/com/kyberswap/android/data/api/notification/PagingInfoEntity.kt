package com.kyberswap.android.data.api.notification


import com.google.gson.annotations.SerializedName

data class PagingInfoEntity(
    @SerializedName("item_count")
    val itemCount: Int? = 0,
    @SerializedName("page_count")
    val pageCount: Int? = 0,
    @SerializedName("page_size")
    val pageSize: Int? = 0,
    @SerializedName("page_index")
    val pageIndex: Int? = 0,
    @SerializedName("unread_count")
    val unreadCount: Int? = 0
)