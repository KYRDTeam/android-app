package com.kyberswap.android.presentation.main.profile.kyc


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentKycSubmitBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class SubmitFragment : BaseFragment() {

    private lateinit var binding: FragmentKycSubmitBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors


    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val handler by lazy {
        Handler()
    }

    private var user: UserInfo? = null

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SubmitViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentKycSubmitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        binding.imgBack.setOnClickListener {
            onBackPress()
        }

        viewModel.getUserInfo()
        viewModel.getUserInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        user = state.userInfo
                        if (binding.info != state.userInfo?.kycInfo) {
                            binding.info = state.userInfo?.kycInfo
                            val info = binding.info
                            info?.let {

                                binding.lnBackSide.visibility =
                                    if (it.isPassport) View.GONE else View.VISIBLE

                                val photoProofAddress =
                                    info.photoProofAddress.removePrefix(BASE64_PREFIX)
                                displayImage(photoProofAddress, binding.imgPhotoProofAddress)

                                val photoIdentityFrontSide =
                                    info.photoIdentityFrontSide.removePrefix(BASE64_PREFIX)
                                displayImage(photoIdentityFrontSide, binding.imgPassportFrontSide)

                                val photoIdentityBackSide =
                                    info.photoIdentityBackSide.removePrefix(BASE64_PREFIX)
                                displayImage(photoIdentityBackSide, binding.imgPassportBackSide)

                                val photoSelfie = info.photoSelfie.removePrefix(BASE64_PREFIX)
                                displayImage(photoSelfie, binding.imgPassportHolding)

                            }


                        }
                    }
                    is UserInfoState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

        binding.tvSubmit.setOnClickListener {
            user?.let {
                viewModel.submit(it)
            }
        }

        viewModel.submitUserInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SavePersonalInfoState.Loading)
                when (state) {
                    is SavePersonalInfoState.Success -> {
                        navigator.navigateToVerification(currentFragment)
                    }
                    is SavePersonalInfoState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

    }

    private fun displayImage(stringImage: String, imageView: ImageView) {
        if (stringImage.isEmpty()) return
        viewModel.decode(stringImage, imageView)
        viewModel.decodeImageCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DecodeBase64State.Loading)
                when (state) {
                    is DecodeBase64State.Success -> {
                        glideDisplayImage(state.byteArray, state.imageView)
                    }
                    is DecodeBase64State.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })
    }


    fun onBackPress() {
        val fm = (activity as MainActivity).getCurrentFragment()?.childFragmentManager
        if (fm != null) {
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
        }
        navigator.navigateToPassport(currentFragment)

    }

    private fun glideDisplayImage(byteArray: ByteArray, imageView: ImageView?) {
        imageView?.let {
            Glide.with(it)
                .load(byteArray)
                .into(it)
        }
    }

    companion object {
        private const val BASE64_PREFIX = "data:image/jpeg;base64,"
        fun newInstance() =
            SubmitFragment()
    }


}
