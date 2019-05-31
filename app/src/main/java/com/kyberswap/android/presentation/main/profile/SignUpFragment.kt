package com.kyberswap.android.presentation.main.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSignupBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_signup.*
import javax.inject.Inject


class SignUpFragment : BaseFragment() {

    private lateinit var binding: FragmentSignupBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SignUpViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.btnRegister.setOnClickListener {
            viewModel.signUp(
                edtEmail.text.toString(),
                edtDisplayName.text.toString(),
                edtPassword.text.toString(),
                cbSubscription.isChecked
            )
        }

        viewModel.signUpCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SignUpState.Loading)
                when (state) {
                    is SignUpState.Success -> {
                        showAlert(state.registerStatus.message)
                    }
                    is SignUpState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })

    }

    companion object {
        private const val WALLET_PARAM = "wallet_param"
        fun newInstance(wallet: Wallet?) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                }
            }
    }


}
