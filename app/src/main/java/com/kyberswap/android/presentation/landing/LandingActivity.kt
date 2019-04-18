package com.kyberswap.android.presentation.landing

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityLandingBinding
import com.kyberswap.android.presentation.base.BaseActivity

class LandingActivity : BaseActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityLandingBinding>(this, R.layout.activity_landing)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.splashScreenTheme)
        super.onCreate(savedInstanceState)
        binding.vpLanding.adapter = LandingPagerAdapter(supportFragmentManager)
        binding.indicator.setViewPager(binding.vpLanding)

    }

    companion object {
        fun newIntent(context: Context) =
            Intent(context, LandingActivity::class.java)
    }
}
