package com.kyberswap.android.presentation.main.setting.wallet


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.util.Attributes
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentManageWalletBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class ManageWalletFragment : BaseFragment() {

    private lateinit var binding: FragmentManageWalletBinding

    @Inject
    lateinit var navigator: Navigator


    @Inject
    lateinit var viewModelFactory: ViewModelFactory


    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var dialogHelper: DialogHelper

    private lateinit var walletAdapter: ManageWalletAdapter

    private val handler by lazy {
        Handler()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ManageWalletViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        walletAdapter =
            ManageWalletAdapter(appExecutors, handler,
                {
                    dialogHelper.showBottomSheetManageWalletDialog(
                        walletAdapter.getData().size == 1,
                        {
                            viewModel.updateSelectedWallet(it)
                , {
                            navigator.navigateToEditWallet(currentFragment, it)
                , {

                            dialogHelper.showConfirmation(
                                getString(R.string.title_delete),
                                getString(R.string.delete_wallet_confirmation),
                                {
                                    viewModel.deleteWallet(it)
                        )


                )
        ,
                {

                    viewModel.updateSelectedWallet(it)
        ,
                {
                    navigator.navigateToEditWallet(currentFragment, it)
        ,
                {
                    dialogHelper.showConfirmation(
                        getString(R.string.title_delete),
                        getString(R.string.delete_wallet_confirmation),
                        {
                            viewModel.deleteWallet(it)
                )
        )



        binding.rvWallet.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        walletAdapter.mode = Attributes.Mode.Single
        binding.rvWallet.adapter = walletAdapter

        viewModel.getWallets()
        viewModel.getAllWalletStateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAllWalletState.Success -> {
                        walletAdapter.submitList(listOf())
                        walletAdapter.submitList(state.wallets)
            
                    is GetAllWalletState.ShowError -> {

            
        
    
)

        binding.imgAddWallet.setOnClickListener {
            dialogHelper.showBottomSheetDialog(
                {
                    dialogHelper.showConfirmation {
                        viewModel.createWallet()
            

        ,
                {
                    navigator.navigateToImportWalletPage()

        
            )


        viewModel.createWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == CreateWalletState.Loading)
                when (state) {
                    is CreateWalletState.Success -> {
                        showAlert(getString(R.string.create_wallet_success)) {
                            navigator.navigateToBackupWalletPage(state.words, state.wallet, true)
                

            
                    is CreateWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.deleteWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DeleteWalletState.Loading)
                when (state) {
                    is DeleteWalletState.Success -> {
                        showAlert(getString(R.string.delete_wallet_success))
                        if (state.verifyStatus.isEmptyWallet) {
                            navigator.navigateToLandingPage()
                            activity?.finishAffinity()
                
            
                    is DeleteWalletState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)


        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()

    }


    companion object {
        fun newInstance() =
            ManageWalletFragment()
    }


}
