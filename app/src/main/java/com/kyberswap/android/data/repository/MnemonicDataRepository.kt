package com.kyberswap.android.data.repository

import com.kyberswap.android.domain.repository.MnemonicRepository
import com.kyberswap.android.util.ext.correctNumberOfWords
import io.reactivex.Single
import org.bitcoinj.crypto.MnemonicCode
import java.security.SecureRandom
import javax.inject.Inject

class MnemonicDataRepository @Inject constructor(
    private val mnemonicCode: MnemonicCode
) : MnemonicRepository {
    override fun create12wordsAccount(numberOfWords: Int): Single<List<String>> {
        return Single.fromCallable {
            val random = SecureRandom()
            val seed = ByteArray(numberOfWords.correctNumberOfWords())
            random.nextBytes(seed)
            mnemonicCode.toMnemonic(seed)

    }

}