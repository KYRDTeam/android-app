package com.kyberswap.android.presentation.main.explore

import androidx.lifecycle.ViewModel
import com.kyberswap.android.util.ErrorHandler
import javax.inject.Inject

class CampaignViewModel @Inject constructor(
    private val errorHandler: ErrorHandler
) : ViewModel()