package com.kyberswap.android.domain.repository


import com.kyberswap.android.domain.model.Wallet
import io.reactivex.Completable
import io.reactivex.Single

interface PasswordRepository {
    fun getPassword(wallet: Wallet): Single<String>

    fun setPassword(wallet: Wallet, password: String): Completable

    fun generatePassword(): Single<String>

    fun passwordVerification(wallet: Wallet, masterPassword: String): Single<Wallet>
}
