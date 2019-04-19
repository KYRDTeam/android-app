package com.kyberswap.android.data.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.kyberswap.android.data.service.AccountService
import com.kyberswap.android.data.service.ServiceException
import com.kyberswap.android.domain.model.Wallet
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.ethereum.geth.*
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Wallet.create
import timber.log.Timber
import java.io.File
import java.math.BigInteger
import java.nio.charset.Charset

class GethKeystoreAccountService : AccountService {

    private val keyStore: KeyStore

    constructor(keyStoreFile: File) {
        keyStore = KeyStore(keyStoreFile.absolutePath, Geth.LightScryptN, Geth.LightScryptP)
    }

    constructor(keyStore: KeyStore) {
        this.keyStore = keyStore
    }

    override fun createAccount(password: String): Single<Wallet> {
        return Single.fromCallable {
            Wallet(
                keyStore.newAccount(password).address.hex.toLowerCase()
            )
        }
    }

    override fun importKeystore(
        store: String,
        password: String,
        newPassword: String
    ): Single<Wallet> {
        return Single.fromCallable {
            val account = keyStore
                .importKey(store.toByteArray(Charset.forName("UTF-8")), password, newPassword)
            Wallet(account.address.hex.toLowerCase())
        }
            .subscribeOn(Schedulers.io())
    }

    override fun importPrivateKey(privateKey: String, newPassword: String): Single<Wallet> {
        return Single.fromCallable {
            val key = BigInteger(privateKey, PRIVATE_KEY_RADIX)
            val keypair = ECKeyPair.create(key)
            val walletFile = create(newPassword, keypair, N, P)
            ObjectMapper().writeValueAsString(walletFile)
        }.compose { upstream -> importKeystore(upstream.blockingGet(), newPassword, newPassword) }
    }

    override fun exportAccount(
        wallet: Wallet,
        password: String,
        newPassword: String
    ): Single<String> {
        return Single
            .fromCallable<Account> { findAccount(wallet.address) }
            .flatMap { account1 ->
                Single.fromCallable {
                    String(
                        keyStore.exportKey(
                            account1,
                            password,
                            newPassword
                        )
                    )
                }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun deleteAccount(address: String, password: String): Completable {
        return Single.fromCallable<Account> { findAccount(address) }
            .flatMapCompletable { account ->
                Completable.fromAction {
                    keyStore.deleteAccount(
                        account,
                        password
                    )
                }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun signTransaction(
        signer: Wallet,
        signerPassword: String,
        toAddress: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        nonce: Long,
        data: ByteArray,
        chainId: Long
    ): Single<ByteArray> {
        return Single.fromCallable {
            val value = BigInt(0)
            value.setString(amount.toString(), 10)

            val gasPriceBI = BigInt(0)
            gasPriceBI.setString(gasPrice.toString(), 10)

            val gasLimitBI = BigInt(0)
            gasLimitBI.setString(gasLimit.toString(), 10)

            val tx = Transaction(
                nonce,
                Address(toAddress),
                value,
                gasLimitBI,
                gasPriceBI,
                data
            )

            val chain = BigInt(chainId) // Chain identifier of the main net
            val gethAccount = findAccount(signer.address)
            keyStore.unlock(gethAccount, signerPassword)
            val signed = keyStore.signTx(gethAccount, tx, chain)
            keyStore.lock(gethAccount.address)

            signed.encodeRLP()
        }
            .subscribeOn(Schedulers.io())
    }

    override fun hasAccount(address: String): Boolean {
        return keyStore.hasAddress(Address(address))
    }

    override fun fetchAccounts(): Single<Array<Wallet?>> {
        return Single.fromCallable<Array<Wallet?>> {
            val accounts = keyStore.accounts
            val len = accounts.size().toInt()
            val result = arrayOfNulls<Wallet>(len)

            for (i in 0 until len) {
                val gethAccount = accounts.get(i.toLong())
                result[i] = Wallet(gethAccount.address.hex.toLowerCase())
            }
            result
        }
            .subscribeOn(Schedulers.io())
    }

    @Throws(ServiceException::class)
    private fun findAccount(address: String): Account {
        val accounts = keyStore.accounts
        val len = accounts.size().toInt()
        for (i in 0 until len) {
            try {
                Timber.d("Address: %s", accounts.get(i.toLong()).address.hex)
                if (accounts.get(i.toLong()).address.hex.equals(address, ignoreCase = true)) {
                    return accounts.get(i.toLong())
                }
            } catch (ex: Exception) {
                /* Quietly: interest only result, maybe next is ok. */
            }

        }
        throw ServiceException("Wallet with address: $address not found")
    }

    companion object {
        private const val PRIVATE_KEY_RADIX = 16
        /**
         * CPU/Memory cost parameter. Must be larger than 1, a power of 2 and less than 2^(128 * r / 8).
         */
        private const val N = 1 shl 9
        /**
         * Parallelization parameter. Must be a positive integer less than or equal to Integer.MAX_VALUE / (128 * r * 8).
         */
        private const val P = 1
    }
}
