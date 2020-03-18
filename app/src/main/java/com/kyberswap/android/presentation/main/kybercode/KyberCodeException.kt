package com.kyberswap.android.presentation.main.kybercode

class KyberCodeException : RuntimeException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, e: Exception?) : super(message, e) {}
}