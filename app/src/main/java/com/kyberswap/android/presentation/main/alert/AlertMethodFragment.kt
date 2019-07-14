package com.kyberswap.android.presentation.main.alert


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentAlertMethodBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.AlertMethods
import com.kyberswap.android.domain.model.Email
import com.kyberswap.android.domain.model.Telegram
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertMethodsState
import com.kyberswap.android.presentation.main.profile.alert.UpdateAlertMethodsState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class AlertMethodFragment : BaseFragment() {

    private lateinit var binding: FragmentAlertMethodBinding

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

    private val defaultValue by lazy {
        getString(R.string.not_enable)
    }

    private var emailAdapter: ArrayAdapter<String>? = null

    private var telegramAdapter: ArrayAdapter<String>? = null

    private var alertMethods: AlertMethods? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AlertMethodViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAlertMethodBinding.inflate(inflater, container, false)
        return binding.root
    }


    fun getLoginStatus() {
        viewModel.getLoginStatus()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getLoginStatus()
        viewModel.getLoginStatusCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        if (!(state.userInfo != null && state.userInfo.uid > 0)) {
                            activity?.onBackPressed()
                        }
                    }
                    is UserInfoState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.getAlertMethods()
        viewModel.getAlertMethodsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetAlertMethodsState.Loading)
                when (state) {
                    is GetAlertMethodsState.Success -> {
                        if (alertMethods != state.alertMethods) {
                            alertMethods = state.alertMethods
                            setupEmails(state.alertMethods.emails)
                            setupTelegrams(state.alertMethods.telegram)
                        }
                    }
                    is GetAlertMethodsState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.imgDone.setOnClickListener {
            val selectedEmailItem = binding.spnEmail.selectedItem.toString()
            var updatedEmailList: List<Email>? = null
            var updatedTelegram: Telegram? = null
            alertMethods?.let { alerts ->
                updatedEmailList = alerts.emails.map {
                    it.copy(active = it.id == selectedEmailItem)
                }

            }

            if (binding.grTelegram.visibility == View.VISIBLE) {
                val selectedTelegramItem = binding.spnTelegram.selectedItem.toString()
                alertMethods?.let { alerts ->
                    updatedTelegram =
                        alerts.telegram.copy(active = alerts.telegram.name == selectedTelegramItem)

                }
            }

            updatedEmailList?.let {
                alertMethods = alertMethods?.copy(emails = it)
            }

            updatedTelegram?.let {
                alertMethods = alertMethods?.copy(telegram = it)
            }

            alertMethods?.let {
                viewModel.updateAlertMethods(it)
            }

        }

        viewModel.updateAlertMethodsCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == UpdateAlertMethodsState.Loading)
                when (state) {
                    is UpdateAlertMethodsState.Success -> {
                        showAlertWithoutIcon(
                            getString(R.string.title_success),
                            getString(R.string.alert_methods_update_success)
                        )
                    }
                    is UpdateAlertMethodsState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })
    }

    private fun setupEmails(emails: List<Email>) {
        if (activity == null) return
        val activeEmail = emails.find { it.active }?.id
        val emailList = mutableListOf(defaultValue)
        emailList.addAll(emails.map {
            it.id
        })

        emailAdapter = ArrayAdapter(
            activity,
            android.R.layout.simple_spinner_item,
            emailList
        )

        emailAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnEmail.adapter = emailAdapter
        if (activeEmail != null) {
            binding.spnEmail.setSelection(emailList.indexOf(activeEmail))
        }
    }

    private fun setupTelegrams(telegram: Telegram) {
        if (activity == null) return
        binding.grTelegram.visibility = if (telegram.id.isNotEmpty()) View.VISIBLE else View.GONE
        if (telegram.id.isNotEmpty()) {
            val telegramList = mutableListOf(defaultValue)
            if (telegram.name.isNotEmpty()) {
                telegramList.add(telegram.name)
            }

            telegramAdapter =
                ArrayAdapter(
                    activity,
                    android.R.layout.simple_spinner_item,
                    telegramList
                )
            telegramAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spnTelegram.adapter = telegramAdapter
            if (telegram.active) {
                binding.spnTelegram.setSelection(telegramList.indexOf(telegram.name))
            }
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    companion object {
        fun newInstance() =
            AlertMethodFragment()
    }


}
