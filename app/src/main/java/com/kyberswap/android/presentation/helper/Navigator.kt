package com.kyberswap.android.presentation.helper

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kyberswap.android.R
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.presentation.landing.LandingActivity
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.balance.address.BalanceAddressFragment
import com.kyberswap.android.presentation.main.balance.chart.ChartFragment
import com.kyberswap.android.presentation.main.balance.send.SendConfirmActivity
import com.kyberswap.android.presentation.main.balance.send.SendFragment
import com.kyberswap.android.presentation.main.profile.SignUpConfirmFragment
import com.kyberswap.android.presentation.main.profile.SignUpFragment
import com.kyberswap.android.presentation.main.setting.AddContactFragment
import com.kyberswap.android.presentation.main.setting.ContactFragment
import com.kyberswap.android.presentation.main.swap.SwapConfirmActivity
import com.kyberswap.android.presentation.main.swap.TokenSearchFragment
import com.kyberswap.android.presentation.main.transaction.*
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


    fun navigateToBalanceAddressScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {

        navigateByChildFragmentManager(currentFragment, BalanceAddressFragment.newInstance(wallet))
    }

    fun navigateToChartScreen(
        currentFragment: Fragment?,
        wallet: Wallet?,
        token: Token?
    ) {
        navigateByChildFragmentManager(currentFragment, ChartFragment.newInstance(wallet, token))
    }

    fun navigateToTokenSearchFromSwapTokenScreen(
        currentFragment: Fragment?,
        wallet: Wallet?,
        isSourceToken: Boolean = false
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            TokenSearchFragment.newInstance(wallet, false, isSourceToken)
        )
    }


    fun navigateToTokenSearchFromSendTokenScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {

        navigateByChildFragmentManager(
            currentFragment,
            TokenSearchFragment.newInstance(wallet, true, false)
        )
    }


    fun navigateToSwapConfirmationScreen(wallet: Wallet?) {
        activity.startActivity(SwapConfirmActivity.newIntent(activity, wallet))
    }


    fun navigateToSendConfirmationScreen(wallet: Wallet?) {
        activity.startActivity(SendConfirmActivity.newIntent(activity, wallet))
    }

    fun navigateToSendScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {
        navigateByChildFragmentManager(currentFragment, SendFragment.newInstance(wallet))
    }

    fun navigateToAddContactScreen(
        currentFragment: Fragment?,
        wallet: Wallet?,
        address: String = ""
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            AddContactFragment.newInstance(wallet, address)
        )
    }

    fun navigateToContactScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            ContactFragment.newInstance(wallet)
        )
    }

    fun navigateToTransactionScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {

        navigateByChildFragmentManager(currentFragment, TransactionFragment.newInstance(wallet))
    }


    fun navigateToTransactionFilterScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {

        navigateByChildFragmentManager(
            currentFragment,
            TransactionFilterFragment.newInstance(wallet)
        )
    }


    fun navigateToSwapTransactionScreen(
        currentFragment: Fragment?,
        wallet: Wallet?,
        transaction: Transaction?
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            TransactionDetailSwapFragment.newInstance(wallet, transaction)
        )


    }

    fun navigateToSendTransactionScreen(
        currentFragment: Fragment?,
        wallet: Wallet?,
        transaction: Transaction?
    ) {

        navigateByChildFragmentManager(
            currentFragment,
            TransactionDetailSendFragment.newInstance(wallet, transaction)
        )

    }

    fun navigateToReceivedTransactionScreen(
        currentFragment: Fragment?,
        wallet: Wallet?,
        transaction: Transaction?
    ) {

        navigateByChildFragmentManager(
            currentFragment,
            TransactionDetailReceiveFragment.newInstance(wallet, transaction)
        )
    }

    private fun navigateByChildFragmentManager(
        currentFragment: Fragment?,
        newFragment: Fragment
    ) {
        currentFragment?.let {
            currentFragment.view?.id?.let { id ->
                replaceFragment(
                    currentFragment.childFragmentManager,
                    id,
                    newFragment
                )
    


    }


    private fun navigateByFragmentManager(
        currentFragment: Fragment?,
        newFragment: Fragment
    ) {
        currentFragment?.fragmentManager?.let { fragmentManager ->
            currentFragment.view?.id?.let { id ->
                replaceFragment(
                    fragmentManager,
                    id,
                    newFragment
                )
    


    }

    fun navigateToSignUpScreen(currentFragment: Fragment?, wallet: Wallet?) {
        navigateByChildFragmentManager(currentFragment, SignUpFragment.newInstance(wallet))
    }

    fun navigateToSignUpConfirmScreen(
        currentFragment: Fragment?,
        wallet: Wallet?,
        socialInfo: SocialInfo?
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            SignUpConfirmFragment.newInstance(wallet, socialInfo)
        )

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
