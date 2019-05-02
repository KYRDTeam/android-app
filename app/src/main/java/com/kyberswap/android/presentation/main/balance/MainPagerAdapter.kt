package com.kyberswap.android.presentation.main.balance

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.main.swap.SwapFragment

class MainPagerAdapter constructor(
    fm: FragmentManager,
    val wallet: Wallet?
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            BALANCE -> BalanceFragment.newInstance(
                wallet
            )
            else -> SwapFragment.newInstance(wallet)


    }

    override fun getCount() = 4

    companion object {
        private const val BALANCE = 0
        private const val SWAP = 1
        private const val PROFILE = 2
        private const val SETTING = 2
    }
}