package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.TransactionApi
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.data.mapper.TransactionMapper
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject


class TransactionDataRepository @Inject constructor(
    private val transactionApi: TransactionApi,
    private val transactionDao: TransactionDao,
    private val transactionMapper: TransactionMapper
) : TransactionRepository {
    override fun fetchERC20TokenTransactions(address: String): Single<List<Transaction>> {
        Timber.e("fetchERC20TokenTransactions")
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            TOKEN_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY
        )
            .map {
                it.result
            }
            .map {
                transactionMapper.transform(it, address, TOKEN_TRANSACTION)
            }
    }

    override fun fetchInternalTransactions(address: String): Single<List<Transaction>> {
        Timber.e("fetchInternalTransactions")
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            INTERNAL_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY
        )
            .map {
                it.result
            }
            .map {
                transactionMapper.transform(
                    it,
                    Transaction.TransactionType.RECEIVED,
                    INTERNAL_TRANSACTION
                )
            }
    }

    override fun fetchNormalTransaction(address: String): Single<List<Transaction>> {
        Timber.e("fetchNormalTransaction")
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            NORMAL_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY
        )
            .map {
                it.result
            }
            .map {
                transactionMapper.transform(
                    it,
                    Transaction.TransactionType.SEND,
                    NORMAL_TRANSACTION
                )
            }
    }

    override fun fetchAllTransactions(address: String): Flowable<List<Transaction>> {
        return getTransactions(address)

    }

    private fun getTransactions(address: String): Flowable<List<Transaction>> {
        val sendTransaction = fetchNormalTransaction(address)
            .toFlowable().flatMapIterable { transactions ->
                transactions
            }
            .filter {
                it.value.toBigDecimalOrDefaultZero() > BigDecimal.ZERO &&
                    it.from == address
            }.map {
                it.copy(
                    tokenSymbol = Token.ETH,
                    tokenName = Token.ETH_NAME,
                    tokenDecimal = Token.ETH_DECIMAL.toString()
                )
            }
            .toList()
        val receivedTransaction = fetchInternalTransactions(address)
        val erc20Transaction = fetchERC20TokenTransactions(address)
        return Flowable.mergeDelayError(
            transactionDao.all,
            Singles.zip(
                sendTransaction,
                receivedTransaction,
                erc20Transaction
            )
            { send, received, erc20 ->
                send.union(received).union(erc20)
            }
                .map {
                    it.groupBy { transaction -> transaction.hash }
                }
                .map {
                    val transactionList = mutableListOf<Transaction>()
                    for ((_, transactions) in it) {
                        if (transactions.size == 2) {
                            val send = transactions.find {
                                it.type == Transaction.TransactionType.SEND
                            }

                            val received = transactions.find {
                                it.type == Transaction.TransactionType.RECEIVED
                            }

                            val sourceAmount = send?.value.toBigDecimalOrDefaultZero()
                                .divide(
                                    10.toBigDecimal()
                                        .pow(
                                            (send?.tokenDecimal ?: Token.ETH_DECIMAL.toString())
                                                .toBigDecimalOrDefaultZero().toInt()
                                        )
                                )

                            val destAmount = received?.value.toBigDecimalOrDefaultZero()
                                .divide(
                                    10.toBigDecimal()
                                        .pow(
                                            (received?.tokenDecimal ?: Token.ETH_DECIMAL.toString())
                                                .toBigDecimalOrDefaultZero().toInt()
                                        )
                                )
                            val transaction = transactions.first().copy(
                                tokenSource = send?.tokenSymbol ?: "",
                                sourceAmount = sourceAmount.toDisplayNumber(),
                                tokenDest = received?.tokenSymbol ?: "",
                                destAmount = destAmount.toDisplayNumber()

                            )
                            transactionList.add(transaction)

                        } else {
                            transactionList.addAll(transactions)
                        }
                    }
                    transactionList.toList()
                }.doAfterSuccess {
                    Timber.e("transactionDao.insertTransactionBatch(it)")
                    transactionDao.insertTransactionBatch(it)
                }
                .toFlowable()
        )

    }

    companion object {
        const val DEFAULT_MODULE = "account"
        const val NORMAL_TRANSACTION = "txlist"
        const val INTERNAL_TRANSACTION = "txlistinternal"
        const val TOKEN_TRANSACTION = "tokentx"
        const val DEFAULT_SORT = "desc"
        const val API_KEY = "7V3E6JSF7941JCB6448FNRI3FSH9HI7HYH"
    }

}