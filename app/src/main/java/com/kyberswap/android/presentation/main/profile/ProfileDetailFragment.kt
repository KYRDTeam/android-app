package com.kyberswap.android.presentation.main.profile


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
import com.kyberswap.android.databinding.FragmentProfileDetailBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.alert.ManageAlertAdapter
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class ProfileDetailFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileDetailBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    private var user: UserInfo? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val handler by lazy {
        Handler()
    }

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ProfileDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments!!.getParcelable(USER_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.user = user
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
                    viewModel.deleteAlert(it)
        )
        binding.rvAlert.adapter = alertAdapter

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertsState.Success -> {
                        alertAdapter.submitAlerts(state.alerts.take(2))
            
                    is GetAlertsState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
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

        viewModel.getAlertsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertsState.Success -> {
                        alertAdapter.submitAlerts(state.alerts.take(2))
            
                    is GetAlertsState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        viewModel.logoutCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == LogoutState.Loading)
                when (state) {
                    is LogoutState.Success -> {
                        navigator.navigateToSignInScreen(currentFragment)
            
                    is LogoutState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
            
        
    
)

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()


        binding.tvLogout.setOnClickListener {
            viewModel.logout()


        binding.imgCreateAlert.setOnClickListener {
            navigator.navigateToPriceAlertScreen(
                currentFragment
            )



        binding.imgLeaderBoard.setOnClickListener {
            navigator.navigateToLeaderBoard(
                currentFragment
            )


        binding.tvMoreAlert.setOnClickListener {
            navigator.navigateToManageAlert(
                currentFragment
            )


    }

    companion object {
        private const val USER_PARAM = "user_param"
        fun newInstance(user: UserInfo?) =
            ProfileDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(USER_PARAM, user)
        
    
    }


}
