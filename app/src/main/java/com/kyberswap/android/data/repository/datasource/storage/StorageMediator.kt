package com.kyberswap.android.data.repository.datasource.storage

import com.kyberswap.android.domain.model.AuthInfo
import javax.inject.Inject

class StorageMediator @Inject constructor(private val hawkWrapper: HawkWrapper) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "key_auth_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_EXPIRES_IN = "key_expiration_time"
        private const val KEY_INITIAL_FRESHCHAT = "key_initial_fresh_chat"
        private const val KEY_SHOW_BALANCE_TUTORIAL = "key_show_balance_tutorial"
        private const val KEY_SHOW_SWAP_TUTORIAL = "key_show_swap_tutorial"
        private const val KEY_SHOW_LIMIT_ORDER_TUTORIAL = "key_show_limit_order_tutorial"
    }

    fun showBalanceTutorial(isShow: Boolean) {
        hawkWrapper.put(KEY_SHOW_BALANCE_TUTORIAL, isShow)
    }

    fun isShownBalanceTutorial(): Boolean {
        return hawkWrapper[KEY_SHOW_BALANCE_TUTORIAL, false]
    }

    fun showSwapTutorial(isShow: Boolean) {
        hawkWrapper.put(KEY_SHOW_SWAP_TUTORIAL, isShow)
    }

    fun isShownSwapTutorial(): Boolean {
        return hawkWrapper[KEY_SHOW_SWAP_TUTORIAL, false]
    }

    fun showLimitOrderTutorial(isShow: Boolean) {
        hawkWrapper.put(KEY_SHOW_LIMIT_ORDER_TUTORIAL, isShow)
    }

    fun isShownLimitOrderTutorial(): Boolean {
        return hawkWrapper[KEY_SHOW_LIMIT_ORDER_TUTORIAL, false]
    }

    fun setInitialFreshChat(isInitial: Boolean) {
        hawkWrapper.put(KEY_INITIAL_FRESHCHAT, isInitial)
    }

    fun isInitialFreshChat(): Boolean {
        return hawkWrapper[KEY_INITIAL_FRESHCHAT, false]
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