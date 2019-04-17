package com.kyberswap.android.domain.model

data class Credential(
    val uid: String,
    val name: String,
    val email: String,
    val salons: List<Salon>
)
