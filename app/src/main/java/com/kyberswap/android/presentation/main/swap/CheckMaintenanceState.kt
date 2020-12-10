package com.kyberswap.android.presentation.main.swap

sealed class CheckMaintenanceState {
    object Loading : CheckMaintenanceState()
    class ShowError(val message: String?) : CheckMaintenanceState()
    class Success(val isMaintenance: Boolean) : CheckMaintenanceState()
}
