package com.kyberswap.android.util

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.util.ext.isNetworkAvailable
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class ErrorHandler @Inject constructor(private val context: Context) {

    fun getError(e: Throwable): String {
        return if (!context.isNetworkAvailable()) {
            context.getString(R.string.no_internet_connect)
        } else if (e is HttpException) {
            context.getString(R.string.error_http_exception)
        } else if (e is UnknownHostException) {
            context.getString(R.string.error_unknown_host)
        } else if (e is ConnectException || e is SocketTimeoutException || e is TimeoutException) {
            context.getString(R.string.error_http_exception)
        } else {
            context.getString(R.string.something_wrong)
        }
    }
}
