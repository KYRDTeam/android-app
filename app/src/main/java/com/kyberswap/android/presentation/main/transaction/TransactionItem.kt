package com.kyberswap.android.presentation.main.transaction

import com.kyberswap.android.domain.model.Transaction


sealed class TransactionItem {
    class Header(val date: String?) : TransactionItem()
    class ItemEven(val transaction: Transaction) : TransactionItem()
    class ItemOdd(val transaction: Transaction) : TransactionItem()
}
