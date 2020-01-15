package com.kyberswap.android.presentation.main.profile

import com.kyberswap.android.domain.model.DataTransferStatus
import com.kyberswap.android.domain.model.UserInfo

sealed class DataTransferState {
    object Loading : DataTransferState()
    class ShowError(val message: String?, val userInfo: UserInfo? = null) : DataTransferState()
    class Success(val status: DataTransferStatus, val userInfo: UserInfo? = null) :
        DataTransferState()
}
