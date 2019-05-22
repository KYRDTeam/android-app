package com.kyberswap.android.presentation.main.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.contact.SaveContactUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.SaveContactState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val saveContactUseCase: SaveContactUseCase
) : ViewModel() {
    private val _saveContactCallback = MutableLiveData<Event<SaveContactState>>()
    val saveContactCallback: LiveData<Event<SaveContactState>>
        get() = _saveContactCallback


    fun save(walletAddress: String, name: String, address: String) {
        saveContactUseCase.execute(
            Action {
                _saveContactCallback.value = Event(SaveContactState.Success(""))
    ,
            Consumer {
                it.printStackTrace()
                _saveContactCallback.value = Event(SaveContactState.ShowError(it.localizedMessage))
    ,
            SaveContactUseCase.Param(walletAddress, name, address)
        )
    }
}