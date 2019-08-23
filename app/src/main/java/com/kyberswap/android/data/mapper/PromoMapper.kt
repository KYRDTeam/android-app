package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.promo.PromoResponseEntity
import com.kyberswap.android.domain.model.Promo
import javax.inject.Inject

class PromoMapper @Inject constructor() {
    fun transform(promoEntity: PromoResponseEntity): Promo {
        promoEntity.data.error = promoEntity.error
        return Promo(promoEntity.data)
    }

}