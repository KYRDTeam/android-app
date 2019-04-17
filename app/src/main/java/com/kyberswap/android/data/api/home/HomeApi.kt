package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.home.entity.ArticlesEntity
import com.kyberswap.android.data.api.home.entity.HeaderEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApi {

    @GET("headers")
    fun getHeaders(@Query("type") type: String): Single<HeaderEntity>

    @GET("articles/top/hairsalon/{page}")
    fun getTopArticles(@Path("page") page: Int): Single<ArticlesEntity>

    @GET("articles/features/hairsalon/{featureArticleId}/{page}")
    fun getFeatureArticles(
        @Path("featureArticleId") featureArticleId: Long,
        @Path("page") page: Int
    ): Single<ArticlesEntity>
}