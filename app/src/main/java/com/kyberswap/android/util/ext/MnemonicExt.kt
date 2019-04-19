package com.kyberswap.android.util.ext

fun Int.correctNumberOfWords(): Int {
    var correctNumberOfWords: Int = this
    if (this % 3 != 0 || this < 12 || this > 24) {
        correctNumberOfWords = 12
    }

    return correctNumberOfWords / 3 * 4
}