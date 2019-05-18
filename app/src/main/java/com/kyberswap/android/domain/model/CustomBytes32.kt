package com.kyberswap.android.domain.model

import org.web3j.abi.datatypes.generated.Bytes32

class CustomBytes32(value: ByteArray) : Bytes32(value) {

    override fun getTypeAsString(): String {
        return TYPE_NAME
    }

    companion object {
        private const val TYPE_NAME = "bytes"
    }
}
