package com.kyberswap.android.presentation.main.balance.chart

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.token.GetToken24hVolUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ChartViewModel @Inject constructor(
    private val saveSwapDataTokenUseCase: SaveSwapDataTokenUseCase,
    private val saveSendTokenUseCase: SaveSendTokenUseCase,
    private val getToken24hVolUseCase: GetToken24hVolUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _callback = MutableLiveData<Event<SaveSwapDataState>>()
    val callback: LiveData<Event<SaveSwapDataState>>
        get() = _callback

    private val _callbackSaveSend = MutableLiveData<Event<SaveSendState>>()
    val callbackSaveSend: LiveData<Event<SaveSendState>>
        get() = _callbackSaveSend

    private val _get24hCallback = MutableLiveData<Event<GetVol24hState>>()
    val get24hCallback: LiveData<Event<GetVol24hState>>
        get() = _get24hCallback

    fun save(walletAddress: String, token: Token, isSell: Boolean = false) {
        saveSwapDataTokenUseCase.execute(
            Action {
                _callback.value = Event(SaveSwapDataState.Success())
            },
            Consumer {
                it.printStackTrace()
                _callback.value =
                    Event(SaveSwapDataState.ShowError(errorHandler.getError(it)))
            },
            SaveSwapDataTokenUseCase.Param(walletAddress, token, isSell)
        )
    }


    fun saveSendToken(address: String, token: Token) {
        saveSendTokenUseCase.execute(
            Action {
                _callbackSaveSend.value = Event(SaveSendState.Success())
            },
            Consumer {
                _callbackSaveSend.value = Event(SaveSendState.Success())
            },
            SaveSendTokenUseCase.Param(address, token)
        )
    }

    fun getVol24h(token: Token?) {

        if (token == null) return
        getToken24hVolUseCase.execute(
            Consumer {
                _get24hCallback.value = Event(GetVol24hState.Success(it))
            },
            Consumer {
                _get24hCallback.value = Event(GetVol24hState.ShowError(errorHandler.getError(it)))
            },
            GetToken24hVolUseCase.Param(token)
        )
    }

    override fun onCleared() {
        saveSwapDataTokenUseCase.dispose()
        saveSendTokenUseCase.dispose()
        getToken24hVolUseCase.dispose()
        super.onCleared()
    }
}

@Parcelize
enum class ChartType : Parcelable {
    DAY, WEEK, MONTH, YEAR, ALL;

    val resolution: String
        get() = when (this) {
            DAY -> "15"
            WEEK, MONTH -> "60"
            YEAR, ALL -> "D"
        }

    fun fromTime(toTime: Long): Long {
        return when (this) {
            DAY -> toTime - 24 * 60 * 60
            WEEK -> toTime - 7 * 24 * 60 * 60
            MONTH -> toTime - 30 * 24 * 60 * 60
            YEAR -> toTime - 365 * 24 * 60 * 60
            ALL -> 1
        }
    }


    private val dateFormatter: DateFormat
        get() {
            val pattern = when (this) {
                DAY -> "HH:mm"
                WEEK, MONTH -> "dd/MM HH:MM"
                YEAR, ALL -> "dd/MM"
            }
            return SimpleDateFormat(pattern, Locale.US)
        }

    fun label(forTime: Long): String {
        val date = Date(forTime)
        return dateFormatter.format(date)
    }
}

