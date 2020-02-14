package com.kyberswap.android.presentation.common

interface PendingTransactionNotification {

    fun showPendingTxNotification(showNotification: Boolean)

    fun showUnReadNotification(showNotification: Boolean)
}