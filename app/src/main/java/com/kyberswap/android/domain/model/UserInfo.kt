package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.UserInfoEntity


data class UserInfo(
    val activeWallets: List<String> = listOf(),
    val avatarUrl: String = "",
    val contactId: String = "",
    val contactType: String = "",
    val kycStatus: String = "",
    val kycStep: Int = 0,
    val name: String = "",
    val uid: Int = 0
) {
    constructor(entity: UserInfoEntity) : this(
        entity.activeWallets,
        entity.avatarUrl,
        entity.contactId,
        entity.contactType,
        entity.kycStatus,
        entity.kycStep,
        entity.name,
        entity.uid
    )
}