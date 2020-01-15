package com.kyberswap.android.data.api.user


import com.google.gson.annotations.SerializedName

data class LoginUserEntity(
    @SerializedName("auth_info")
    val authInfo: AuthInfoEntity = AuthInfoEntity(),
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("user_info")
    val userInfo: UserInfoEntity = UserInfoEntity(),
    @SerializedName("message")
    val message: String = "",
    @SerializedName("confirm_signup_required")
    val confirmSignUpRequired: Boolean = false,
    @SerializedName("2fa_required")
    val twoFaRequired: Boolean = false

)