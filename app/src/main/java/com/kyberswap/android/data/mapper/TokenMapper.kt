package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.token.TokenEntity
import com.kyberswap.android.domain.model.Token
import javax.inject.Inject

class TokenMapper @Inject constructor() {
    fun transform(entity: TokenEntity): Token {
        return Token(entity)
    }
}