package com.kyberswap.android.data.repository.datasource.local

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity
import java.util.Collections

class HeaderTypeConverter {
    private val gson = Gson()
    @TypeConverter
    fun stringToList(data: String?): List<ArticleFeatureEntity> {
        if (data == null) {
            return Collections.emptyList()


        val listType = object : TypeToken<List<ArticleFeatureEntity>>() {
.type

        return gson.fromJson<List<ArticleFeatureEntity>>(data, listType)
    }

    @TypeConverter
    fun listToString(someObjects: List<ArticleFeatureEntity>): String {
        return gson.toJson(someObjects)
    }
}