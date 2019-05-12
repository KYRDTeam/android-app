package com.kyberswap.android.presentation.main.swap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivitySwapConfirmBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class SwapConfirmActivity : BaseActivity() {
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var appExecutors: AppExecutors

    private val swapConfirmViewModel: SwapConfirmViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SwapConfirmViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivitySwapConfirmBinding>(
            this,
            R.layout.activity_swap_confirm
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewModel = swapConfirmViewModel
    }


    companion object {
        fun newIntent(context: Context) =
            Intent(context, SwapConfirmActivity::class.java).apply {
    
    }
}
