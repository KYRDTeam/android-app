package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kyberswap.android.data.api.user.UserInfoEntity
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "users")
@Parcelize
data class UserInfo(
    @Ignore
    val activeWallets: List<String> = listOf(),
    var avatarUrl: String = "",
    var contactId: String = "",
    var contactType: String = "",
    var kycStatus: String = "",
    var kycStep: Int = 0,
    var name: String = "",
    @PrimaryKey
    var uid: Long = 0
) : Parcelable {
    constructor(entity: UserInfoEntity) : this(
        entity.activeWallets,
        entity.avatarUrl ?: "",
        entity.contactId ?: "",
        entity.contactType ?: "",
        entity.kycStatus ?: "",
        entity.kycStep,
        entity.name ?: "",
        entity.uid
    )
}