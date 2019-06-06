package com.kyberswap.android.presentation.main.setting


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentAddContactBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.SaveContactState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.isContact
import javax.inject.Inject


class AddContactFragment : BaseFragment() {

    private lateinit var binding: FragmentAddContactBinding

    @Inject
    lateinit var navigator: Navigator


    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var wallet: Wallet? = null
    private var address: String? = null


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AddContactViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = arguments!!.getParcelable(WALLET_PARAM)
        address = arguments!!.getString(ADDRESS_PARAM)
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

        binding.edtAddress.setText(address)
        binding.imgBack.setOnClickListener {
            activity!!.onBackPressed()


        binding.tvSave.setOnClickListener {
            if (binding.edtAddress.text.isNullOrEmpty())
                showAlert(getString(R.string.provide_receive_address))
            else if (!binding.edtAddress.text.toString().isContact()) {
                binding.edtAddress.error = getString(R.string.invalid_contact_address)
     else {
                viewModel.save(
                    wallet!!.address,
                    binding.edtName.text.toString(),
                    binding.edtAddress.text.toString()
                )
    


        viewModel.saveContactCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveContactState.Loading)
                when (state) {
                    is SaveContactState.Success -> {
                        activity!!.onBackPressed()
            
                    is SaveContactState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                        Toast.makeText(
                            activity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
            
        
    
)

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

    companion object {
        private const val WALLET_PARAM = "param_wallet"
        private const val ADDRESS_PARAM = "param_address"
        fun newInstance(wallet: Wallet?, address: String) =
            AddContactFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WALLET_PARAM, wallet)
                    putString(ADDRESS_PARAM, address)
        
    
    }


}
