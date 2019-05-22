package com.kyberswap.android.presentation.main.setting


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentAddContactBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.util.di.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class AddContactFragment : BaseFragment() {

    private lateinit var binding: FragmentAddContactBinding

    @Inject
    lateinit var navigator: Navigator


    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var tab: Int? = null

    private val handler by lazy {
        Handler()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SettingViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tab = arguments!!.getInt(PARAM_TAB)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.imgQRCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .initiateScan()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showAlert(getString(R.string.message_cancelled))
     else {
                binding.edtAddress.setText(result.contents.toString())
    
 else {
            super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onDestroyView() {
        if (activity is MainActivity && tab != null) {
            handler.post {
                activity!!.bottomNavigation.currentItem = tab!!
    

        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val PARAM_TAB = "param_tab"
        fun newInstance(tab: Int) =
            AddContactFragment().apply {
                arguments = Bundle().apply {
                    putInt(PARAM_TAB, tab)
        
    
    }


}
