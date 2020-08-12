package com.kyberswap.android.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object FileUtils {

    @Throws(Exception::class)
    fun getStringFromPath(context: Context, resId: Int): String {

        val inputStream = context.resources.openRawResource(resId)

        val builder = StringBuilder()

        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var string: String? = reader.readLine()
            while (string != null) {
                builder.append(string + System.getProperty("line.separator"))
                string = reader.readLine()
            }
        }

        return builder.toString()
    }
}