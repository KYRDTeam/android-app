package com.kyberswap.android.data.repository.datasource.storage

import com.kyberswap.android.domain.model.AuthInfo
import javax.inject.Inject

class StorageMediator @Inject constructor(private val hawkWrapper: HawkWrapper) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "key_auth_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_EXPIRES_IN = "key_expiration_time"
    }

    fun applyToken(authInfo: AuthInfo) {
        hawkWrapper.put(KEY_ACCESS_TOKEN, authInfo.authToken)
        hawkWrapper.put(KEY_REFRESH_TOKEN, authInfo.refreshToken)
        hawkWrapper.put(KEY_EXPIRES_IN, authInfo.expirationTime)
    }

    fun clearToken() {
        hawkWrapper.put(KEY_ACCESS_TOKEN, null)
        hawkWrapper.put(KEY_REFRESH_TOKEN, null)
        hawkWrapper.put(KEY_EXPIRES_IN, null)
    }

    fun getAccessToken(): String? = hawkWrapper[KEY_ACCESS_TOKEN, null]

    fun getStringValue(key: String, defaultValue: String): String? {
        return hawkWrapper[key, defaultValue]
    }

    fun setStringValue(key: String, value: String) {
        hawkWrapper.put(key, value)
    }

    fun setBooleanValue(key: String, value: Boolean) {
        hawkWrapper.put(key, value)
    }

    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
        return hawkWrapper[key, defaultValue]
    }
}