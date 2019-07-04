package com.kyberswap.android.presentation.main.profile.kyc

import android.widget.ImageView

sealed class DecodeBase64State {
    object Loading : DecodeBase64State()
    class ShowError(val message: String?) : DecodeBase64State()
    class Success(val byteArray: ByteArray, val imageView: ImageView? = null) : DecodeBase64State()
}
