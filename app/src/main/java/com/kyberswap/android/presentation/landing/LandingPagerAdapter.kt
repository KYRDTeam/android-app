package com.kyberswap.android.presentation.landing

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class LandingPagerAdapter constructor(
    fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return LandingFragment.newInstance(position)
    }

    override fun getCount() = 3
}