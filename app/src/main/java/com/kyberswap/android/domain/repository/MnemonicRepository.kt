package com.kyberswap.android.domain.repository

import io.reactivex.Single

interface MnemonicRepository {
    fun create12wordsAccount(numberOfWords: Int): Single<List<String>>
}