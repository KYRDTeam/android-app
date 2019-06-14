package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.TransactionApi
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.data.mapper.TransactionMapper
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsUseCase
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.Singles
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class TransactionDataRepository @Inject constructor(
    private val transactionApi: TransactionApi,
    private val transactionDao: TransactionDao,
    private val transactionMapper: TransactionMapper,
    private val tokenClient: TokenClient
) : TransactionRepository {
    override fun monitorPendingTransactionsPolling(transactions: List<Transaction>): Flowable<List<Transaction>> {
        return Flowable.fromCallable {
            val pendingTransactions = tokenClient.monitorPendingTransactions(transactions)
            transactionDao.insertTransactionBatch(pendingTransactions)
            pendingTransactions

            .repeatWhen {
                it.delay(10, TimeUnit.SECONDS)
    
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
    
    }

    override fun fetchPendingTransaction(address: String): Flowable<List<Transaction>> {
        return transactionDao.getTransactionByStatus(Transaction.PENDING_TRANSACTION_STATUS)
    }

    override fun fetchERC20TokenTransactions(address: String): Single<List<Transaction>> {
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            TOKEN_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY
        )
            .map {
                it.result
    
            .map {
                transactionMapper.transform(it, address, TOKEN_TRANSACTION)
    
    }

    override fun fetchInternalTransactions(address: String): Single<List<Transaction>> {
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            INTERNAL_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY
        )
            .map {
                it.result
    
            .map {
                transactionMapper.transform(
                    it,
                    Transaction.TransactionType.RECEIVED,
                    INTERNAL_TRANSACTION
                )
    
    }

    override fun fetchNormalTransaction(address: String): Single<List<Transaction>> {
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            NORMAL_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY
        )
            .map {
                it.result
    
            .map {
                transactionMapper.transform(
                    it,
                    Transaction.TransactionType.SEND,
                    NORMAL_TRANSACTION
                )
    
    }


    private fun <T> zipWithFlatMap(): FlowableTransformer<T, Long> {
        return FlowableTransformer { flowable ->
            flowable.zipWith(
                Flowable.range(COUNTER_START, ATTEMPTS),
                BiFunction<T, Int, Int> { _: T, u: Int -> u })
                .flatMap { t -> Flowable.timer(t * 5L, TimeUnit.SECONDS) }

    }

    override fun fetchAllTransactions(param: GetTransactionsUseCase.Param): Flowable<List<Transaction>> {
        if (param.transactionType == Transaction.PENDING) {
            return fetchPendingTransaction(param.walletAddress)

        return getTransactions(param.walletAddress)

    }

    private fun getTransactions(address: String): Flowable<List<Transaction>> {
        val sendTransaction = fetchNormalTransaction(address)
            .toFlowable().flatMapIterable { transactions ->
                transactions
    
            .filter {
                it.value.toBigDecimalOrDefaultZero() > BigDecimal.ZERO &&
                    it.from == address || it.isTransactionFail
    .map {
                it.copy(
                    tokenSymbol = Token.ETH,
                    tokenName = Token.ETH_NAME,
                    tokenDecimal = Token.ETH_DECIMAL.toString()
                )
    
            .toList()
        val receivedTransaction = fetchInternalTransactions(address)
            .toFlowable().flatMapIterable { transactions ->
                transactions
    
            .map {
                it.copy(
                    tokenSymbol = Token.ETH,
                    tokenName = Token.ETH_NAME,
                    tokenDecimal = Token.ETH_DECIMAL.toString()
                )
    .toList()

        val erc20Transaction = fetchERC20TokenTransactions(address)
        return Flowable.mergeDelayError(
            transactionDao.getCompletedTransactions(),
            Singles.zip(
                sendTransaction,
                receivedTransaction,
                erc20Transaction
            )
            { send, received, erc20 ->
                val transactions = send.toMutableList()
                transactions.addAll(received)
                transactions.addAll(erc20)
                transactions.toList().sortedByDescending { it.timeStamp.toLong() }
    
                .map {
                    it.groupBy { transaction -> transaction.hash }
        
                .map {
                    val transactionList = mutableListOf<Transaction>()
                    for ((_, transactions) in it) {
                        if (transactions.size == 2) {
                            val send = transactions.find {
                                it.type == Transaction.TransactionType.SEND
                    

                            val received = transactions.find {
                                it.type == Transaction.TransactionType.RECEIVED
                    

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
                            val tx =
                                if (transactions.first().gasPrice.isEmpty()) transactions.last() else transactions.first()

                            val transaction = tx.copy(
                                tokenSource = send?.tokenSymbol ?: "",
                                sourceAmount = sourceAmount.toDisplayNumber(),
                                tokenDest = received?.tokenSymbol ?: "",
                                destAmount = destAmount.toDisplayNumber()

                            )

                            transactionList.add(transaction)

                 else {
                            transactionList.addAll(transactions)
                
            
                    transactionList.toList()
        .doAfterSuccess {
                    transactionDao.insertTransactionBatch(it)
        
                .toFlowable()
        )

    }

    companion object {
        private const val COUNTER_START = 1
        private const val ATTEMPTS = 5
        const val DEFAULT_MODULE = "account"
        const val NORMAL_TRANSACTION = "txlist"
        const val INTERNAL_TRANSACTION = "txlistinternal"
        const val TOKEN_TRANSACTION = "tokentx"
        const val DEFAULT_SORT = "desc"
        const val API_KEY = "7V3E6JSF7941JCB6448FNRI3FSH9HI7HYH"
    }


}