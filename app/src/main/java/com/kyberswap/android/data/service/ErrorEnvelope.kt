package com.kyberswap.android.data.service

import com.kyberswap.android.Constant

class ErrorEnvelope constructor(
    val code: Int,
    val message: String?,
    private val throwable: Throwable? = null
) {

    constructor(message: String?) : this(Constant.ErrorCode.UNKNOWN, message)
}
