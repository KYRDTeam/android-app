package com.trustwallet.walletconnect.exceptions

class InvalidHmacException : Exception("Received and computed HMAC doesn't mach")