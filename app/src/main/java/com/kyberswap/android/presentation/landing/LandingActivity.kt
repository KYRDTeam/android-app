package com.kyberswap.android.presentation.landing

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityLandingBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class LandingActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(LandingViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityLandingBinding>(this, R.layout.activity_landing)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.splashScreenTheme)
        super.onCreate(savedInstanceState)
        binding.viewModel = viewModel
        binding.vpLanding.adapter = LandingPagerAdapter(supportFragmentManager)
        binding.indicator.setViewPager(binding.vpLanding)

    }

    companion object {
        fun newIntent(context: Context) =
                Intent(context, LandingActivity::class.java)
    }
}
