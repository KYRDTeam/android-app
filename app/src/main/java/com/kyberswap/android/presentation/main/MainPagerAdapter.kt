package com.kyberswap.android.presentation.main

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.main.balance.BalanceFragment
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.setting.SettingFragment
import com.kyberswap.android.presentation.main.swap.SwapFragment


class MainPagerAdapter constructor(
    fm: FragmentManager,
    val wallet: Wallet?
) : FragmentPagerAdapter(fm) {

    var registeredFragments = SparseArray<Fragment>()


    override fun getItem(position: Int): Fragment {
        return when (position) {
            BALANCE -> BalanceFragment.newInstance(
                wallet
            )
            SWAP -> SwapFragment.newInstance(wallet)
            SETTING -> SettingFragment.newInstance()
            else -> ProfileFragment.newInstance(wallet)
        }
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun getRegisteredFragment(position: Int): Fragment? {
        return registeredFragments.get(position)
    }

    override fun getCount() = 4

    companion object {
        const val BALANCE = 0
        const val SWAP = 1
        const val PROFILE = 2
        const val SETTING = 3
    }
}