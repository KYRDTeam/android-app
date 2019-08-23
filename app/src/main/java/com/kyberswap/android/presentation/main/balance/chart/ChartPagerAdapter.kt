package com.kyberswap.android.presentation.main.balance.chart

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ChartPagerAdapter constructor(
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    private val fragmentList = mutableListOf<Fragment>()
    private val fragmentTitle = mutableListOf<String>()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentTitle.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitle[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitle.add(title)
    }


}