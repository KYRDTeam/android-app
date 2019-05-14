package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.BuildConfig
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.CurrencyApi
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.util.TokenClient
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val client = OkHttpClient().newBuilder()
        client.addInterceptor {
            val original = it.request()
            val builder = original.newBuilder()
            val request = builder.method(original.method(), original.body())
                .build()
            it.proceed(request)

        if (BuildConfig.DEBUG) {
            client.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
    )

        return client.build()
    }

    @Provides
    @Singleton
    fun provideHomeApi(context: Context, client: OkHttpClient): TokenApi {
        return createApiClient(
            TokenApi::class.java,
            context.getString(R.string.token_endpoint_url),
            client
        )
    }

    @Provides
    @Singleton
    fun provideWeb3j(context: Context, client: OkHttpClient): Web3j {
        return Web3j.build(
            HttpService(
                context.getString(R.string.base_rpc_url),
                client,
                false
            )
        )
    }


    @Provides
    @Singleton
    fun provideRateApi(context: Context, client: OkHttpClient): SwapApi {
        return createApiClient(
            SwapApi::class.java,
            context.getString(R.string.rate_endpoint_url),
            client
        )
    }

    @Provides
    @Singleton
    fun provideChange24hApi(context: Context, client: OkHttpClient): CurrencyApi {
        return createApiClient(
            CurrencyApi::class.java,
            context.getString(R.string.currency_endpoint_url),
            client
        )
    }

    @Provides
    @Singleton
    fun provideTokenClient(web3j: Web3j): TokenClient {
        return TokenClient(web3j)
    }

    private fun <T> createApiClient(clazz: Class<T>, baseUrl: String, client: OkHttpClient): T {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(clazz)
    }
}
