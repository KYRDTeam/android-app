package com.kyberswap.android.presentation.main.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.usecase.contact.DeleteContactUseCase
import com.kyberswap.android.domain.usecase.contact.GetContactUseCase
import com.kyberswap.android.domain.usecase.contact.SaveContactUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.swap.DeleteContactState
import com.kyberswap.android.presentation.main.swap.GetContactState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ContactViewModel @Inject constructor(
    private val saveContactUseCase: SaveContactUseCase,
    private val getContactUseCase: GetContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase) {
    private val _saveContactCallback = MutableLiveData<Event<SaveContactState>>()
    val saveContactCallback: LiveData<Event<SaveContactState>>
        get() = _saveContactCallback

    private val _getContactCallback = MutableLiveData<Event<GetContactState>>()
    val getContactCallback: LiveData<Event<GetContactState>>
        get() = _getContactCallback

    private val _deleteContactCallback = MutableLiveData<Event<DeleteContactState>>()
    val deleteContactCallback: LiveData<Event<DeleteContactState>>
        get() = _deleteContactCallback


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
            SaveContactUseCase.Param(walletAddress, contact.address, contact.name, true)
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

    fun deleteContact(contact: Contact) {
        deleteContactUseCase.execute(
            Action {
                _deleteContactCallback.value = Event(DeleteContactState.Success(""))
    ,
            Consumer {
                it.printStackTrace()
                _deleteContactCallback.value =
                    Event(DeleteContactState.ShowError(it.localizedMessage))
    ,
            DeleteContactUseCase.Param(contact)
        )
    }

    override fun onCleared() {
        saveContactUseCase.dispose()
        getContactUseCase.dispose()
        super.onCleared()
    }

}