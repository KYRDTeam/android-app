package com.kyberswap.android.data.repository.datasource.storage

import javax.inject.Inject

class StorageMediator @Inject constructor(private val hawkWrapper: HawkWrapper) {
    companion object {
        private const val KEY_UNIT = "selected_unit"
    }

    fun setSelectedUnit(unit: String): Boolean {
        return hawkWrapper.put(KEY_UNIT, unit)
    }

    fun getSelectedUnit(): String? {
        return hawkWrapper.getItem(KEY_UNIT, String::class.java)
    }
}