package com.kyberswap.android.data.api.transaction

import com.google.gson.annotations.SerializedName

data class TransactionsEntity(
    @SerializedName("message")
    val message: String? = "",
    @SerializedName("result")
    val result: List<TransactionEntity> = listOf(),
    @SerializedName("status")
    val status: String? = ""
)