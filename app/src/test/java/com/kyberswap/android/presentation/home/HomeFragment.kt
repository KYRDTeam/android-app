package com.kyberswap.android.presentation.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.databinding.FragmentHomeBinding
import com.kyberswap.android.domain.model.Header
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.home.adapter.HomePagerAdapter
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class HomeFragment : BaseFragment() {
    private var currentIndex: Int = 0
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeAdapter: HomePagerAdapter
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.headers.observe(this, Observer<List<Header>> { it ->
            it?.let {
                setupViewPager(it)
            }
        })
        viewModel.getHeaders()
    }

    private fun setupViewPager(headers: List<Header>) {
        homeAdapter = HomePagerAdapter(this.activity, childFragmentManager, headers)
        binding.vpHome.adapter = homeAdapter
        binding.tabLayout.setupWithViewPager(binding.vpHome)
    }

    override fun onDestroy() {
        viewModel.destroy()
        super.onDestroy()
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
