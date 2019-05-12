package com.kyberswap.android.presentation.helper

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.landing.LandingActivity
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.BalanceAddressFragment
import com.kyberswap.android.presentation.main.swap.SwapConfirmActivity
import com.kyberswap.android.presentation.main.swap.SwapFragment
import com.kyberswap.android.presentation.main.swap.TokenSearchFragment
import com.kyberswap.android.presentation.wallet.BackupWalletActivity
import com.kyberswap.android.presentation.wallet.ImportWalletActivity
import com.kyberswap.android.presentation.wallet.VerifyBackupWordActivity
import javax.inject.Inject

class Navigator @Inject constructor(private val activity: AppCompatActivity) {
    private val containerId: Int = R.id.container
    private val fragmentManager: FragmentManager = activity.supportFragmentManager

    fun navigateToLandingPage() {
        activity.startActivity(LandingActivity.newIntent(activity))
        activity.finish()
    }

    fun navigateToImportWalletPage() {
        activity.startActivity(ImportWalletActivity.newIntent(activity))
    }

    fun navigateVerifyBackupWordPage(words: List<Word>, wallet: Wallet) {
        activity.startActivity(VerifyBackupWordActivity.newIntent(activity, words, wallet))
    }

    fun navigateToBackupWalletPage(words: List<Word>, wallet: Wallet) {
        activity.startActivity(BackupWalletActivity.newIntent(activity, words, wallet))
        activity.finishAffinity()
    }

    fun navigateToHome(wallet: Wallet? = null) {
        activity.startActivity(MainActivity.newIntent(activity, wallet))
        activity.finishAffinity()
    }

    @JvmOverloads
    fun replaceFragment(
        fragment: Fragment,
        addToBackStack: Boolean = true,
        customAnimations: Int = WITHOUT_ANIMATION,
        container: Int = this.containerId
    ) {
        val transaction = fragmentManager.beginTransaction()
        if (customAnimations == IN_RIGHT_OUT_LEFT) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (customAnimations == IN_LEFT_OUT_RIGHT) {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        transaction.replace(container, fragment, fragment.javaClass.simpleName)
        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.simpleName)
        }
        transaction.commitAllowingStateLoss()
    }


    fun navigateToBalanceAddressScreen(wallet: Wallet?) {
        replaceFragment(BalanceAddressFragment.newInstance(wallet))
    }

    fun navigateToSwap(wallet: Wallet?, container: Int) {
        replaceFragment(SwapFragment.newInstance(wallet), false, WITHOUT_ANIMATION, container)
    }

    fun navigateToTokenSearch(
        container: Int,
        wallet: Wallet?,
        isSourceToken: Boolean = false
    ) {
        replaceFragment(
            TokenSearchFragment.newInstance(wallet, isSourceToken),
            true,
            WITHOUT_ANIMATION,
            container
        )
    }

    fun navigateToSwapConfirmationScreen() {
        activity.startActivity(SwapConfirmActivity.newIntent(activity))
    }

    companion object {
        const val IN_RIGHT_OUT_LEFT = 1
        const val IN_LEFT_OUT_RIGHT = -1
        const val WITHOUT_ANIMATION = 0
    }
}
