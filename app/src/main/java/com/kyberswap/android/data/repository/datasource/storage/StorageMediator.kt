package com.kyberswap.android.data.repository.datasource.storage

import javax.inject.Inject

class StorageMediator @Inject constructor(private val hawkWrapper: HawkWrapper) {
    companion object {
        private const val KEY_AUTHENTICATION = "authentication"
        private const val KEY_CREDENTIAL = "credential"
        private const val KEY_SHOWN_POST_GUIDE = "post_guide"
    }
}