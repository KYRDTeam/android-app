package com.kyberswap.android.util.ext

import android.os.Bundle
import com.kyberswap.android.util.ACTION_NAME
import com.kyberswap.android.util.ACTION_VALUE


fun Bundle.createEvent(name: String, value: String?): Bundle {
    this.putString(ACTION_NAME, name)
    this.putString(ACTION_VALUE, value)
    return this
}

fun Bundle.createEvent(value: String? = "1"): Bundle {
    this.putString(ACTION_VALUE, value)
    return this
}