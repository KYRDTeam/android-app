package com.kyberswap.android.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.TransactionApi
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.data.db.TransactionFilterDao
import com.kyberswap.android.data.mapper.TransactionMapper
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.transaction.GetTransactionFilterUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsPeriodicallyUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsUseCase
import com.kyberswap.android.domain.usecase.transaction.MonitorPendingTransactionUseCase
import com.kyberswap.android.domain.usecase.transaction.SaveTransactionFilterUseCase
import com.kyberswap.android.domain.usecase.transaction.TransactionsData
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toLongSafe
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.Singles
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max

class TransactionDataRepository @Inject constructor(
    private val transactionApi: TransactionApi,
    private val transactionDao: TransactionDao,
    private val transactionMapper: TransactionMapper,
    private val tokenClient: TokenClient,
    private val tokenDao: TokenDao,
    private val swapDao: SwapDao,
    private val sendDao: SendDao,
    private val limitOrderDao: LocalLimitOrderDao,
    private val transactionFilterDao: TransactionFilterDao,
    private val context: Context
) : TransactionRepository {

    override fun monitorPendingTransactionsPolling(param: MonitorPendingTransactionUseCase.Param): Flowable<List<Transaction>> {
        return Flowable.fromCallable {
            tokenClient.monitorPendingTransactions(param.transactions)
        }
            .doAfterNext { pendingTransactions ->
                pendingTransactions.forEach { tx ->
                    val transaction =
                        transactionDao.findTransaction(
                            tx.hash.toLowerCase(Locale.getDefault()),
                            Transaction.PENDING_TRANSACTION_STATUS
                        )
                    transaction?.let {
                        if (tx.blockNumber.toLongSafe() > 0) {
                            if (tx.blockNumber.toLongSafe() == Transaction.DEFAULT_DROPPED_BLOCK_NUMBER) {
                                sendNotification(tx, true)
                            }
                            transactionDao.delete(it)
                            updateBalance(it, param.wallet)
                        }
                    }
                }
            }
            .repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    private fun updateBalance(transaction: Transaction, wallet: Wallet) {
        if (transaction.tokenSource.isNotBlank()) {
            val tokenSource = tokenDao.getTokenBySymbol(transaction.tokenSource)
            tokenSource?.let { src ->
                updateTokenBalance(src, wallet)

            }
        }

        if (transaction.tokenDest.isNotBlank()) {
            val tokenDest = tokenDao.getTokenBySymbol(transaction.tokenDest)
            tokenDest?.let { dest ->
                updateTokenBalance(dest, wallet)
            }
        }

        if (transaction.tokenSymbol.isNotBlank() && ((transaction.tokenSymbol != transaction.tokenSource) ||
                (transaction.tokenSymbol != transaction.tokenDest))
        ) {
            val tokenSymbol = tokenDao.getTokenBySymbol(transaction.tokenSymbol)
            tokenSymbol?.let { symbol ->
                updateTokenBalance(symbol, wallet)
            }
        }

        if (!(transaction.tokenSource == Token.ETH_SYMBOL
                || transaction.tokenDest == Token.ETH_SYMBOL
                || transaction.tokenSymbol == Token.ETH_SYMBOL)
        ) {
            val tokenSymbol = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
            tokenSymbol?.let { symbol ->
                updateTokenBalance(symbol, wallet)
            }
        }
    }

    private fun updateTokenBalance(token: Token, wallet: Wallet) {
        val updatedBalanceToken = tokenClient.updateBalance(token)
        tokenDao.updateToken(updatedBalanceToken)

        val swapByAddress = swapDao.findSwapByAddress(wallet.address)
        swapByAddress?.let { swap ->
            if (swap.tokenSource.tokenSymbol == updatedBalanceToken.tokenSymbol) {
                swapDao.updateSwap(swap.copy(tokenSource = updatedBalanceToken))
            } else if (swap.tokenDest.tokenSymbol == updatedBalanceToken.tokenSymbol) {
                swapDao.updateSwap(swap.copy(tokenDest = updatedBalanceToken))
            }
        }

        val sendByAddress = sendDao.findSendByAddress(wallet.address)
        sendByAddress?.let { send ->
            if (send.tokenSource.tokenSymbol == updatedBalanceToken.tokenSymbol) {
                sendDao.updateSend(send.copy(tokenSource = updatedBalanceToken))
            }

        }

        val orderByAddress =
            limitOrderDao.findLocalLimitOrderByAddress(wallet.address)
        orderByAddress?.let { order ->
            when {
                order.tokenSource.tokenSymbol == updatedBalanceToken.tokenSymbol -> limitOrderDao.updateOrder(
                    order.copy(
                        tokenSource = order.tokenSource.updateBalance(
                            updatedBalanceToken.currentBalance
                        )
                    )
                )

                order.tokenDest.tokenSymbol == updatedBalanceToken.tokenSymbol -> limitOrderDao.updateOrder(
                    order.copy(
                        tokenDest = order.tokenDest.updateBalance(updatedBalanceToken.currentBalance)
                    )
                )

                order.tokenSource.tokenSymbol == Token.ETH_SYMBOL_STAR && updatedBalanceToken.tokenSymbol == Token.ETH_SYMBOL -> {
                    val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)
                    val ethBalance = updatedBalanceToken.currentBalance
                    val wethBalance = wethToken?.currentBalance ?: BigDecimal.ZERO

                    limitOrderDao.updateOrder(
                        order.copy(
                            tokenSource = order.tokenSource.updateBalance(
                                ethBalance.plus(wethBalance)
                            ),
                            wethToken = wethToken ?: order.wethToken,
                            ethToken = updatedBalanceToken
                        )
                    )
                }
                order.tokenDest.tokenSymbol == Token.ETH_SYMBOL_STAR && updatedBalanceToken.tokenSymbol == Token.ETH_SYMBOL -> {
                    val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)
                    val ethBalance = updatedBalanceToken.currentBalance
                    val wethBalance = wethToken?.currentBalance ?: BigDecimal.ZERO

                    limitOrderDao.updateOrder(
                        order.copy(
                            tokenDest = order.tokenDest.updateBalance(
                                ethBalance.plus(wethBalance)
                            ),
                            wethToken = wethToken ?: order.wethToken,
                            ethToken = updatedBalanceToken
                        )
                    )
                }
                order.tokenSource.tokenSymbol == Token.ETH_SYMBOL_STAR && updatedBalanceToken.tokenSymbol == Token.WETH_SYMBOL -> {
                    val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
                    val wethBalance = updatedBalanceToken.currentBalance
                    val ethBalance = ethToken?.currentBalance ?: BigDecimal.ZERO

                    limitOrderDao.updateOrder(
                        order.copy(
                            tokenSource = order.tokenSource.updateBalance(
                                ethBalance.plus(wethBalance)
                            ),
                            ethToken = ethToken ?: order.ethToken,
                            wethToken = updatedBalanceToken
                        )
                    )
                }
                order.tokenDest.tokenSymbol == Token.ETH_SYMBOL_STAR && updatedBalanceToken.tokenSymbol == Token.WETH_SYMBOL -> {
                    val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
                    val wethBalance = updatedBalanceToken.currentBalance
                    val ethBalance = ethToken?.currentBalance ?: BigDecimal.ZERO

                    limitOrderDao.updateOrder(
                        order.copy(
                            tokenDest = order.tokenDest.updateBalance(
                                ethBalance.plus(wethBalance)
                            ),
                            ethToken = ethToken ?: order.ethToken,
                            wethToken = updatedBalanceToken
                        )
                    )
                }
            }
        }
    }

    override fun fetchPendingTransaction(address: String): Flowable<List<Transaction>> {
        return transactionDao.getTransactionByStatus(
            address,
            Transaction.PENDING_TRANSACTION_STATUS
        ).map { txs ->
            txs.map {
                it.apply {
                    currentAddress = address
                }
            }
        }
    }

    override fun fetchERC20TokenTransactions(
        wallet: Wallet,
        startBlock: Long
    ): Single<List<Transaction>> {
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            TOKEN_TRANSACTION,
            wallet.address,
            DEFAULT_SORT,
            API_KEY,
            startBlock
        )
            .map {
                transactionMapper.transform(it.result, wallet.address, TOKEN_TRANSACTION)
            }.doAfterSuccess {
                val tokenAddress = tokenDao.allTokens.filter {
                    !it.isOther
                }.map {
                    it.tokenAddress.toLowerCase(Locale.getDefault())
                }
                val otherTokenList = it.filterNot { tx ->
                    tokenAddress.contains(tx.contractAddress.toLowerCase(Locale.getDefault()))
                }.map { tx ->
                    Token(tx).copy(isOther = true).updateSelectedWallet(wallet)
                }.filter {
                    it.tokenName.isNotEmpty()
                }
                tokenDao.insertTokens(otherTokenList)
            }
    }

    override fun fetchInternalTransactions(
        address: String,
        startBlock: Long
    ): Single<List<Transaction>> {
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            INTERNAL_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY,
            startBlock
        )
            .map {
                it.result
            }
            .map {
                transactionMapper.transform(
                    it,
                    Transaction.TransactionType.RECEIVED,
                    INTERNAL_TRANSACTION,
                    address
                )
            }
    }

    override fun fetchNormalTransaction(
        address: String,
        startBlock: Long
    ): Single<List<Transaction>> {
        return transactionApi.getTransaction(
            DEFAULT_MODULE,
            NORMAL_TRANSACTION,
            address,
            DEFAULT_SORT,
            API_KEY,
            startBlock
        )
            .map {
                it.result
            }
            .map {
                transactionMapper.transform(
                    it,
                    Transaction.TransactionType.SEND,
                    NORMAL_TRANSACTION,
                    address
                )
            }
    }

    override fun fetchAllTransactions(param: GetTransactionsUseCase.Param): Flowable<TransactionsData> {
        return getTransactions(param.wallet, param.isForceRefresh)
    }

    override fun fetchTransactionPeriodically(param: GetTransactionsPeriodicallyUseCase.Param): Flowable<List<Transaction>> {
        return Flowable.fromCallable {
            transactionDao.getLatestTransaction(param.wallet.address)?.blockNumber?.toLongSafe()
                ?: 1
        }
            .flatMap { latestBlockNumber ->
                getTransactionRemote(param.wallet, max(latestBlockNumber - 10, 1))
            }.doOnNext {
                val latestTransaction = transactionDao.getLatestTransaction(param.wallet.address)
                val latestBlock =
                    latestTransaction?.blockNumber?.toLongSafe() ?: 1
                it.forEach { tx ->
                    val blockNumber = tx.blockNumber.toLongSafe()
                    latestTransaction?.let {
                        if (blockNumber > latestBlock) {
                            if (tx.isReceived(
                                    param.wallet.address.toLowerCase(
                                        Locale.getDefault()
                                    )
                                )
                            ) {
                                sendNotification(tx)
                            }
                            updateBalance(tx, param.wallet)
                        }
                    }
                }
                transactionDao.insertTransactionBatch(it)

            }
            .repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    private fun sendNotification(transaction: Transaction, isDropped: Boolean = false) {
        if (Date().time / 1000 - transaction.timeStamp <= 5 * 60) {

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse(context.getString(R.string.transaction_etherscan_endpoint_url) + transaction.hash)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            if (!transaction.isTransactionFail || isDropped) {
                val title: String =
                    if (isDropped) context.getString(R.string.notification_dropped) else String.format(
                        context.getString(R.string.notification_received_token),
                        transaction.tokenSymbol
                    )
                val message: String = if (isDropped) String.format(
                    context.getString(R.string.notification_dropped_message),
                    transaction.hash
                ) else String.format(
                    context.getString(R.string.notification_success_received),
                    transaction.displayValue,
                    transaction.from.displayWalletAddress()
                )

                val channelId = context.getString(R.string.default_notification_channel_id)
                val defaultSoundUri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setColor(ContextCompat.getColor(context, R.color.notification_background))
                    .setContentTitle(title)
                    .setContentText(
                        message
                    )
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(message)
                    )
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                notificationManager.notify(
                    Numeric.toBigInt(transaction.hash).toInt(),
                    notificationBuilder.build()
                )
            }
        }
    }

    private fun composedTransaction(
        send: Transaction,
        received: Transaction,
        transactions: List<Transaction>,
        wallet: Wallet,
        addressToSymbolMap: Map<String, String>
    ): Transaction {
        val sourceAmount = send.value.toBigDecimalOrDefaultZero()
            .divide(
                BigDecimal.TEN
                    .pow(
                        send.tokenDecimal
                            .toBigDecimalOrDefaultZero().toInt()
                    ),
                18,
                RoundingMode.HALF_EVEN
            )

        val destAmount = received.value.toBigDecimalOrDefaultZero()
            .divide(
                BigDecimal.TEN
                    .pow(
                        received.tokenDecimal
                            .toBigDecimalOrDefaultZero().toInt()
                    )
                , 18,
                RoundingMode.HALF_EVEN
            )
        val tx =
            if (transactions.first().gasPrice.isEmpty()) transactions.last() else transactions.first()

        return tx.copy(
            tokenSource = addressToSymbolMap[send.contractAddress] ?: send.tokenSymbol,
            from = send.from,
            to = received.to,
            sourceAmount = sourceAmount.toDisplayNumber(),
            tokenDest = addressToSymbolMap[received.contractAddress] ?: received.tokenSymbol,
            destAmount = destAmount.toDisplayNumber(),
            walletAddress = wallet.address
        )
    }

    private fun findTransactionPair(transactions: List<Transaction>): Pair<Transaction?, Transaction?> {
        val send = transactions.find {
            it.type == Transaction.TransactionType.SEND
        }

        val received = transactions.find {
            it.type == Transaction.TransactionType.RECEIVED
        }

        return Pair(send, received)
    }

    private fun getTransactionRemote(
        wallet: Wallet,
        startBlock: Long = 1
    ): Flowable<List<Transaction>> {
        val sendTransaction = fetchNormalTransaction(wallet.address, startBlock)
            .toFlowable().flatMapIterable { transactions ->
                transactions
            }
            .filter {
                it.value.toBigDecimalOrDefaultZero() > BigDecimal.ZERO || it.isTransactionFail
            }.map {
                it.copy(
                    tokenSymbol = Token.ETH,
                    tokenName = Token.ETH_NAME,
                    tokenDecimal = Token.ETH_DECIMAL.toString()
                )
            }
            .toList()
        val receivedTransaction = fetchInternalTransactions(wallet.address, startBlock)
            .toFlowable().flatMapIterable { transactions ->
                transactions
            }
            .map {
                it.copy(
                    tokenSymbol = Token.ETH,
                    tokenName = Token.ETH_NAME,
                    tokenDecimal = Token.ETH_DECIMAL.toString()
                )
            }.toList()

        val erc20Transaction = fetchERC20TokenTransactions(wallet, startBlock)

        return Singles.zip(
            sendTransaction,
            receivedTransaction,
            erc20Transaction
        )
        { send, received, erc20 ->
            val transactions = send.toMutableList()
            transactions.addAll(received)
            transactions.addAll(erc20)
            transactions.toList().sortedByDescending { it.timeStamp }
        }
            .map {
                it.groupBy { transaction -> transaction.hash }
            }
            .map {
                val transactionList = mutableListOf<Transaction>()
                val addressToSymbolMap = tokenDao.allTokens.map {
                    it.tokenAddress to it.tokenSymbol
                }.toMap()

                for ((_, transactions) in it) {
                    if (transactions.size == 2) {
                        if (transactions.first() == transactions.last()) {
                            val last = transactions.last()
                            transactionList.add(
                                last.copy(
                                    walletAddress = wallet.address,
                                    type = Transaction.TransactionType.SEND,
                                    tokenSymbol = addressToSymbolMap[last.contractAddress]
                                        ?: last.tokenSymbol
                                )
                            )
                        } else {
                            val pair = findTransactionPair(transactions)
                            val send = pair.first
                            val received = pair.second

                            if (send != null && received != null) {
                                val transaction =
                                    composedTransaction(
                                        send,
                                        received,
                                        transactions,
                                        wallet,
                                        addressToSymbolMap
                                    )
                                transactionList.add(
                                    transaction.copy(
                                        type = if (transaction.isTransfer)
                                            transaction.type
                                        else Transaction.TransactionType.SWAP
                                    )
                                )
                            } else {
                                transactionList.add(

                                    transactions.last().copy(
                                        walletAddress = wallet.address,
                                        tokenSymbol = addressToSymbolMap[transactions.last().contractAddress]
                                            ?: transactions.last().tokenSymbol
                                    )
                                )

                                transactionList.add(
                                    transactions.first().copy(
                                        walletAddress = wallet.address,
                                        tokenSymbol = addressToSymbolMap[transactions.first().contractAddress]
                                            ?: transactions.first().tokenSymbol
                                    )
                                )
                            }
                        }
                    } else if (transactions.size > 2) {
                        val pair = findTransactionPair(transactions)
                        val send = pair.first
                        val received = pair.second

                        if (send != null && received != null) {

                            val transaction =
                                composedTransaction(
                                    send,
                                    received,
                                    transactions,
                                    wallet,
                                    addressToSymbolMap
                                )

                            transactionList.add(
                                transaction.copy(
                                    type = if (transaction.isTransfer)
                                        transaction.type
                                    else Transaction.TransactionType.SWAP
                                )
                            )

                            val remainingTransactions = transactions.toMutableList()
                            remainingTransactions.remove(send)
                            remainingTransactions.remove(received)
                            if (remainingTransactions.size > 0) {
                                transactionList.addAll(remainingTransactions.map { tx ->
                                    tx.copy(
                                        walletAddress = wallet.address,
                                        type = if (tx.isTransfer)
                                            tx.type
                                        else Transaction.TransactionType.SWAP,
                                        tokenSymbol = addressToSymbolMap[tx.contractAddress]
                                            ?: tx.tokenSymbol
                                    )
                                })
                            }
                        } else {
                            transactionList.addAll(transactions.map { tx ->
                                tx.copy(
                                    walletAddress = wallet.address,
                                    type = if (tx.isTransfer)
                                        tx.type
                                    else Transaction.TransactionType.SWAP,
                                    tokenSymbol = addressToSymbolMap[tx.contractAddress]
                                        ?: tx.tokenSymbol
                                )
                            })
                        }
                    } else {
                        transactionList.addAll(transactions.map { tx ->
                            tx.copy(
                                walletAddress = wallet.address,
                                type = if (tx.isTransfer)
                                    tx.type
                                else Transaction.TransactionType.SWAP,
                                tokenSymbol = addressToSymbolMap[tx.contractAddress]
                                    ?: tx.tokenSymbol
                            )
                        })
                    }
                }
                transactionList.toList()
            }.toFlowable()
    }

    private fun getTransactions(
        wallet: Wallet, isForceRefresh: Boolean
    ): Flowable<TransactionsData> {
        return Flowable.mergeDelayError(
            transactionDao.getCompletedTransactions(wallet.address).map {
                TransactionsData(it.map {
                    it.apply {
                        currentAddress = wallet.address
                    }
                }, false)
            },

            Flowable.fromCallable {
                transactionDao.getLatestTransaction(wallet.address) != null
            }.flatMap {
                if (it) {
                    Flowables.zip(
                        transactionDao.getCompletedTransactions(wallet.address),
                        Flowable.fromCallable {
                            transactionDao.getLatestTransaction(wallet.address)?.blockNumber?.toLongSafe()
                                ?: 1
                        }.flatMap {
                            getTransactionRemote(wallet, if (isForceRefresh) 1 else max(it - 10, 1))
                                .doOnNext {
                                    val latestTransaction =
                                        transactionDao.getLatestTransaction(wallet.address)
                                    val latestBlock =
                                        latestTransaction?.blockNumber?.toLongSafe() ?: 1
                                    it.forEach { tx ->
                                        val blockNumber = tx.blockNumber.toLongSafe()
                                        latestTransaction?.let {
                                            if (blockNumber > latestBlock) {
                                                if (tx.isReceived(
                                                        wallet.address.toLowerCase(
                                                            Locale.getDefault()
                                                        )
                                                    )
                                                ) {
                                                    sendNotification(tx)
                                                }
                                                updateBalance(tx, wallet)
                                            }
                                        }
                                    }
                                    if (isForceRefresh) {

                                        transactionDao.forceUpdateTransactionBatch(
                                            it,
                                            wallet.address
                                        )
                                    } else {
                                        transactionDao.insertTransactionBatch(it)
                                    }
                                }
                        }
                            .onErrorReturn { listOf() }

                    ) { local, remote ->

                        val transactionList = mutableListOf<Transaction>()
                        transactionList.addAll(remote)
                        transactionList.addAll(local)
                        transactionList
                    }.map {
                        TransactionsData(it.map {
                            it.apply {
                                currentAddress = wallet.address
                            }
                        }, true)
                    }
                } else {
                    Flowable.fromCallable {
                        transactionDao.getLatestTransaction(wallet.address)?.blockNumber?.toLongSafe()
                            ?: 1
                    }.flatMap {
                        getTransactionRemote(wallet, if (isForceRefresh) 1 else max(it - 10, 1))
                            .map {
                                TransactionsData(it.map {
                                    it.apply {
                                        currentAddress = wallet.address
                                    }
                                }, true)
                            }
                            .doOnNext {
                                val latestTransaction =
                                    transactionDao.getLatestTransaction(wallet.address)
                                val latestBlock =
                                    latestTransaction?.blockNumber?.toLongSafe() ?: 1
                                it.transactionList.forEach { tx ->
                                    val blockNumber = tx.blockNumber.toLongSafe()
                                    latestTransaction?.let {
                                        if (blockNumber > latestBlock) {
                                            if (tx.isReceived(
                                                    wallet.address.toLowerCase(
                                                        Locale.getDefault()
                                                    )
                                                )
                                            ) {
                                                sendNotification(tx)
                                            }
                                            updateBalance(tx, wallet)
                                        }
                                    }
                                }
                                if (isForceRefresh) {
                                    transactionDao.forceUpdateTransactionBatch(
                                        it.transactionList,
                                        wallet.address
                                    )
                                } else {
                                    transactionDao.insertTransactionBatch(it.transactionList)
                                }
                            }
                    }
                }

            }

        )
    }

    override fun saveTransactionFilter(param: SaveTransactionFilterUseCase.Param): Completable {
        return Completable.fromCallable {
            transactionFilterDao.updateTrasactionFilter(param.transactionFilter)
        }
    }

    override fun getTransactionFilter(param: GetTransactionFilterUseCase.Param): Flowable<TransactionFilter> {
        return Flowable.fromCallable {
            when (val filter =
                transactionFilterDao.findTransactionFilterByAddress(param.walletAddress)) {
                null -> {
                    tokenDao.allTokens.map {
                        it.tokenSymbol
                    }
                    val newFilter = TransactionFilter(
                        walletAddress = param.walletAddress,
                        types = listOf(
                            Transaction.TransactionType.SWAP,
                            Transaction.TransactionType.SEND,
                            Transaction.TransactionType.RECEIVED
                        ),
                        tokens = tokenDao.allTokens.map {
                            it.tokenSymbol
                        }
                    )
                    transactionFilterDao.insertTransactionFilter(newFilter)
                    newFilter
                }
                else -> filter
            }

        }.flatMap {
            transactionFilterDao.findTransactionFilterByAddressFlowable(param.walletAddress)
                .defaultIfEmpty(it)
        }
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