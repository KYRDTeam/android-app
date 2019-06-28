package com.kyberswap.android.presentation.main.profile.kyc


import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentPassportBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.tbruyelle.rxpermissions2.RxPermissions
import pl.aprilapps.easyphotopicker.*
import javax.inject.Inject


class PassportFragment : BaseFragment() {

    private lateinit var binding: FragmentPassportBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors


    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var easyImage: EasyImage

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private var currentSelectedView: View? = null


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PassportViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPassportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val rxPermissions = RxPermissions(this)

        easyImage = EasyImage.Builder(this.context!!)
            .setChooserTitle(getString(R.string.upload_document))
            .setCopyImagesToPublicGalleryFolder(true)
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .setFolderName("kyc")
            .build()

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()


        binding.tvNext.setOnClickListener {
            navigator.navigateToSubmitKYC(currentFragment)


        binding.tvPassportFrontSide.setOnClickListener {
            dialogHelper.showBottomSheetPassportDialog(appExecutors)


        binding.tvPassportBackSide.setOnClickListener {
            dialogHelper.showBottomSheetPassportDialog(appExecutors)


        binding.tvPassportHolding.setOnClickListener {
            dialogHelper.showBottomSheetHoldPassportDialog(appExecutors)

        listOf(
            binding.tvBrowsePassportFontSide,
            binding.tvBrowsePassportBackSide,
            binding.tvBrowsePassportHolding
        )
            .forEach {
                it.setOnClickListener {
                    currentSelectedView = it
                    dialogHelper.showImagePickerBottomSheetDialog(
                        {
                            rxPermissions.request(Manifest.permission.CAMERA)
                                .subscribe { granted ->
                                    if (granted) {
                                        easyImage.openCameraForImage(this)
                             else {
                                        showAlert(getString(R.string.permission_required))
                            
                        
                ,
                        {
                            rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                                .subscribe { granted ->
                                    if (granted) {
                                        easyImage.openGallery(this)
                             else {
                                        showAlert(getString(R.string.permission_required))
                            
                        
                
                    )
        
    

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activity?.let {
            easyImage.handleActivityResult(requestCode, resultCode, data, it, object :
                DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    onPhotosReturned(imageFiles)
        

                override fun onImagePickerError(error: Throwable, source: MediaSource) {

                    error.printStackTrace()
        

                override fun onCanceled(source: MediaSource) {

        
    )

    }

    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        currentSelectedView?.let {
            val image = when (currentSelectedView) {
                binding.tvBrowsePassportBackSide -> binding.imgPassportBackSide
                binding.tvBrowsePassportFontSide -> binding.imgPassportFrontSide
                else -> binding.imgPassportHolding
    
            Glide.with(image).load(returnedPhotos.first().file).into(image)



    }

    companion object {
        fun newInstance() =
            PassportFragment()
    }


}
