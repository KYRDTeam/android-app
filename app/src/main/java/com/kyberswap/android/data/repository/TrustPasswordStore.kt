package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.PasswordRepository
import com.kyberswap.android.util.KS
import io.reactivex.Completable
import io.reactivex.Single
import java.security.SecureRandom

class TrustPasswordStore(private val context: Context) : PasswordRepository {

    override fun getPassword(wallet: Wallet): Single<String> {
        return Single.fromCallable { String(KS.get(context, wallet.address)) }
    }

    override fun setPassword(wallet: Wallet, password: String): Completable {
        return Completable.fromAction { KS.put(context, wallet.address, password) }
    }

    override fun generatePassword(): Single<String> {
        return Single.fromCallable {
            val bytes = ByteArray(256)
            val random = SecureRandom()
            random.nextBytes(bytes)
            bytes.toString(Charsets.UTF_8)
        }
    }

    override fun passwordVerification(wallet: Wallet, masterPassword: String): Single<Wallet> {
        throw UnsupportedOperationException()
    }
}
