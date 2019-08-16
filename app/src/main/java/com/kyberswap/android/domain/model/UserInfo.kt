package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Embedded
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
    var uid: Long = 0,
    @Embedded(prefix = "kyc_")
    var kycInfo: KycInfo = KycInfo(),
    var blockReason: String? = ""
) : Parcelable {
    constructor(entity: UserInfoEntity) : this(
        entity.activeWallets,
        entity.avatarUrl ?: "",
        entity.contactId ?: "",
        entity.contactType ?: "",
        entity.kycStatus ?: "",
        entity.kycStep,
        entity.name ?: "",
        entity.uid,
        KycInfo(entity.kycInfo),
        entity.blockReason ?: ""
    )

    val isKycReject: Boolean
        get() = kycStatus == "rejected"

    companion object {
        const val DRAFT = "draft"
        const val PENDING = "pending"
        const val REJECT = "rejected"
        const val BLOCK = "blocke"
        const val BLOCKED = "blocked"
        const val APPROVED = "approved"
        const val KYC_STEP_PERSONAL_INFO = 1
        const val KYC_STEP_ID_PASSPORT = 2
        const val KYC_STEP_SUBMIT = 3
    }
}