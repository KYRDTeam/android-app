package com.kyberswap.android.presentation.wallet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.kyberswap.android.domain.model.Word

private const val NUMBER_WORD_PER_PAGE = 4

class BackupWalletPagerAdapter constructor(
    fm: FragmentManager,
    private val words: List<Word>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val subListWords = words.subList(
            position * NUMBER_WORD_PER_PAGE,
            (position + 1) * NUMBER_WORD_PER_PAGE
        )
        return if (position == 0) {
            BackupWalletFragment.newInstance(
                subListWords
            )
        } else {
            BackupWalletFragmentNext.newInstance(
                subListWords
            )
        }
    }

    override fun getCount() = words.size / NUMBER_WORD_PER_PAGE
}