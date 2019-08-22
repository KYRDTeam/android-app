package com.kyberswap.android.presentation.main.profile.kyc


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

    private var frontImageString: String? = null
    private var backImageString: String? = null
    private var selfieImageString: String? = null
    private var stringImage: String? = null

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
                        if (state.userInfo?.isLoaded == true) {
                            binding.isLoaded = state.userInfo.isLoaded
                        }
                        if (binding.info != state.userInfo?.kycInfo) {
                            binding.info = state.userInfo?.kycInfo
                            val info = binding.info
                            info?.let {

                                binding.lnBackSide.visibility =
                                    if (it.isPassport) View.GONE else View.VISIBLE




                                if (info.photoIdentityFrontSide.isNotEmpty()) {
                                    this.frontImageString =
                                        info.photoIdentityFrontSide.removePrefix(BASE64_PREFIX)
                                }
                                displayImage(this.frontImageString, binding.imgPassportFrontSide)

                                info.photoIdentityBackSide.removePrefix(BASE64_PREFIX)

                                if (info.photoIdentityBackSide.isNotEmpty()) {
                                    this.backImageString =
                                        info.photoIdentityBackSide.removePrefix(BASE64_PREFIX)
                                }

                                displayImage(this.backImageString, binding.imgPassportBackSide)

                                info.photoSelfie.removePrefix(BASE64_PREFIX)

                                if (info.photoSelfie.isNotEmpty()) {
                                    this.selfieImageString =
                                        info.photoSelfie.removePrefix(BASE64_PREFIX)
                                }

                                displayImage(this.selfieImageString, binding.imgPassportHolding)


                                if (info.photoProofAddress.isNotEmpty()) {
                                    this.stringImage =
                                        info.photoProofAddress.removePrefix(
                                            BASE64_PREFIX
                                        )
                                }

                                displayImage(this.stringImage, binding.imgPhotoProofAddress)

//                                val photoProofAddress =
//                                    info.photoProofAddress.removePrefix(BASE64_PREFIX)
//                                displayImage(photoProofAddress, binding.imgPhotoProofAddress)
//
//                                val photoIdentityFrontSide =
//                                    info.photoIdentityFrontSide.removePrefix(BASE64_PREFIX)
//                                displayImage(photoIdentityFrontSide, binding.imgPassportFrontSide)
//
//                                val photoIdentityBackSide =
//                                    info.photoIdentityBackSide.removePrefix(BASE64_PREFIX)
//                                displayImage(photoIdentityBackSide, binding.imgPassportBackSide)
//
//                                val photoSelfie = info.photoSelfie.removePrefix(BASE64_PREFIX)
//                                displayImage(photoSelfie, binding.imgPassportHolding)

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

    private fun showLoadingImage(isShown: Boolean, imageView: ImageView?) {
        imageView?.let {
            when (it) {
                binding.imgPassportFrontSide -> binding.progressBarPassportFrontSide.visibility =
                    if (isShown) View.VISIBLE else View.GONE
                binding.imgPassportBackSide -> binding.progressBarPassportBackSide.visibility =
                    if (isShown) View.VISIBLE else View.GONE
                binding.imgPassportHolding -> binding.progressBarPassportHolding.visibility =
                    if (isShown) View.VISIBLE else View.GONE
                binding.imgPhotoProofAddress -> binding.progressBarProofAddress.visibility =
                    if (isShown) View.VISIBLE else View.GONE
            }
        }
    }

    private fun displayImage(stringImage: String?, imageView: ImageView) {
        if (stringImage.isNullOrEmpty()) return
        showLoadingImage(true, imageView)
        viewModel.decode(stringImage, imageView)
        viewModel.decodeImageCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DecodeBase64State.Loading)
                when (state) {
                    is DecodeBase64State.Success -> {
                        glideDisplayImage(state.byteArray, state.imageView)
                    }
                    is DecodeBase64State.ShowError -> {
                        showLoadingImage(false, imageView)
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
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        showLoadingImage(false, imageView)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        showLoadingImage(false, imageView)
                        return false
                    }
                })
                .into(it)
        }
    }

    companion object {
        private const val BASE64_PREFIX = "data:image/jpeg;base64,"
        fun newInstance() =
            SubmitFragment()
    }
}
