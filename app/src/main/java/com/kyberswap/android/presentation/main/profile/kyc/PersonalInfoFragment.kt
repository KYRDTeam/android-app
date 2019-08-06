package com.kyberswap.android.presentation.main.profile.kyc


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
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
import com.kyberswap.android.util.ext.toDate
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

    private var currentSelectedView: View? = null

    private var hasLocalImage: Boolean = false


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

    private var currentDisplayString: String? = null

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

        viewModel.fetchUserInfo()

        viewModel.getUserInfoCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
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
                            occupationCode?.let {
                                if (it.isNotEmpty()) {
                                    binding.edtOccupationCode.setText("$occupationCode - ${occupationCodes[occupationCode]}")
                                }

                            }

                            industryCode?.let {
                                if (it.isNotEmpty()) {
                                    binding.edtIndus.setText("$industryCode - ${industrialCodes[industryCode]}")
                                }
                            }

                            val tin = binding.info?.haveTaxIdentification
                            tin?.let {
                                if (it) {
                                    binding.rgTin.check(R.id.rbYes)
                                    binding.edtTin.isEnabled = true
                                } else {
                                    binding.rgTin.check(R.id.rbNo)
                                    binding.edtTin.isEnabled = false
                                }
                            }

                            if (!binding.info?.photoProofAddress.isNullOrEmpty() && !hasLocalImage) {
                                if (stringImage != binding.info?.photoProofAddress?.removePrefix(
                                        BASE64_PREFIX
                                    )
                                ) {
                                    this.stringImage =
                                        binding.info?.photoProofAddress?.removePrefix(BASE64_PREFIX)
                                }
                            }

                            this.stringImage?.let { it1 -> displayImage(it1) }
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

        viewModel.savePersonalInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SavePersonalInfoState.Loading)
                when (state) {
                    is SavePersonalInfoState.Success -> {
                        navigator.navigateToPassport(currentFragment)
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


        viewModel.saveKycInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveKycInfoState.Success -> {
                        navigateToSearch()
                    }
                    is SaveKycInfoState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })


        viewModel.compositeDisposable.add(
            binding.rgTin.checkedChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    val enable = R.id.rbYes == it
                    binding.edtTin.isEnabled = enable
                    if (enable) {
                        binding.edtTin.requestFocus()
                    } else {
                        binding.edtTin.setText("")
                    }

                })



        binding.edtNationality.setOnClickListener {
            currentSelectedView = it
            saveCurrentKycInfo()
        }

        binding.edtCountryResident.setOnClickListener {
            currentSelectedView = it
            saveCurrentKycInfo()

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
            currentSelectedView = it
            saveCurrentKycInfo()

        }

        binding.edtProofAddress.setOnClickListener {
            currentSelectedView = it
            saveCurrentKycInfo()
        }

        binding.imgSrcFund.setOnClickListener {
            currentSelectedView = it
            saveCurrentKycInfo()

        }

        binding.edtIndus.setOnClickListener {
            currentSelectedView = it
            saveCurrentKycInfo()
        }

        binding.edtTaxCountry.setOnClickListener {
            currentSelectedView = it
            saveCurrentKycInfo()

        }

        binding.imgBack.setOnClickListener {
            onBackPress()

        }

        viewModel.compositeDisposable.add(binding.edtFirstName.textChanges().subscribe {
            binding.ilFirstName.error = null
        })

        viewModel.compositeDisposable.add(binding.edtLastName.textChanges().subscribe {
            binding.ilLastName.error = null
        })

        viewModel.compositeDisposable.add(binding.edtNationality.textChanges().subscribe {
            binding.ilNationality.error = null
        })


        viewModel.compositeDisposable.add(binding.edtCountryResident.textChanges().subscribe {
            binding.ilCountry.error = null
        })


        viewModel.compositeDisposable.add(binding.edtBod.textChanges().subscribe {
            binding.ilDob.error = null
        })


        viewModel.compositeDisposable.add(binding.edtResidentAddress.textChanges().subscribe {
            binding.ilResidentialAddress.error = null
        })


        viewModel.compositeDisposable.add(binding.edtCityResident.textChanges().subscribe {
            binding.ilCity.error = null
        })

        viewModel.compositeDisposable.add(binding.edtPostalCode.textChanges().subscribe {
            binding.ilZipCode.error = null
        })

        viewModel.compositeDisposable.add(binding.edtProofAddress.textChanges().subscribe {
            binding.ilDocumentProofAddress.error = null
        })



        binding.tvNext.setOnClickListener {


            when {
                edtFirstName.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_first_name_required)
                    showError(error)
                    binding.ilFirstName.error = error

                }

                edtLastName.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_last_name_required)
                    showError(error)
                    binding.ilLastName.error = error
                }

                edtBod.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_dob_required)
                    showError(error)
                    binding.ilDob.error = error
                }

                viewModel.inValidDob(edtBod.text.toString().toDate()) -> {
                    val error = getString(R.string.kyc_dob_18_year_old)
                    showError(getString(R.string.kyc_dob_18_year_old))
                    binding.ilDob.error = error
                }

                edtNationality.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_nationality_required)
                    showError(error)
                    binding.ilNationality.error = error
                }


                edtCountryResident.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_country_resident_required)
                    showError(error)
                    binding.ilCountry.error = error
                }


                edtResidentAddress.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_residental_address_required)
                    showError(error)
                    binding.ilResidentialAddress.error = error
                }

                edtCityResident.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_city_required)
                    showError(error)
                    binding.ilCity.error = error
                }

                edtPostalCode.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_zip_code_required)
                    showError(error)
                    binding.ilZipCode.error = error
                }

                edtProofAddress.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_document_proof_address_required)
                    showError(error)
                    binding.ilDocumentProofAddress.error = error
                }

                edtSourceFund.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_source_of_fun_required)
                    showError(error)
                    binding.ilSourceFund.error = error
                }

                else -> {
                    viewModel.save(
                        binding.info?.copy(
                            firstName = edtFirstName.text.toString(),
                            middleName = edtMiddleName.text.toString(),
                            lastName = edtLastName.text.toString(),
                            nativeFullName = binding.edtFullName.text.toString(),
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
                            industryCode = binding.edtIndus.text.toString().split("-").firstOrNull()
                                ?: "",
                            taxResidencyCountry = binding.edtTaxCountry.text.toString(),
                            haveTaxIdentification = binding.rbYes.isChecked,
                            taxIdentificationNumber = binding.edtTaxCountry.text.toString(),
                            sourceFund = binding.edtSourceFund.text.toString()
                        )
                    )
                }

            }
        }

    }

    private fun saveCurrentKycInfo() {
        binding.info?.copy(
            firstName = edtFirstName.text.toString(),
            middleName = edtMiddleName.text.toString(),
            lastName = edtLastName.text.toString(),
            nativeFullName = binding.edtFullName.text.toString(),
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
            industryCode = binding.edtIndus.text.toString().split("-").firstOrNull()
                ?: "",
            taxResidencyCountry = binding.edtTaxCountry.text.toString(),
            haveTaxIdentification = binding.rbYes.isChecked,
            taxIdentificationNumber = binding.edtTaxCountry.text.toString(),
            sourceFund = binding.edtSourceFund.text.toString()
        )
            ?.let {
                viewModel.saveLocal(
                    it
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

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        viewModel.onCleared()
        super.onDestroyView()
    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        binding.edtBod.setText(
            String.format(
                getString(R.string.date_format_yyyy_mm_dd),
                year,
                monthOfYear + 1,
                dayOfMonth
            )
        )
    }

    private fun navigateToSearch() {
        when (currentSelectedView) {
            binding.edtNationality -> {
                kycData?.let {
                    navigator.navigateToSearch(
                        currentFragment,
                        it.nationalities,
                        KycInfoType.NATIONALITY
                    )
                }
            }

            binding.edtCountryResident -> {
                kycData?.let {
                    navigator.navigateToSearch(
                        currentFragment,
                        it.countries,
                        KycInfoType.COUNTRY_OF_RESIDENCE
                    )
                }
            }

            binding.edtOccupationCode -> {
                kycData?.let {
                    navigator.navigateToSearch(
                        currentFragment,
                        listOf(),
                        KycInfoType.OCCUPATION_CODE
                    )
                }
            }

            binding.edtProofAddress -> {
                kycData?.let {
                    navigator.navigateToSearch(
                        currentFragment,
                        it.proofAddress,
                        KycInfoType.PROOF_ADDRESS
                    )
                }
            }

            binding.imgSrcFund -> {
                kycData?.let {
                    navigator.navigateToSearch(
                        currentFragment,
                        it.sourceFunds,
                        KycInfoType.SOURCE_FUND
                    )
                }
            }

            binding.edtIndus -> {
                kycData?.let {
                    navigator.navigateToSearch(
                        currentFragment,
                        listOf(),
                        KycInfoType.INDUSTRY_CODE
                    )
                }
            }


            binding.edtTaxCountry -> {
                kycData?.let {
                    navigator.navigateToSearch(
                        currentFragment,
                        it.countries,
                        KycInfoType.TAX_RESIDENCY_COUNTRY
                    )
                }
            }

        }
    }

    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        returnedPhotos.first().file.absolutePath?.let {
            viewModel.resizeImage(it)
            viewModel.resizeImageCallback.observe(viewLifecycleOwner, Observer {
                it?.getContentIfNotHandled()?.let { state ->
                    showProgress(state == ResizeImageState.Loading)
                    when (state) {
                        is ResizeImageState.Success -> {
                            hasLocalImage = state.stringImage.isNotEmpty()
                            displayImage(state.stringImage)
                        }
                        is ResizeImageState.ShowError -> {
                            showAlert(
                                state.message ?: getString(R.string.something_wrong),
                                R.drawable.ic_info_error
                            )
                        }
                    }
                }
            })
        }
    }

    private fun displayImage(stringImage: String) {
        if (stringImage.isEmpty()) return
        binding.progressBar.visibility = View.VISIBLE
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
                        binding.progressBar.visibility = View.GONE
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                    }
                }
            }
        })

    }

    private fun glideDisplayImage(byteArray: ByteArray) {
        Glide.with(binding.imgAddress)
            .asBitmap()
            .load(
                byteArray
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.progressBar.visibility = View.GONE
                    binding.imgAddress.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    binding.progressBar.visibility = View.GONE
                    // this is called when imageView is cleared on lifecycle call or for
                    // some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView
                    // clear it here as you can no longer have the bitmap
                }
            })

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
