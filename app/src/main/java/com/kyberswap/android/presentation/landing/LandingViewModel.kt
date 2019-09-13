package com.kyberswap.android.presentation.landing

import androidx.lifecycle.ViewModel
import com.kyberswap.android.R
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import javax.inject.Inject

data class Landing(
    val imageId: Int,
    val title: Int,
    val titleDescription: Int
)

class LandingViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase
) : ViewModel() {
    val landingList by lazy {
        listOf(

            Landing(R.drawable.swap, R.string.landing_2_title, R.string.landing_2_content),
            Landing(
                R.drawable.ic_litmit_order_icons,
                R.string.limit_order,
                R.string.landing_limit_order_description
            ),

                Landing(
                R.drawable.ic_security_check,
            R.string.landing_1_title,
            R.string.landing_1_content
        )

        )
    }

    override fun onCleared() {
        createWalletUseCase.dispose()
        super.onCleared()
    }
}