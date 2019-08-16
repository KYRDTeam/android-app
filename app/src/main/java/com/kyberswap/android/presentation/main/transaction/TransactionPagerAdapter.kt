package com.kyberswap.android.presentation.main.transaction

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kyberswap.android.domain.model.Wallet

class TransactionPagerAdapter constructor(
    fm: FragmentManager,
    private val wallet: Wallet?
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TransactionStatusFragment.newInstance(position, wallet)
    }

    override fun getCount() = 2

}