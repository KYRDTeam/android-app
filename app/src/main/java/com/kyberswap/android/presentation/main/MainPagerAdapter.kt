package com.kyberswap.android.presentation.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.main.balance.BalanceFragment
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.swap.SwapFragment

class MainPagerAdapter constructor(
    fm: FragmentManager,
    val wallet: Wallet?
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            BALANCE -> BalanceFragment.newInstance(
                wallet
            )
            SWAP -> SwapFragment.newInstance(wallet)
            else -> ProfileFragment.newInstance()


    }

    override fun getCount() = 4

    companion object {
        private const val BALANCE = 0
        private const val SWAP = 1
        private const val PROFILE = 2
        private const val SETTING = 2
    }
}