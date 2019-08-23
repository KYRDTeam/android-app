package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Ignore
import com.kyberswap.android.data.api.user.KycInfoEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class KycInfo(
    var id: Int = 0,
    var firstName: String = "",
    var lastName: String = "",
    var nationality: String = "",
    var country: String = "",
    var rejectReason: String? = "",
    var gender: Boolean = false,
    var dob: String = "",
    var profileCompleted: Boolean = false,
    var documentId: String = "",
    var documentType: String = "",
    @Ignore
    var photoSelfie: String = "",
    @Ignore
    var photoIdentityFrontSide: String = "",
    var duplicateId: String = "",
    var residentialAddress: String = "",
    var city: String = "",
    var zipCode: String = "",
    var documentProofAddress: String = "",
    @Ignore
    var photoProofAddress: String = "",
    var sourceFund: String = "",
    var occupationCode: String = "",
    var industryCode: String = "",
    var taxResidencyCountry: String = "",
    var haveTaxIdentification: Boolean? = false,
    var taxIdentificationNumber: String = "",
    var documentIssueDate: String = "",
    var documentExpiryDate: String = "",
    @Ignore
    var photoIdentityBackSide: String = "",
    var duplicateType: String = "",
    var internalApproval: String = "",
    var middleName: String = "",
    var customReason: String = "",
    var adminNote: String = "",
    var nativeFullName: String = "",
    var blockReason: String = "",
    var issueDateNonApplicable: Boolean = false,
    var expiryDateNonApplicable: Boolean = false
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

    val displayExpiredDate: String
        get() = if (documentExpiryDate.isEmpty()) "N/A" else documentExpiryDate

    val displayTaxIdentificationNumber: String
        get() = if (taxIdentificationNumber.isEmpty()) "N/A" else taxIdentificationNumber

    fun hasSameIdentityInfo(other: KycInfo?): Boolean {
        return this.documentId == other?.documentId &&
            this.documentType == other.documentType &&
            this.documentIssueDate == other.documentIssueDate &&
            this.issueDateNonApplicable == other.issueDateNonApplicable &&
            this.expiryDateNonApplicable == other.expiryDateNonApplicable &&
            this.documentExpiryDate == other.documentExpiryDate &&
            this.photoIdentityFrontSide == other.photoIdentityFrontSide &&
            this.photoIdentityBackSide == other.photoIdentityBackSide &&
            this.photoSelfie == other.photoSelfie
    }

    fun hasSamePersonalInfo(other: KycInfo?): Boolean {
        return this.firstName == other?.firstName &&
            this.middleName == other.middleName &&
            this.lastName == other.lastName &&
            this.nativeFullName == other.nativeFullName &&
            this.nationality == other.nationality &&
            this.country == other.country &&
            this.dob == other.dob &&
            this.gender == other.gender &&
            this.residentialAddress == other.residentialAddress &&
            this.city == other.city &&
            this.zipCode == other.zipCode &&
            this.documentProofAddress == other.documentProofAddress &&
            this.photoProofAddress == other.photoProofAddress &&
            this.occupationCode == other.occupationCode &&
            this.industryCode == other.industryCode &&
            this.taxResidencyCountry == other.taxResidencyCountry &&
            this.haveTaxIdentification == other.haveTaxIdentification &&
            this.taxIdentificationNumber == other.taxIdentificationNumber &&
            this.sourceFund == other.sourceFund
    }

    fun hasSameKycInfo(other: KycInfo?): Boolean {
        return hasSameIdentityInfo(other) && hasSamePersonalInfo(other)
    }

    companion object {
        const val TYPE_PASSPORT = "passport"
        const val TYPE_NATIONAL_ID = "national_id"
    }
}