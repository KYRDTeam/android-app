package com.kyberswap.android.presentation.main.balance

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSendBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainPagerAdapter
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import net.cachapa.expandablelayout.ExpandableLayout
import javax.inject.Inject


class SendFragment : BaseFragment() {

    private lateinit var binding: FragmentSendBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var token: Token? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SendViewModel::class.java)
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
        token = arguments!!.getParcelable(TOKEN_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walletName = wallet?.name
        binding.token = token
        binding.tvAdvanceOption.setOnClickListener {
            binding.expandableLayout.expand()


        binding.imgClose.setOnClickListener {
            binding.expandableLayout.collapse()


        listOf(binding.imgTokenSource, binding.tvSource).forEach {
            it.setOnClickListener {
                navigator.navigateToTokenSearchFromSendTokenScreen(R.id.container, wallet)
    



        binding.tvAddContact.setOnClickListener {
            navigator.navigateToAddContactScreen(R.id.container, MainPagerAdapter.BALANCE)


        binding.expandableLayout.setOnExpansionUpdateListener { _, state ->
            if (state == ExpandableLayout.State.EXPANDED) {
                val animator = ObjectAnimator.ofInt(
                    binding.scView,
                    "scrollY",
                    binding.tvRevertNotification.bottom
                )

                animator.duration = 300
                animator.interpolator = AccelerateInterpolator()
                animator.start()
    


        binding.rbFast.isChecked = true
        binding.rbDefaultRate.isChecked = true

        binding.imgBack.setOnClickListener {
            activity!!.onBackPressed()


        binding.rvContact.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        val tokenAdapter =
            ContactAdapter(appExecutors) { contact ->

    
        binding.rvContact.adapter = tokenAdapter

        binding.imgQRCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()


    }

    private fun moveToSettingTab() {
        if (activity is MainActivity) {
            activity!!.bottomNavigation.currentItem = MainPagerAdapter.SETTING

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showAlert(getString(R.string.message_cancelled))
     else {
                binding.edtAddress.setText(result.contents.toString())
    
 else {
            super.onActivityResult(requestCode, resultCode, data)

    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        private const val TOKEN_PARAM = "token_param"
        fun newInstance(wallet: Wallet?, token: Token?) =
            SendFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putParcelable(TOKEN_PARAM, token)
        
    
    }
}
