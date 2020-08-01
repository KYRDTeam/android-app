package com.kyberswap.android.presentation.main.explore

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kyberswap.android.domain.model.Campaign

class CampaignPagerAdapter constructor(
    fm: FragmentManager,
    private val campaigns: List<Campaign>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return CampaignFragment.newInstance(campaigns[position])
    }

    override fun getCount() = campaigns.size
}