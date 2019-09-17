package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.transaction.TransactionEntity
import com.kyberswap.android.domain.model.Transaction
import java.util.Locale
import javax.inject.Inject

class TransactionMapper @Inject constructor() {
    fun transform(
        entity: TransactionEntity,
        type: Transaction.TransactionType,
        txType: String
    ): Transaction {
        return Transaction(entity, type, txType)
    }


    fun transform(entity: TransactionEntity, address: String, txType: String): Transaction {
        return Transaction(entity, address, txType)
    }


    fun transform(
        entities: List<TransactionEntity>,
        type: Transaction.TransactionType,
        txType: String,
        walletAddress: String
    ): List<Transaction> {
        return entities.map {
            val transactionType =
                when {
                    walletAddress.toLowerCase(Locale.getDefault()) == it.from?.toLowerCase(Locale.getDefault()) -> Transaction.TransactionType.SEND
                    walletAddress.toLowerCase(Locale.getDefault()) == it.to?.toLowerCase(
                        Locale.getDefault()
                    ) -> Transaction.TransactionType.RECEIVED
                    else -> type
                }
            transform(it, transactionType, txType)
        }
    }


    fun transform(
        entities: List<TransactionEntity>,
        address: String,
        txType: String
    ): List<Transaction> {
        return entities.map {
            transform(it, address, txType)
        }
    }
}