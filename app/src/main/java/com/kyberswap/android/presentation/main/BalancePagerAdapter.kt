package com.kyberswap.android.presentation.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kyberswap.android.domain.model.Wallet

class BalancePagerAdapter constructor(
    fm: FragmentManager,
    private val wallet: Wallet?
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {

        return when (position) {
            KYBER_LIST -> KyberListFragment.newInstance(wallet)
            else -> return OtherFragment.newInstance(wallet)
        }
    }

    override fun getCount() = 2

    companion object {
        private const val KYBER_LIST = 0
        private const val OTHER = 1
    }
}