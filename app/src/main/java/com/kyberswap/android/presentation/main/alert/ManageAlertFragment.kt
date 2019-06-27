package com.kyberswap.android.presentation.main.alert


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentManageAlertBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class ManageAlertFragment : BaseFragment() {

    private lateinit var binding: FragmentManageAlertBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val handler by lazy {
        Handler()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MangeAlertViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentManageAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getAlert()

        binding.rvAlert.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        val alertAdapter =
            ManageAlertAdapter(appExecutors, handler,
                {
                    navigator.navigateToPriceAlertScreen(
                        currentFragment, it
                    )
        , {
                    navigator.navigateToPriceAlertScreen(
                        currentFragment, it
                    )

        , {
                    dialogHelper.showConfirmDeleteAlert(
                        {
                            viewModel.deleteAlert(it)
                
                    )

        )
        binding.rvAlert.adapter = alertAdapter

        binding.rvTrigger.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )


        val triggerAlertAdapter = ManageTriggerAlertAdapter(appExecutors, handler) {
            dialogHelper.showConfirmDeleteAlert(
                {
                    viewModel.deleteAlert(it)
        
            )


        viewModel.deleteAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DeleteAlertsState.Loading)
                when (state) {
                    is DeleteAlertsState.Success -> {
                        showAlert(getString(R.string.delete_alert_success))
            
                    is DeleteAlertsState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.rvTrigger.adapter = triggerAlertAdapter

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertsState.Success -> {
                        alertAdapter.submitAlerts(state.alerts.filter {
                            !it.isFilled
                )
                        triggerAlertAdapter.submitAlerts(state.alerts.filter {
                            it.isFilled
                )
            
                    is GetAlertsState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)


        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()


        binding.imgAdd.setOnClickListener {
            navigator.navigateToPriceAlertScreen(
                currentFragment
            )


        binding.imgLeaderBoard.setOnClickListener {
            navigator.navigateToLeaderBoard(
                currentFragment
            )


        binding.imgSetting.setOnClickListener {
            showAlert(getString(R.string.to_do))



    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            ManageAlertFragment()
    }


}
