package com.kyberswap.android.presentation.main.transaction

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TransactionPagerAdapter constructor(
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TransactionStatusFragment.newInstance(position)
    }

    override fun getCount() = 2

}