package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.Contact

sealed class GetContactState {
    object Loading : GetContactState()
    class ShowError(val message: String?) : GetContactState()
    class Success(val contacts: List<Contact>) : GetContactState()
}
