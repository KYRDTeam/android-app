package com.kyberswap.android.presentation.helper

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.landing.LandingActivity
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.address.BalanceAddressFragment
import com.kyberswap.android.presentation.main.balance.chart.ChartFragment
import com.kyberswap.android.presentation.main.balance.send.SendConfirmActivity
import com.kyberswap.android.presentation.main.balance.send.SendFragment
import com.kyberswap.android.presentation.main.profile.SignUpFragment
import com.kyberswap.android.presentation.main.setting.AddContactFragment
import com.kyberswap.android.presentation.main.setting.ContactFragment
import com.kyberswap.android.presentation.main.swap.SwapConfirmActivity
import com.kyberswap.android.presentation.main.swap.TokenSearchFragment
import com.kyberswap.android.presentation.main.transaction.TransactionDetailReceiveFragment
import com.kyberswap.android.presentation.main.transaction.TransactionDetailSendFragment
import com.kyberswap.android.presentation.main.transaction.TransactionDetailSwapFragment
import com.kyberswap.android.presentation.main.transaction.TransactionFragment
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
 else if (customAnimations == IN_LEFT_OUT_RIGHT) {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)

        transaction.replace(container, fragment, fragment.javaClass.simpleName)
        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.simpleName)

        transaction.commitAllowingStateLoss()
    }


    fun navigateToBalanceAddressScreen(wallet: Wallet?) {
        replaceFragment(BalanceAddressFragment.newInstance(wallet))
    }

    fun navigateToChartScreen(wallet: Wallet?, token: Token?) {
        replaceFragment(ChartFragment.newInstance(wallet, token))
    }


    private fun navigateToTokenSearch(
        container: Int,
        wallet: Wallet?,
        isSend: Boolean = false,
        isSourceToken: Boolean = false

    ) {
        replaceFragment(
            TokenSearchFragment.newInstance(wallet, isSend, isSourceToken),
            true,
            WITHOUT_ANIMATION,
            container
        )
    }

    fun navigateToTokenSearchFromSwapTokenScreen(
        container: Int,
        wallet: Wallet?,
        isSourceToken: Boolean = false
    ) {
        navigateToTokenSearch(container, wallet, false, isSourceToken)
    }


    fun navigateToTokenSearchFromSendTokenScreen(
        container: Int,
        wallet: Wallet?
    ) {
        navigateToTokenSearch(container, wallet, true)
    }


    fun navigateToSwapConfirmationScreen(wallet: Wallet?) {
        activity.startActivity(SwapConfirmActivity.newIntent(activity, wallet))
    }


    fun navigateToSendConfirmationScreen(wallet: Wallet?) {
        activity.startActivity(SendConfirmActivity.newIntent(activity, wallet))
    }

    fun navigateToSendScreen(wallet: Wallet?) {
        replaceFragment(SendFragment.newInstance(wallet))
    }

    fun navigateToAddContactScreen(
        wallet: Wallet?,
        address: String = ""
    ) {
        replaceFragment(
            AddContactFragment.newInstance(wallet, address)
        )
    }

    fun navigateToContactScreen(
        wallet: Wallet?
    ) {
        replaceFragment(
            ContactFragment.newInstance(wallet)
        )
    }

    fun navigateToTransactionScreen(
        fragmentManager: FragmentManager,
        container: Int,
        wallet: Wallet?
    ) {
        replaceFragment(fragmentManager, container, TransactionFragment.newInstance(wallet))
    }

    fun navigateToSwapTransactionScreen(wallet: Wallet?, transaction: Transaction?) {
        replaceFragment(TransactionDetailSwapFragment.newInstance(wallet, transaction))
    }

    fun navigateToSendTransactionScreen(wallet: Wallet?, transaction: Transaction?) {
        replaceFragment(TransactionDetailSendFragment.newInstance(wallet, transaction))
    }

    fun navigateToReceivedTransactionScreen(wallet: Wallet?, transaction: Transaction?) {
        replaceFragment(TransactionDetailReceiveFragment.newInstance(wallet, transaction))
    }

    fun navigateToSignUpScreen(fragmentManager: FragmentManager, container: Int, wallet: Wallet?) {
        val fragment = SignUpFragment.newInstance(wallet)
        replaceFragment(fragmentManager, containerId, fragment)

    }

    private fun replaceFragment(
        fragmentManager: FragmentManager,
        container: Int,
        fragment: Fragment
    ) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(container, fragment, fragment.javaClass.simpleName)
        transaction.addToBackStack(fragment.javaClass.simpleName)
        transaction.commitAllowingStateLoss()
    }

    companion object {
        const val IN_RIGHT_OUT_LEFT = 1
        const val IN_LEFT_OUT_RIGHT = -1
        const val WITHOUT_ANIMATION = 0
        const val SWAP_CONFIRMATION = 1010
        const val SEND_CONFIRMATION = 1011
    }
}
