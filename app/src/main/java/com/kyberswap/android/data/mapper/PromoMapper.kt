package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.promo.PromoEntity
import com.kyberswap.android.domain.model.Promo
import javax.inject.Inject

class PromoMapper @Inject constructor() {
    fun transform(promoEntity: PromoEntity): Promo {
        return Promo(promoEntity)
    }

}