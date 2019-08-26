package com.kyberswap.android.presentation.wallet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ImportWalletPagerAdapter constructor(
    fm: FragmentManager,
    val fromMain: Boolean
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {

        return when (position) {
            JSON -> ImportJsonFragment.newInstance(fromMain)
            PRIVATE_KEY -> return ImportPrivateKeyFragment.newInstance(fromMain)
            else -> return ImportSeedFragment.newInstance(fromMain)
        }
    }

    override fun getCount() = 3

    companion object {
        private const val JSON = 0
        private const val PRIVATE_KEY = 1
        private const val SEED = 2
    }
}