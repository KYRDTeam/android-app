package com.kyberswap.android.presentation.wallet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ImportWalletPagerAdapter constructor(
    fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {

        return when (position) {
            JSON -> ImportJsonFragment.newInstance()
            PRIVATE_KEY -> return ImportPrivateKeyFragment.newInstance()
            else -> return ImportSeedFragment.newInstance()
        }
    }

    override fun getCount() = 3

    companion object {
        private const val JSON = 0
        private const val PRIVATE_KEY = 1
        private const val SEED = 2
    }
}