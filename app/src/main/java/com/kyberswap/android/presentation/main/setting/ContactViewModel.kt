package com.kyberswap.android.presentation.main.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.usecase.contact.GetContactUseCase
import com.kyberswap.android.domain.usecase.contact.SaveContactUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.GetContactState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ContactViewModel @Inject constructor(
    private val saveContactUseCase: SaveContactUseCase,
    private val getContactUseCase: GetContactUseCase
) : ViewModel() {
    private val _saveContactCallback = MutableLiveData<Event<SaveContactState>>()
    val saveContactCallback: LiveData<Event<SaveContactState>>
        get() = _saveContactCallback

    private val _getContactCallback = MutableLiveData<Event<GetContactState>>()
    val getContactCallback: LiveData<Event<GetContactState>>
        get() = _getContactCallback


    fun saveSendContact(walletAddress: String, contact: Contact) {
        saveContactUseCase.execute(
            Action {
                _saveContactCallback.value = Event(SaveContactState.Success())
    ,
            Consumer {
                it.printStackTrace()
                _saveContactCallback.value =
                    Event(SaveContactState.ShowError(it.localizedMessage))
    ,
            SaveContactUseCase.Param(walletAddress, contact.address, contact.name)
        )
    }

    fun getContact(walletAddress: String) {
        getContactUseCase.execute(
            Consumer {
                _getContactCallback.value = Event(GetContactState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getContactCallback.value = Event(GetContactState.ShowError(it.localizedMessage))
    ,
            GetContactUseCase.Param(walletAddress)
        )
    }

}