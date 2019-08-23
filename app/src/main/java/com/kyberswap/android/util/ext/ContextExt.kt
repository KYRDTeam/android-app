package com.kyberswap.android.util.ext

import android.content.Context
import java.io.IOException

fun Context.loadJSONFromAssets(fileName: String): String? {
    var json: String? = null
    try {
        val inputStream = assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer, Charsets.UTF_8)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return json
}