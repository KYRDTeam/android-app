package com.kyberswap.android.presentation.main.profile.kyc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentKycInfoSearchBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.KycCode
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.loadJSONFromAssets
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class KycInfoSearchFragment : BaseFragment() {
    private lateinit var binding: FragmentKycInfoSearchBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider


    private val data = mutableListOf<String>()


    private var kycInfoType: KycInfoType? = null

    private var currentSearchString = ""

    private val gson by lazy {
        Gson()
    }

    private val hash = mutableMapOf<String, String>()


    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(CountryViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data.addAll(arguments?.getStringArrayList(PARAM_DATA) ?: listOf())
        kycInfoType = arguments?.getParcelable(PARAM_INFO_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKycInfoSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        kycInfoType?.let {
            val title = when (it) {
                KycInfoType.NATIONALITY -> getString(R.string.nationality)
                KycInfoType.COUNTRY_OF_RESIDENCE -> getString(R.string.country_of_residence)
                KycInfoType.PROOF_ADDRESS -> getString(R.string.proof_of_address)
                KycInfoType.SOURCE_FUND -> getString(R.string.source_of_funds)
                KycInfoType.OCCUPATION_CODE -> getString(R.string.occupation_code)
                KycInfoType.INDUSTRY_CODE -> getString(R.string.industry_code)
                KycInfoType.TAX_RESIDENCY_COUNTRY -> getString(R.string.your_country_of_tax_residency)
            }
            binding.title = title
        }



        binding.rvSearch.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        val searchAdapter =
            KycInfoSearchAdapter(appExecutors) {
                when (kycInfoType) {
                    KycInfoType.OCCUPATION_CODE, KycInfoType.INDUSTRY_CODE -> kycInfoType?.let { it1 ->
                        viewModel.save(hash.filterValues { entry ->
                            entry == it
                        }.keys.first(), it1)
                    }
                    else -> kycInfoType?.let { it1 -> viewModel.save(it, it1) }
                }

            }

        binding.rvSearch.adapter = searchAdapter

        val file = when (kycInfoType) {
            KycInfoType.OCCUPATION_CODE -> KYC_OCCUPATION_CODE_FILE
            KycInfoType.INDUSTRY_CODE -> KYC_INDUSTRY_CODE_FILE
            else -> null
        }

        file?.let {
            context?.loadJSONFromAssets(it)?.let {
                val kycOccupationCode = gson.fromJson(it, KycCode::class.java)
                hash.clear()
                hash.putAll(kycOccupationCode.data)
                data.clear()
                data.addAll(kycOccupationCode.data.values)
            }
        }

        updateFilterList(currentSearchString, searchAdapter)


        viewModel.compositeDisposable.add(
            binding.edtSearch.textChanges()
                .skipInitialValue()
                .debounce(
                    250,
                    TimeUnit.MILLISECONDS
                )
                .map {
                    return@map it.trim().toString().toLowerCase()
                }.observeOn(schedulerProvider.ui())
                .subscribe { searchedText ->
                    currentSearchString = searchedText
                    updateFilterList(currentSearchString, searchAdapter)
                })

        binding.imgBack.setOnClickListener {
            activity!!.onBackPressed()
        }

        viewModel.saveKycInfoCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is SaveKycInfoState.Success -> {
                        onSelectionComplete()
                    }
                    is SaveKycInfoState.ShowError -> {
                        showAlert(state.message ?: getString(R.string.something_wrong))
                    }
                }
            }
        })


    }

    private fun onSelectionComplete() {
        activity?.onBackPressed()
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.dispose()
        super.onDestroyView()
    }

    private fun updateFilterList(searchedText: String?, searchAdapter: KycInfoSearchAdapter) {
        if (searchedText.isNullOrEmpty()) {
            searchAdapter.submitFilterList(data)
        } else {
            searchAdapter.submitFilterList(
                getFilterTokenList(
                    currentSearchString,
                    data
                )
            )
        }
    }


    private fun getFilterTokenList(searchedString: String, data: List<String>): List<String> {
        return data.filter { item ->
            item.toLowerCase().contains(searchedString)
        }
    }


    companion object {
        private const val KYC_OCCUPATION_CODE_FILE = "kyc/occupation_code"
        private const val KYC_INDUSTRY_CODE_FILE = "kyc/industry_code"
        private const val PARAM_DATA = "param_data"
        private const val PARAM_INFO_TYPE = "param_type"
        fun newInstance(data: List<String>, kycInfoType: KycInfoType) =
            KycInfoSearchFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(PARAM_DATA, ArrayList(data))
                    putParcelable(PARAM_INFO_TYPE, kycInfoType)

                }
            }
    }
}