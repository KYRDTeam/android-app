package com.kyberswap.android.presentation.main.profile

import com.kyberswap.android.domain.model.UserInfo

sealed class UserInfoState {
    object Loading : UserInfoState()
    class ShowError(val message: String?) : UserInfoState()
    class Success(val userInfo: UserInfo?) : UserInfoState()
}
