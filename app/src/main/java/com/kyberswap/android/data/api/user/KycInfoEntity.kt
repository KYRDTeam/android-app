package com.kyberswap.android.data.api.user


import com.google.gson.annotations.SerializedName

data class KycInfoEntity(
    @SerializedName("id")
    val id: Int? = 0,
    @SerializedName("first_name")
    val firstName: String? = "",
    @SerializedName("last_name")
    val lastName: String? = "",
    @SerializedName("nationality")
    val nationality: String? = "",
    @SerializedName("country")
    val country: String? = "",
    @SerializedName("reject_reason")
    val rejectReason: String? = "",
    @SerializedName("gender")
    val gender: Boolean? = false,
    @SerializedName("dob")
    val dob: String? = "",
    @SerializedName("profile_completed")
    val profileCompleted: Boolean? = false,
    @SerializedName("document_id")
    val documentId: String? = "",
    @SerializedName("document_type")
    val documentType: String? = "",
    @SerializedName("photo_selfie")
    val photoSelfie: String? = "",
    @SerializedName("photo_identity_front_side")
    val photoIdentityFrontSide: String? = "",
    @SerializedName("artemis_response")
    val artemisResponse: String? = "",
    @SerializedName("duplicate_id")
    val duplicateId: String? = "",
    @SerializedName("residential_address")
    val residentialAddress: String? = "",
    @SerializedName("city")
    val city: String? = "",
    @SerializedName("zip_code")
    val zipCode: String? = "",
    @SerializedName("document_proof_address")
    val documentProofAddress: String? = "",
    @SerializedName("photo_proof_address")
    val photoProofAddress: String? = "",
    @SerializedName("source_fund")
    val sourceFund: String? = "",
    @SerializedName("occupation_code")
    val occupationCode: String? = "",
    @SerializedName("industry_code")
    val industryCode: String? = "",
    @SerializedName("tax_residency_country")
    val taxResidencyCountry: String? = "",
    @SerializedName("have_tax_identification")
    val haveTaxIdentification: Boolean? = false,
    @SerializedName("tax_identification_number")
    val taxIdentificationNumber: String? = "",
    @SerializedName("document_issue_date")
    val documentIssueDate: String? = "",
    @SerializedName("document_expiry_date")
    val documentExpiryDate: String? = "",
    @SerializedName("photo_identity_back_side")
    val photoIdentityBackSide: String? = "",
    @SerializedName("duplicate_type")
    val duplicateType: String? = "",
    @SerializedName("internal_approval")
    val internalApproval: String? = "",
    @SerializedName("middle_name")
    val middleName: String? = "",
    @SerializedName("custom_reason")
    val customReason: String? = "",
    @SerializedName("admin_note")
    val adminNote: String? = "",
    @SerializedName("native_full_name")
    val nativeFullName: String? = "",
    @SerializedName("block_reason")
    val blockReason: String? = "",
    @SerializedName("issue_date_non_applicable")
    val issueDateNonApplicable: Boolean? = false,
    @SerializedName("expiry_date_non_applicable")
    val expiryDateNonApplicable: Boolean? = false
)