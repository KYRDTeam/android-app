package com.kyberswap.android.util.ext

fun Map<String, List<String>>.display(): String {
    val builder = StringBuilder()
    for ((key, value) in this) {
        builder.append(key).append(" : ")
        value.forEachIndexed { index, s ->
            builder.append(s)
            if (index < value.size - 1) {
                builder.append(", ")
    


        builder.append("\n")
    }
    return builder.toString()
}