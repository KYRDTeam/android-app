package com.kyberswap.android.data.repository.datasource.storage

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.orhanobut.hawk.Hawk
import timber.log.Timber
import javax.inject.Inject

class HawkWrapper @Inject constructor() {
    val gson: Gson = Gson()
    fun remove(key: String): Boolean {
        return Hawk.delete(key)
    }

    operator fun contains(key: String): Boolean {
        return Hawk.contains(key)
    }

    operator fun get(key: String, defValue: String?): String? {
        return Hawk.get<String>(key, defValue)
    }

    operator fun get(key: String, defValue: Int): Int {
        return Hawk.get(key, defValue)
    }

    operator fun get(key: String, defValue: Long): Long {
        return Hawk.get(key, defValue)
    }

    operator fun get(key: String, defValue: Float): Float {
        return Hawk.get(key, defValue)
    }

    operator fun get(key: String, defValue: Boolean): Boolean {
        return Hawk.get(key, defValue)
    }

    fun <T> getItem(key: String, classOfT: Class<T>): T? {
        return getItem(key, null, classOfT)
    }

    fun <T> getItem(key: String, defValue: T?, classOfT: Class<T>): T? {
        if (!Hawk.contains(key)) {
            return defValue

        val json = Hawk.get<String>(key)
        return try {
            gson.fromJson(json, classOfT)
 catch (e: JsonSyntaxException) {
            Timber.e(e)
            defValue

    }

    fun put(key: String, value: String?): Boolean {
        return Hawk.put<String>(key, value)
    }

    fun put(key: String, value: Int): Boolean {
        return Hawk.put(key, value)
    }

    fun put(key: String, value: Long): Boolean {
        return Hawk.put(key, value)
    }

    fun put(key: String, value: Float): Boolean {
        return Hawk.put(key, value)
    }

    fun put(key: String, value: Boolean): Boolean {
        return Hawk.put(key, value)
    }

    fun <T> putItem(key: String, t: T): Boolean {
        return Hawk.put(key, gson.toJson(t))
    }
}
