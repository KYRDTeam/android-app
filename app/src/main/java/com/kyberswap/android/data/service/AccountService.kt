package com.kyberswap.android.data.service

import com.kyberswap.android.domain.model.Wallet
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigInteger

interface AccountService {
    /**
     * Create account in keystore
     * @param password account password
     * @return new [Wallet]
     */
    fun createAccount(password: String): Single<Wallet>

    /**
     * Include new existing keystore
     * @param store store to include
     * @param password store password
     * @return included [Wallet] if success
     */
    fun importKeystore(
        store: String,
        password: String,
        newPassword: String
    ): Single<Wallet>

    fun importPrivateKey(privateKey: String, newPassword: String): Single<Wallet>

    /**
     * Export wallet to keystore
     * @param wallet wallet to export
     * @param password password from wallet
     * @param newPassword new password to store
     * @return store data
     */
    fun exportAccount(
        wallet: Wallet,
        password: String,
        newPassword: String
    ): Single<String>

    /**
     * Delete account from keystore
     * @param address account address
     * @param password account password
     */
    fun deleteAccount(address: String, password: String): Completable

    /**
     * Sign transaction
     * @param signer [Wallet]
     * @param signerPassword password from [Wallet]
     * @param toAddress transaction destination address
     * @param wei
     * @param nonce
     * @return sign data
     */
    fun signTransaction(
        signer: Wallet,
        signerPassword: String,
        toAddress: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        nonce: Long,
        data: ByteArray,
        chainId: Long
    ): Single<ByteArray>

    /**
     * Check if there is an address in the keystore
     * @param address [Wallet] address
     */
    fun hasAccount(address: String): Boolean

    /**
     * Return all [Wallet] from keystore
     * @return wallets
     */
    fun fetchAccounts(): Single<Array<Wallet?>>
}