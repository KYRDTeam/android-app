package com.kyberswap.android.data.repository.datasource.storage

import com.kyberswap.android.domain.model.Credential
import com.kyberswap.android.domain.model.LoginSession
import javax.inject.Inject

class StorageMediator @Inject constructor(private val hawkWrapper: HawkWrapper) {
    companion object {
        private const val KEY_AUTHENTICATION = "authentication"
        private const val KEY_CREDENTIAL = "credential"
        private const val KEY_SHOWN_POST_GUIDE = "post_guide"
    }

    fun setAuthentication(loginSession: LoginSession): Boolean {
        return hawkWrapper.putItem(KEY_AUTHENTICATION, loginSession)
    }

    fun isAuthenticated(): Boolean {
        val loginSession = hawkWrapper.getItem(KEY_AUTHENTICATION, LoginSession::class.java)
        return loginSession != null &&
            loginSession.createdAt + loginSession.expiresIn > System.currentTimeMillis() / 1000
    }

    fun getAuthentication(): LoginSession? {
        return hawkWrapper.getItem(KEY_AUTHENTICATION, LoginSession::class.java)
    }

    fun saveCredential(credential: Credential): Boolean {
        return hawkWrapper.putItem(KEY_CREDENTIAL, credential)
    }

    fun getCredential(): Credential? {
        return hawkWrapper.getItem(KEY_CREDENTIAL, Credential::class.java)
    }

    fun clearAuthenticationInformation() {
        hawkWrapper.remove(KEY_CREDENTIAL)
        hawkWrapper.remove(KEY_AUTHENTICATION)
    }

    fun isShownPostGuide(): Boolean {
        return hawkWrapper[KEY_SHOWN_POST_GUIDE, false]
    }

    fun savePostGuide(isShownPostGuide: Boolean) {
        hawkWrapper.put(KEY_SHOWN_POST_GUIDE, isShownPostGuide)
    }
}