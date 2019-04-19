package com.kyberswap.android.presentation.landing

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.kyberswap.android.R
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import io.reactivex.functions.Consumer
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
            Landing(
                R.drawable.ic_security_check,
                R.string.landing_1_title,
                R.string.landing_1_content
            ),
            Landing(R.drawable.swap, R.string.landing_2_title, R.string.landing_2_content),
            Landing(
                R.drawable.profile,
                R.string.landing_3_title,
                R.string.landing_3_content
            )
        )
    }

    val createWalletCallback: MutableLiveData<CreateWalletState> = MutableLiveData()

    fun createWallet(pinLock: String = "") {
        createWalletCallback.postValue(CreateWalletState.Loading)
        createWalletUseCase.execute(
            Consumer {

                createWalletCallback.value = CreateWalletState.Success(it)

            },
            Consumer {
                it.printStackTrace()
                createWalletCallback.value = CreateWalletState.ShowError(it.localizedMessage)
            },
            CreateWalletUseCase.Param(pinLock)
        )
    }

}