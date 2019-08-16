package com.kyberswap.android.data.api.limitorder


import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class PagingInfoEntity(
    @SerializedName("items_count")
    val itemsCount: Int = 0,
    @SerializedName("page_count")
    val pageCount: Int = 0,
    @SerializedName("page_index")
    val pageIndex: Int = 0,
    @SerializedName("page_size")
    val pageSize: Int = 0
)

fun JsonObject.toPagingInfo(): PagingInfoEntity {
    return try {
        val pageCount = if (get("page_count").isJsonNull) {
            0
        } else {
            get("page_count").asInt
        }

        val pageIndex = if (get("page_index").isJsonNull) {
            0
        } else {
            get("page_index").asInt
        }

        val pageSize = if (get("page_size").isJsonNull) {
            0
        } else {
            get("page_size").asInt
        }

        val itemCount = if (get("items_count").isJsonNull) {
            0
        } else {
            get("items_count").asInt
        }


        PagingInfoEntity(itemCount, pageCount, pageIndex, pageSize)
    } catch (ex: Exception) {
        ex.printStackTrace()
        PagingInfoEntity()
    }


}