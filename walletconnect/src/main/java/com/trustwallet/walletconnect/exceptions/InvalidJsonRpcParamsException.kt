package com.trustwallet.walletconnect.exceptions

class InvalidJsonRpcParamsException(val requestId: Long) : Exception("Invalid JSON RPC Request")
