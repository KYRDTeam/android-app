package com.kyberswap.android.data.api.notification


import com.google.gson.annotations.SerializedName

data class NotificationEntity(
    @SerializedName("id")
    val id: Long?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("scope")
    val scope: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("read")
    val read: Boolean?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("user_id")
    val userId: Long?,
    @SerializedName("data")
    val `data`: NotificationExtEntity?
)