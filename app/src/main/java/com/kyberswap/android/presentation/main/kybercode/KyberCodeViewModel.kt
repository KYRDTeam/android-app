package com.kyberswap.android.presentation.main.kybercode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.ApplyKyberCodeUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class KyberCodeViewModel @Inject constructor(
    private val applyKyberCodeUseCase: ApplyKyberCodeUseCase

) : ViewModel() {
    private val _getKyberCodeCallback = MutableLiveData<Event<KyberCodeState>>()
    val getKyberCodeCallback: LiveData<Event<KyberCodeState>>
        get() = _getKyberCodeCallback

    fun createWalletByKyberCode(kyberCode: String, walletName: String) {
        _getKyberCodeCallback.postValue(Event(KyberCodeState.Loading))
        applyKyberCodeUseCase.execute(
            Consumer {
                if (it.promo?.error.isNullOrEmpty()) {
                    _getKyberCodeCallback.value = Event(KyberCodeState.Success(it))
         else {
                    _getKyberCodeCallback.value = Event(KyberCodeState.ShowError(it.promo?.error))
        

    ,
            Consumer {
                it.printStackTrace()
                _getKyberCodeCallback.value = Event(KyberCodeState.ShowError(it.localizedMessage))
    ,
            ApplyKyberCodeUseCase.Param(kyberCode, walletName)
        )
    }
}