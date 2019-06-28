package com.kyberswap.android.presentation.main.profile.kyc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import javax.inject.Inject

class PersonalInfoViewModel @Inject constructor(
) : ViewModel() {

    private val _getAlertsCallback = MutableLiveData<Event<GetAlertsState>>()
    val getAlertsCallback: LiveData<Event<GetAlertsState>>
        get() = _getAlertsCallback

}