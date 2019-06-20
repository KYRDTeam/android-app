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
import com.kyberswap.android.presentation.main.limitorder.*
import com.kyberswap.android.presentation.main.profile.*
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

    fun navigateToHome(wallet: Wallet? = null, user: UserInfo? = null) {
        activity.startActivity(MainActivity.newIntent(activity, wallet, user))
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


    fun navigateToTokenSearchFromLimitOrder(
        currentFragment: Fragment?,
        wallet: Wallet?,
        isSourceToken: Boolean = false
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            LimitOrderTokenSearchFragment.newInstance(wallet, isSourceToken)
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
        newFragment: Fragment,
        addToBackStack: Boolean = true
    ) {
        currentFragment?.let {
            currentFragment.view?.id?.let { id ->
                replaceFragment(
                    currentFragment.childFragmentManager,
                    id,
                    newFragment,
                    addToBackStack
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

    fun navigateToSignInScreen(currentFragment: Fragment?, wallet: Wallet?) {
        navigateByChildFragmentManager(currentFragment, ProfileFragment.newInstance(wallet), false)
    }

    fun navigateToTermAndCondition() {
        activity.startActivity(TermConditionActivity.newIntent(activity))
    }

    fun navigateToLimitOrderSuggestionScreen(currentFragment: Fragment?, wallet: Wallet?) {
        navigateByChildFragmentManager(
            currentFragment,
            LimitOrderSuggestionFragment.newInstance(wallet)
        )
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

    fun navigateToProfileDetail(
        currentFragment: Fragment?,
        wallet: Wallet?,
        user: UserInfo?
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            ProfileDetailFragment.newInstance(wallet, user),
            false
        )

    }

    private fun replaceFragment(
        fragmentManager: FragmentManager,
        container: Int,
        fragment: Fragment,
        addToBackStack: Boolean = true
    ) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(container, fragment, fragment.javaClass.simpleName)
        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.simpleName)

        transaction.commitAllowingStateLoss()
    }

    fun navigateToManageOrder(currentFragment: Fragment?, wallet: Wallet?) {
        navigateByChildFragmentManager(
            currentFragment,
            ManageOrderFragment.newInstance(wallet)
        )
    }

    fun navigateToLimitOrderFilterScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {

        navigateByChildFragmentManager(
            currentFragment,
            FilterLimitOrderFragment.newInstance(wallet)
        )
    }

    fun navigateToOrderConfirmScreen(
        currentFragment: Fragment?,
        wallet: Wallet?
    ) {

        navigateByChildFragmentManager(
            currentFragment,
            OrderConfirmFragment.newInstance(wallet)
        )
    }

    fun navigateToConvertFragment(
        currentFragment: Fragment?,
        wallet: Wallet?,
        order: LocalLimitOrder?
    ) {
        navigateByChildFragmentManager(
            currentFragment,
            ConvertFragment.newInstance(wallet, order)
        )
    }

    companion object {
        const val IN_RIGHT_OUT_LEFT = 1
        const val IN_LEFT_OUT_RIGHT = -1
        const val WITHOUT_ANIMATION = 0
    }
}
