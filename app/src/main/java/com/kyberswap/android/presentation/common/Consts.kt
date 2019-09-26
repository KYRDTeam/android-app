package com.kyberswap.android.presentation.common

import java.math.BigInteger

const val DEFAULT_ACCEPT_RATE_PERCENTAGE = 3
const val PERM = "PERM"
val DEFAULT_MAX_AMOUNT: BigInteger = 2.toBigInteger().pow(255)
const val DEFAULT_WALLET_ID = "0x3fFFF2F4f6C0831FAC59534694ACd14AC2Ea501b"
val DEFAULT_GAS_LIMIT = 380_000.toBigInteger()
const val DEFAULT_NAME = "Untitled"
const val MIN_SUPPORT_SWAP_SOURCE_AMOUNT = 0.001
val KEEP_ETH_BALANCE_FOR_GAS = 600_000.toBigDecimal()


enum class Stage { FINGERPRINT, NEW_FINGERPRINT_ENROLLED, PASSWORD }

const val DEFAULT_KEY_NAME = "default_key"