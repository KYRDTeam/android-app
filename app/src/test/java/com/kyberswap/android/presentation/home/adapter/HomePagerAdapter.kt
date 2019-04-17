package com.kyberswap.android.presentation.home.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.Header
import com.kyberswap.android.domain.model.Header.Type.FEATURE
import com.kyberswap.android.domain.model.Header.Type.TOP
import com.kyberswap.android.presentation.home.top.FeatureFragment
import com.kyberswap.android.presentation.home.top.TopFragment

class HomePagerAdapter(
    private val context: Context?,
    fm: FragmentManager,
    private val fragmentTitles: List<Header>
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val header = fragmentTitles[getRealPosition(position)]
        return when (header.type) {
            TOP -> TopFragment.newInstance()
            FEATURE -> FeatureFragment.newInstance(header)

    }

    override fun getCount(): Int {
        return fragmentTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return getTitleAt(position)
    }

    private fun getRealPosition(position: Int): Int {
        return position % fragmentTitles.size
    }

    private fun getTitleAt(position: Int): String? {
        return when (fragmentTitles[getRealPosition(position)].type) {
            TOP -> context?.getString(R.string.header_top)
            FEATURE -> fragmentTitles[position % fragmentTitles.size].label

    }
}
