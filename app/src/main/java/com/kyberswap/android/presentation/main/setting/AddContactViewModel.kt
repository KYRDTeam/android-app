package com.kyberswap.android.presentation.main.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.usecase.contact.DeleteContactUseCase
import com.kyberswap.android.domain.usecase.contact.SaveContactUseCase
import com.kyberswap.android.domain.usecase.send.ENSResolveUseCase
import com.kyberswap.android.domain.usecase.send.ENSRevertResolveUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.DeleteContactState
import com.kyberswap.android.presentation.main.swap.GetENSAddressState
import com.kyberswap.android.presentation.main.swap.SaveContactState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val saveContactUseCase: SaveContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val ensResolveUseCase: ENSResolveUseCase,
    private val ensRevertResolveUseCase: ENSRevertResolveUseCase
) : ViewModel() {
    private val _saveContactCallback = MutableLiveData<Event<SaveContactState>>()
    val saveContactCallback: LiveData<Event<SaveContactState>>
        get() = _saveContactCallback

    private val _deleteContactCallback = MutableLiveData<Event<DeleteContactState>>()
    val deleteContactCallback: LiveData<Event<DeleteContactState>>
        get() = _deleteContactCallback

    private val _getGetENSCallback = MutableLiveData<Event<GetENSAddressState>>()
    val getGetENSCallback: LiveData<Event<GetENSAddressState>>
        get() = _getGetENSCallback

    private val _getGetRevertENSCallback = MutableLiveData<Event<GetENSAddressState>>()
    val getGetRevertENSCallback: LiveData<Event<GetENSAddressState>>
        get() = _getGetRevertENSCallback

    val compositeDisposable = CompositeDisposable()


    fun save(walletAddress: String, name: String, address: String) {
        saveContactUseCase.execute(
            Action {
                _saveContactCallback.value = Event(SaveContactState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _saveContactCallback.value = Event(SaveContactState.ShowError(it.localizedMessage))
            },
            SaveContactUseCase.Param(walletAddress, address, name)
        )
    }

    fun saveSendContact(walletAddress: String, contact: Contact, isSend: Boolean = false) {
        saveContactUseCase.execute(
            Action {
                _saveContactCallback.value = Event(SaveContactState.Success(isSend = isSend))
            },
            Consumer {
                it.printStackTrace()
                _saveContactCallback.value =
                    Event(SaveContactState.ShowError(it.localizedMessage))
            },
            SaveContactUseCase.Param(walletAddress, contact.address, contact.name, isSend)
        )
    }

    fun deleteContact(contact: Contact) {
        deleteContactUseCase.execute(
            Action {
                _deleteContactCallback.value = Event(DeleteContactState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _deleteContactCallback.value =
                    Event(DeleteContactState.ShowError(it.localizedMessage))
            },
            DeleteContactUseCase.Param(contact)
        )
    }

    fun revertResolve(address: String) {

        ensRevertResolveUseCase.dispose()
        ensRevertResolveUseCase.execute(
            Consumer {
                _getGetRevertENSCallback.value =
                    Event(GetENSAddressState.Success(it, address))
            },
            Consumer {
                _getGetRevertENSCallback.value = Event(GetENSAddressState.ShowError(it.message))
            },
            ENSRevertResolveUseCase.Param(address)
        )
    }

    fun resolve(name: String) {
        ensResolveUseCase.dispose()
        ensResolveUseCase.execute(
            Consumer {
                _getGetENSCallback.value =
                    Event(GetENSAddressState.Success(name, it))
            },
            Consumer {
                _getGetENSCallback.value = Event(GetENSAddressState.ShowError(it.message))
            },
            ENSResolveUseCase.Param(name)
        )
    }
}