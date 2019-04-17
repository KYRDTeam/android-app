package com.kyberswap.android.presentation.landing

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class LandingPagerAdapter constructor(
        fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return LandingFragment.newInstance(position)
    }

    override fun getCount() = 3
}