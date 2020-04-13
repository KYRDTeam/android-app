package com.kyberswap.android.presentation.main

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.presentation.main.balance.BalanceFragment
import com.kyberswap.android.presentation.main.limitorder.LimitOrderV2Fragment
import com.kyberswap.android.presentation.main.profile.ProfileFragment
import com.kyberswap.android.presentation.main.setting.SettingFragment
import com.kyberswap.android.presentation.main.swap.SwapFragment

class MainPagerAdapter constructor(
    fm: FragmentManager,
    alert: NotificationAlert?,
    limitOrder: NotificationLimitOrder?,
    notification: Notification? = null
) : FragmentPagerAdapter(fm) {
    private val listFragment = mutableListOf<Fragment>()

    init {
        listFragment.add(BALANCE, BalanceFragment.newInstance())
        listFragment.add(SWAP, SwapFragment.newInstance(alert, notification))
        listFragment.add(LIMIT_ORDER, LimitOrderV2Fragment.newInstance(limitOrder))
        listFragment.add(PROFILE, ProfileFragment.newInstance())
        listFragment.add(SETTING, SettingFragment.newInstance())

    }

    var registeredFragments = SparseArray<Fragment>()

    override fun getItem(position: Int): Fragment {
        return listFragment[position]
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

    override fun getCount() = 5

    companion object {
        const val BALANCE = 0
        const val SWAP = 1
        const val LIMIT_ORDER = 2
        const val PROFILE = 3
        const val SETTING = 4
    }
}