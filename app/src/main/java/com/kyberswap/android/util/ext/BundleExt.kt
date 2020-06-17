package com.kyberswap.android.util.ext

import android.os.Bundle
import com.kyberswap.android.util.ACTION_NAME
import com.kyberswap.android.util.ACTION_VALUE
import kotlin.math.min


fun Bundle.createEvent(name: String, value: String?): Bundle {
    this.putString(ACTION_NAME, name)
    this.putString(ACTION_VALUE, value)
    return this
}

fun Bundle.createEvent(names: List<String>, values: List<String?>): Bundle {
    val length = min(names.size, values.size)
    for (i in 0 until length) {
        this.putString(names[i], values[i])
    }
    return this
}


fun Bundle.createEvent(value: String? = "1"): Bundle {
    this.putString(ACTION_VALUE, value)
    return this
}