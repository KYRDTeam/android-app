package com.kyberswap.android.presentation.main.setting.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.wallet.DeleteWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.ExportKeystoreWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.ExportMnemonicWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.ExportPrivateKeyWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class EditWalletViewModel @Inject constructor(
    private val deleteWalletUseCase: DeleteWalletUseCase,
    private val exportKeystoreWalletUseCase: ExportKeystoreWalletUseCase,
    private val exportPrivateKeyWalletUseCase: ExportPrivateKeyWalletUseCase,
    private val exportMnemonicWalletUseCase: ExportMnemonicWalletUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val saveWalletUseCase: SaveWalletUseCase
) : ViewModel() {
    private val _deleteWalletCallback = MutableLiveData<Event<DeleteWalletState>>()
    val deleteWalletCallback: LiveData<Event<DeleteWalletState>>
        get() = _deleteWalletCallback

    private val _exportKeystoreWalletCallback = MutableLiveData<Event<ExportWalletState>>()
    val exportKeystoreWalletCallback: LiveData<Event<ExportWalletState>>
        get() = _exportKeystoreWalletCallback

    private val _exportPrivateKeyWalletCallback = MutableLiveData<Event<ExportWalletState>>()
    val exportPrivateKeyWalletCallback: LiveData<Event<ExportWalletState>>
        get() = _exportPrivateKeyWalletCallback

    private val _exportMnemonicCallback = MutableLiveData<Event<ExportWalletState>>()
    val exportMnemonicCallback: LiveData<Event<ExportWalletState>>
        get() = _exportMnemonicCallback

    private val _saveWalletCallback = MutableLiveData<Event<SaveWalletState>>()
    val saveWalletCallback: LiveData<Event<SaveWalletState>>
        get() = _saveWalletCallback

    private val _updateWalletCallback = MutableLiveData<Event<SaveWalletState>>()
    val updateWalletCallback: LiveData<Event<SaveWalletState>>
        get() = _updateWalletCallback

    val compositeDisposable = CompositeDisposable()


    fun backupKeyStore(password: String, wallet: Wallet) {
        _exportKeystoreWalletCallback.postValue(Event(ExportWalletState.Loading))
        exportKeystoreWalletUseCase.execute(
            Consumer {
                _exportKeystoreWalletCallback.value = Event(ExportWalletState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _exportKeystoreWalletCallback.value =
                    Event(ExportWalletState.ShowError(it.localizedMessage))
            },
            ExportKeystoreWalletUseCase.Param(password, wallet)
        )
    }

    fun deleteWallet(wallet: Wallet) {
        _deleteWalletCallback.postValue(Event(DeleteWalletState.Loading))
        deleteWalletUseCase.execute(
            Consumer {
                _deleteWalletCallback.value = Event(DeleteWalletState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _deleteWalletCallback.value =
                    Event(DeleteWalletState.ShowError(it.localizedMessage))
            },
            DeleteWalletUseCase.Param(wallet)
        )
    }


    fun backupPrivateKey(wallet: Wallet) {
        _exportPrivateKeyWalletCallback.postValue(Event(ExportWalletState.Loading))
        exportPrivateKeyWalletUseCase.execute(
            Consumer {
                _exportPrivateKeyWalletCallback.value = Event(ExportWalletState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _exportPrivateKeyWalletCallback.value =
                    Event(ExportWalletState.ShowError(it.localizedMessage))

            },
            ExportPrivateKeyWalletUseCase.Param(wallet)
        )
    }

    fun backupMnemonic(wallet: Wallet) {
        _exportMnemonicCallback.postValue(Event(ExportWalletState.Loading))
        exportMnemonicWalletUseCase.execute(
            Consumer {
                _exportMnemonicCallback.value = Event(ExportWalletState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _exportMnemonicCallback.value =
                    Event(ExportWalletState.ShowError(it.localizedMessage))
            },
            ExportMnemonicWalletUseCase.Param(wallet)
        )
    }


    fun updateWallet(wallet: Wallet, extra: String) {
        updateWalletUseCase.dispose()
        updateWalletUseCase.execute(
            Action {
                _updateWalletCallback.value = Event(SaveWalletState.Success("", extra))
            },
            Consumer {
                it.printStackTrace()
                _updateWalletCallback.value =
                    Event(SaveWalletState.ShowError(it.localizedMessage))
            },
            wallet
        )
    }

    fun save(wallet: Wallet) {
        saveWalletUseCase.dispose()
        saveWalletUseCase.execute(
            Action {
                _saveWalletCallback.value = Event(SaveWalletState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _saveWalletCallback.value = Event(SaveWalletState.ShowError(it.localizedMessage))
            },
            SaveWalletUseCase.Param(wallet)
        )
    }

    override fun onCleared() {
        saveWalletUseCase.dispose()
        updateWalletUseCase.dispose()
        compositeDisposable.dispose()
        deleteWalletUseCase.dispose()
        exportKeystoreWalletUseCase.dispose()
        exportPrivateKeyWalletUseCase.dispose()
        exportMnemonicWalletUseCase.dispose()
        super.onCleared()
    }
}