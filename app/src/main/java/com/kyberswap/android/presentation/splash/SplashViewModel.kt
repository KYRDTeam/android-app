package com.kyberswap.android.presentation.splash

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class SplashViewModel @Inject constructor(
) : ViewModel() {

    private val _authorization = MutableLiveData<Boolean>()
    val authorization: MutableLiveData<Boolean> = _authorization

    init {
    }
}