package com.kyberswap.android.data.service

class ServiceException(message: String) : Exception(message) {
    val error = ErrorEnvelope(message)
}
