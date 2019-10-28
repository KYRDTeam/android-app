package com.kyberswap.android.presentation.main.profile.kyc


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
import pl.aprilapps.easyphotopicker.ChooserType
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.util.Calendar
import java.util.Locale
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

    private val sourceFunds by lazy {
        resources.getStringArray(R.array.source_funds)
    }

    private val sourceFundsKeys by lazy {
        resources.getStringArray(R.array.source_funds_key)
    }

    private val proofAddress by lazy {
        resources.getStringArray(R.array.proof_address)
    }

    private val proofAddressKeys by lazy {
        resources.getStringArray(R.array.proof_address_key)
    }

    private var currentByteArray: ByteArray? = null

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
            .setChooserTitle(getString(R.string.browse))
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
                        if (state.userInfo?.isLoaded == true) {
                            binding.isLoaded = state.userInfo.isLoaded
                        }
                        if (binding.info?.hasSamePersonalInfo(state.userInfo?.kycInfo) != true) {
                            binding.info = state.userInfo?.kycInfo
                            val gender = binding.info?.gender
                            if (gender == true) {
                                binding.rgGender.check(R.id.rbMale)
                            } else {
                                binding.rgGender.check(R.id.rbFemale)
                            }

                            val sourceFundIndex = sourceFundsKeys.indexOf(binding.info?.sourceFund)
                            if (sourceFundIndex >= 0) {
                                edtSourceFund.setText(sourceFunds[sourceFundIndex])
                            } else {
                                edtSourceFund.setText(binding.info?.sourceFund)
                            }

                            val proofAddressIndex =
                                proofAddressKeys.indexOf(binding.info?.documentProofAddress)
                            if (proofAddressIndex >= 0) {
                                edtProofAddress.setText(proofAddress[proofAddressIndex])
                            } else {
                                edtProofAddress.setText(binding.info?.documentProofAddress)
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

                            this.stringImage?.let { it1 ->
                                displayImage(it1)
                            }
                        }
                    }
                    is UserInfoState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
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
            fragmentManager?.let { it1 -> dpd.show(it1, "Datepickerdialog") }
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

        viewModel.compositeDisposable.add(
            binding.edtFirstName.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilFirstName.error = null
                })

        viewModel.compositeDisposable.add(
            binding.edtLastName.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilLastName.error = null
                })

        viewModel.compositeDisposable.add(
            binding.edtNationality.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilNationality.error = null
                })


        viewModel.compositeDisposable.add(
            binding.edtCountryResident.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilCountry.error = null
                })


        viewModel.compositeDisposable.add(
            binding.edtBod.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilDob.error = null
                })


        viewModel.compositeDisposable.add(
            binding.edtResidentAddress.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilResidentialAddress.error = null
                })


        viewModel.compositeDisposable.add(
            binding.edtCityResident.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilCity.error = null
                })

        viewModel.compositeDisposable.add(
            binding.edtPostalCode.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilZipCode.error = null
                })

        viewModel.compositeDisposable.add(
            binding.edtProofAddress.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    binding.ilDocumentProofAddress.error = null
                })

        viewModel.compositeDisposable.add(binding.edtSourceFund.textChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                binding.ilSourceFund.error = null
            })



        binding.tvNext.setOnClickListener {

            when {
                edtFirstName.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_first_name_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_name), message = error)
                    binding.ilFirstName.error = error
                }

                edtLastName.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_last_name_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_name), message = error)
                    binding.ilLastName.error = error
                }

                edtBod.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_dob_required)
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_date_of_birth),
                        message = error
                    )
                    binding.ilDob.error = error
                }

                viewModel.inValidDob(edtBod.text.toString().toDate()) -> {
                    val error = getString(R.string.kyc_dob_18_year_old)
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_date_of_birth),
                        message = getString(R.string.kyc_dob_18_year_old)
                    )
                    binding.ilDob.error = error
                }

                edtNationality.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_nationality_required)
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_nationality),
                        message = error
                    )
                    binding.ilNationality.error = error
                }

                edtNationality.text.toString().toLowerCase(Locale.getDefault()) == getString(R.string.nationality_singaporean).toLowerCase(
                    Locale.getDefault()
                ) -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_nationality),
                        message = String.format(
                            getString(R.string.kyc_not_support),
                            getString(R.string.country_singapore)
                        )
                    )
                }

                edtCityResident.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_city_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_input), message = error)
                    binding.ilCity.error = error
                }

                edtPostalCode.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_zip_code_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_input), message = error)
                    binding.ilZipCode.error = error
                }

                edtProofAddress.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_document_proof_address_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_input), message = error)
                    binding.ilDocumentProofAddress.error = error
                }

                stringImage.isNullOrEmpty() -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_input), message = getString(
                            R.string.invalid_photo_address
                        )
                    )
                }

                edtSourceFund.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_source_of_fun_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_input), message = error)
                    binding.ilSourceFund.error = error
                }

                edtCountryResident.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_country_resident_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_input), message = error)
                    binding.ilCountry.error = error
                }

                edtCountryResident.text.toString().toLowerCase(Locale.getDefault()) == getString(R.string.country_singapore).toLowerCase(
                    Locale.getDefault()
                ) -> {
                    showAlertWithoutIcon(
                        title = getString(R.string.invalid_input), message = String.format(
                            getString(R.string.kyc_not_support),
                            getString(R.string.country_singapore)
                        )
                    )
                }

                edtResidentAddress.text.toString().isBlank() -> {
                    val error = getString(R.string.kyc_residental_address_required)
                    showAlertWithoutIcon(title = getString(R.string.invalid_input), message = error)
                    binding.ilResidentialAddress.error = error
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
                            documentProofAddress =
                            if (proofAddress.indexOf(binding.edtProofAddress.text.toString()) >= 0) {
                                proofAddressKeys[proofAddress.indexOf(binding.edtProofAddress.text.toString())]
                            } else binding.edtProofAddress.text.toString(),
                            photoProofAddress = BASE64_PREFIX + stringImage,
                            occupationCode = binding.edtOccupationCode.text.toString().split("-").firstOrNull()
                                ?: "",
                            industryCode = binding.edtIndus.text.toString().split("-").firstOrNull()
                                ?: "",
                            taxResidencyCountry = binding.edtTaxCountry.text.toString(),
                            haveTaxIdentification = binding.rbYes.isChecked,
                            taxIdentificationNumber = binding.edtTin.text.toString(),
                            sourceFund =
                            if (sourceFunds.indexOf(binding.edtSourceFund.text.toString()) >= 0) {
                                sourceFundsKeys[sourceFunds.indexOf(binding.edtSourceFund.text.toString())]
                            } else
                                binding.edtSourceFund.text.toString()
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
            documentProofAddress = if (proofAddress.indexOf(binding.edtProofAddress.text.toString()) >= 0) {
                proofAddressKeys[proofAddress.indexOf(binding.edtProofAddress.text.toString())]
            } else binding.edtProofAddress.text.toString(),
            photoProofAddress = BASE64_PREFIX + stringImage,
            occupationCode = binding.edtOccupationCode.text.toString().split("-").firstOrNull()
                ?: "",
            industryCode = binding.edtIndus.text.toString().split("-").firstOrNull()
                ?: "",
            taxResidencyCountry = binding.edtTaxCountry.text.toString(),
            haveTaxIdentification = binding.rbYes.isChecked,
            taxIdentificationNumber = binding.edtTin.text.toString(),
            sourceFund = if (sourceFunds.indexOf(binding.edtSourceFund.text.toString()) >= 0) {
                sourceFundsKeys[sourceFunds.indexOf(binding.edtSourceFund.text.toString())]
            } else
                binding.edtSourceFund.text.toString()
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
        currentByteArray = null
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
                            showError(
                                state.message ?: getString(R.string.something_wrong)
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
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private fun glideDisplayImage(byteArray: ByteArray) {
        if (currentByteArray?.contentEquals(byteArray) == true) {
            return
        }
        currentByteArray = byteArray
//        Glide.with(binding.imgAddress)
//            .asBitmap()
//            .load(
//                byteArray
//            )
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    binding.progressBar.visibility = View.GONE
//                    binding.imgAddress.setImageBitmap(resource)
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    binding.progressBar.visibility = View.GONE
//                    // this is called when imageView is cleared on lifecycle call or for
//                    // some other reason.
//                    // if you are referencing the bitmap somewhere else too other than this imageView
//                    // clear it here as you can no longer have the bitmap
//                }
//            })
//        Glide.with(binding.imgAddress)
//            .asBitmap()
//            .load(byteArray)
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    binding.progressBar.visibility = View.GONE
//                    binding.imgAddress.setImageBitmap(resource)
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    binding.progressBar.visibility = View.GONE
//                    // this is called when imageView is cleared on lifecycle call or for
//                    // some other reason.
//                    // if you are referencing the bitmap somewhere else too other than this imageView
//                    // clear it here as you can no longer have the bitmap
//                }
//            })

        Glide.with(binding.imgAddress)
            .load(byteArray)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }
            })

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
