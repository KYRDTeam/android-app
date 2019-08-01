package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.user.KycInfoEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class KycInfo(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val nationality: String = "",
    val country: String = "",
    val rejectReason: String? = "",
    val gender: Boolean = false,
    val dob: String = "",
    val profileCompleted: Boolean = false,
    val documentId: String = "",
    val documentType: String = "",
    val photoSelfie: String = "",
    val photoIdentityFrontSide: String = "",
    val artemisResponse: String = "",
    val duplicateId: String = "",
    val residentialAddress: String = "",
    val city: String = "",
    val zipCode: String = "",
    var documentProofAddress: String = "",
    val photoProofAddress: String = "",
    val sourceFund: String = "",
    val occupationCode: String = "",
    val industryCode: String = "",
    val taxResidencyCountry: String = "",
    val haveTaxIdentification: Boolean? = false,
    val taxIdentificationNumber: String = "",
    val documentIssueDate: String = "",
    val documentExpiryDate: String = "",
    val photoIdentityBackSide: String = "",
    val duplicateType: String = "",
    val internalApproval: String = "",
    val middleName: String = "",
    val customReason: String = "",
    val adminNote: String = "",
    val nativeFullName: String = "",
    val blockReason: String = "",
    val issueDateNonApplicable: Boolean = false,
    val expiryDateNonApplicable: Boolean = false
) : Parcelable {
    constructor(entity: KycInfoEntity) : this(
        entity.id ?: 0,
        entity.firstName ?: "",
        entity.lastName ?: "",
        entity.nationality ?: "",
        entity.country ?: "",
        entity.rejectReason ?: "",
        entity.gender ?: false,
        entity.dob ?: "",
        entity.profileCompleted ?: false,
        entity.documentId ?: "",
        entity.documentType ?: "",
        entity.photoSelfie ?: "",
        entity.photoIdentityFrontSide ?: "",
        entity.artemisResponse ?: "",
        entity.duplicateId ?: "",
        entity.residentialAddress ?: "",
        entity.city ?: "",
        entity.zipCode ?: "",
        entity.documentProofAddress ?: "",
        entity.photoProofAddress ?: "",
        entity.sourceFund ?: "",
        entity.occupationCode ?: "",
        entity.industryCode ?: "",
        entity.taxResidencyCountry ?: "",
        entity.haveTaxIdentification,
        entity.taxIdentificationNumber ?: "",
        entity.documentIssueDate ?: "",
        entity.documentExpiryDate ?: "",
        entity.photoIdentityBackSide ?: "",
        entity.duplicateType ?: "",
        entity.internalApproval ?: "",
        entity.middleName ?: "",
        entity.customReason ?: "",
        entity.adminNote ?: "",
        entity.nativeFullName ?: "",
        entity.blockReason ?: "",
        entity.issueDateNonApplicable ?: false,
        entity.expiryDateNonApplicable ?: false
    )

    val isIdentityCard: Boolean
        get() = TYPE_NATIONAL_ID == documentType

    val isPassport: Boolean
        get() = TYPE_PASSPORT == documentType

    val displayDob: String
        get() = dob.replace("-", "/")

    val displayIssueDate: String
        get() = if (documentIssueDate.isEmpty()) "N/A" else documentIssueDate

    companion object {
        const val TYPE_PASSPORT = "passport"
        const val TYPE_NATIONAL_ID = "national_id"
    }
}