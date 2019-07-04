package com.kyberswap.android.presentation.main.profile.kyc


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentPersonalInfoBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.KycCode
import com.kyberswap.android.domain.model.KycData
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.loadJSONFromAssets
import com.tbruyelle.rxpermissions2.RxPermissions
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_personal_info.*
import pl.aprilapps.easyphotopicker.*
import java.util.*
import javax.inject.Inject


class PersonalInfoFragment : BaseFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentPersonalInfoBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val occupationCodes = mutableMapOf<String, String>()

    private val industrialCodes = mutableMapOf<String, String>()

    private lateinit var easyImage: EasyImage

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PersonalInfoViewModel::class.java)
    }

    private val gson by lazy {
        Gson()
    }
    private val kycData by lazy {
        context?.loadJSONFromAssets(KYC_DATA_FILE)?.let {
            gson.fromJson(it, KycData::class.java)
        }
    }

    private var stringImage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val rxPermissions = RxPermissions(this)

        easyImage = EasyImage.Builder(this.context!!)
            .setChooserTitle(getString(R.string.upload_document))
            .setCopyImagesToPublicGalleryFolder(true)
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .setFolderName("kyc")
            .build()
        binding.tvUploadAddress.setOnClickListener {
            dialogHelper.showImagePickerBottomSheetDialog(
                {
                    rxPermissions.request(Manifest.permission.CAMERA)
                        .subscribe { granted ->
                            if (granted) {
                                easyImage.openCameraForImage(this)
                            } else {
                                showAlert(getString(R.string.permission_required))
                            }
                        }
                },
                {
                    rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe { granted ->
                            if (granted) {
                                easyImage.openGallery(this)
                            } else {
                                showAlert(getString(R.string.permission_required))
                            }
                        }
                }
            )
        }

        context?.loadJSONFromAssets(KYC_OCCUPATION_CODE_FILE)?.let {
            val kycOccupationCode = gson.fromJson(it, KycCode::class.java)
            occupationCodes.clear()
            occupationCodes.putAll(kycOccupationCode.data)
        }

        context?.loadJSONFromAssets(KYC_INDUSTRY_CODE_FILE)?.let {
            val kycOccupationCode = gson.fromJson(it, KycCode::class.java)
            industrialCodes.clear()
            industrialCodes.putAll(kycOccupationCode.data)
        }

        viewModel.getUserInfo()

        viewModel.getUserInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is UserInfoState.Success -> {
                        if (binding.info != state.userInfo?.kycInfo) {
                            binding.info = state.userInfo?.kycInfo
                            val gender = binding.info?.gender
                            if (gender == true) {
                                binding.rgGender.check(R.id.rbMale)
                            } else {
                                binding.rgGender.check(R.id.rbFemale)
                            }

                            val occupationCode = binding.info?.occupationCode?.trim()
                            val industryCode = binding.info?.industryCode?.trim()
                            binding.edtOccupationCode.setText("$occupationCode - ${occupationCodes[occupationCode]}")
                            binding.edtIndus.setText("$industryCode - ${industrialCodes[industryCode]}")

                            val tin = binding.info?.haveTaxIdentification
                            tin?.let {
                                if (it) {
                                    binding.rgTin.check(R.id.rbYes)
                                } else {
                                    binding.rgTin.check(R.id.rbNo)
                                }
                            }

                            val stringImage =
                                binding.info?.photoProofAddress?.removePrefix(BASE64_PREFIX)

                            stringImage?.let { it1 -> displayImage(it1) }
                        }
                    }
                    is UserInfoState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })

        viewModel.savePersonalInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SavePersonalInfoState.Loading)
                when (state) {
                    is SavePersonalInfoState.Success -> {
                        navigator.navigateToPassport(currentFragment)
                    }
                    is SavePersonalInfoState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })


        binding.edtNationality.setOnClickListener {
            kycData?.let {
                navigator.navigateToSearch(
                    currentFragment,
                    it.nationalities,
                    KycInfoType.NATIONALITY
                )
            }
        }

        binding.edtCountryResident.setOnClickListener {
            kycData?.let {
                navigator.navigateToSearch(
                    currentFragment,
                    it.countries,
                    KycInfoType.COUNTRY_OF_RESIDENCE
                )
            }
        }

        binding.flToggle.setOnClickListener {
            binding.expandableLayout.toggle()
            binding.imgToggle.isSelected = !binding.imgToggle.isSelected
            val action = if (binding.expandableLayout.isExpanded) {
                getString(R.string.see_less)
            } else {
                getString(R.string.see_more)
            }

            binding.tvAction.text = action
        }

        binding.edtBod.setOnClickListener {
            val now = Calendar.getInstance()
            val dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show(fragmentManager, "Datepickerdialog")
        }

        binding.edtOccupationCode.setOnClickListener {
            kycData?.let {
                navigator.navigateToSearch(
                    currentFragment,
                    listOf(),
                    KycInfoType.OCCUPATION_CODE
                )
            }
        }

        binding.edtProofAddress.setOnClickListener {
            kycData?.let {
                navigator.navigateToSearch(
                    currentFragment,
                    it.proofAddress,
                    KycInfoType.PROOF_ADDRESS
                )
            }
        }

        binding.edtSourceFund.setOnClickListener {
            kycData?.let {
                navigator.navigateToSearch(
                    currentFragment,
                    it.sourceFunds,
                    KycInfoType.SOURCE_FUND
                )
            }
        }

        binding.edtIndus.setOnClickListener {
            kycData?.let {
                navigator.navigateToSearch(
                    currentFragment,
                    listOf(),
                    KycInfoType.INDUSTRY_CODE
                )
            }
        }

        binding.edtTaxCountry.setOnClickListener {
            kycData?.let {
                navigator.navigateToSearch(
                    currentFragment,
                    it.countries,
                    KycInfoType.TAX_RESIDENCY_COUNTRY
                )
            }
        }

        binding.imgBack.setOnClickListener {
            onBackPress()

        }

        binding.tvNext.setOnClickListener {
            viewModel.save(
                binding.info?.copy(
                    firstName = edtFirstName.text.toString(),
                    lastName = edtLastName.text.toString(),
                    nationality = edtNationality.text.toString(),
                    country = edtCountryResident.text.toString(),
                    dob = binding.edtBod.text.toString(),
                    gender = rbMale.isChecked,
                    residentialAddress = binding.edtResidentAddress.text.toString(),
                    city = binding.edtCityResident.text.toString(),
                    zipCode = binding.edtPostalCode.text.toString(),
                    documentProofAddress = binding.edtProofAddress.text.toString(),
                    photoProofAddress = BASE64_PREFIX + stringImage,
                    occupationCode = binding.edtOccupationCode.text.toString().split("-").firstOrNull()
                        ?: "",
                    industryCode = binding.edtIndus.text.toString().split("-").firstOrNull() ?: "",
                    taxResidencyCountry = binding.edtTaxCountry.text.toString(),
                    haveTaxIdentification = binding.rbYes.isChecked,
                    taxIdentificationNumber = binding.edtTaxCountry.text.toString()
                )
            )
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activity?.let {
            easyImage.handleActivityResult(requestCode, resultCode, data, it, object :
                DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    onPhotosReturned(imageFiles)
                }

                override fun onImagePickerError(error: Throwable, source: MediaSource) {

                    error.printStackTrace()
                }

                override fun onCanceled(source: MediaSource) {

                }
            })
        }
    }


    private fun onBackPress() {
        val fm = (activity as MainActivity).getCurrentFragment()?.childFragmentManager
        if (fm != null) {
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
        }

    }


    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        binding.edtBod.setText("$dayOfMonth/${monthOfYear + 1}/$year")
    }

    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        returnedPhotos.first().file.absolutePath?.let {
            viewModel.resizeImage(it)
            viewModel.resizeImageCallback.observe(viewLifecycleOwner, Observer {
                it?.getContentIfNotHandled()?.let { state ->
                    showProgress(state == ResizeImageState.Loading)
                    when (state) {
                        is ResizeImageState.Success -> {
                            displayImage(state.stringImage)
                        }
                        is ResizeImageState.ShowError -> {
                            showAlert(state.message ?: getString(R.string.something_wrong))
                        }
                    }
                }
            })
        }
    }

    private fun displayImage(stringImage: String) {
        this.stringImage = stringImage
        viewModel.decode(stringImage)
        viewModel.decodeImageCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == DecodeBase64State.Loading)
                when (state) {
                    is DecodeBase64State.Success -> {
                        glideDisplayImage(state.byteArray)
                    }
                    is DecodeBase64State.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })
    }

    private fun glideDisplayImage(byteArray: ByteArray) {
        Glide.with(binding.imgAddress)
            .load(
                byteArray
            )
            .into(binding.imgAddress)
    }

    companion object {
        private const val BASE64_PREFIX = "data:image/jpeg;base64,"
        private const val KYC_DATA_FILE = "kyc/kyc_data"
        private const val KYC_OCCUPATION_CODE_FILE = "kyc/occupation_code"
        private const val KYC_INDUSTRY_CODE_FILE = "kyc/industry_code"
        fun newInstance() =
            PersonalInfoFragment()
    }


}
