package com.kyberswap.android.presentation.main.balance

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kyberswap.android.presentation.main.balance.kyberlist.KyberListFragment
import com.kyberswap.android.presentation.main.balance.other.OtherFragment

class BalancePagerAdapter constructor(
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {

        return when (position) {
            KYBER_LIST -> KyberListFragment.newInstance()
            else -> return OtherFragment.newInstance()

    }

    override fun getCount() = 2

    companion object {
        private const val KYBER_LIST = 0
        private const val OTHER = 1
    }
}