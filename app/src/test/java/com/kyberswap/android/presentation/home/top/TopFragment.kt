package com.kyberswap.android.presentation.home.top

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentTopBinding
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.listener.EndlessRecyclerOnScrollListener
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class TopFragment : BaseFragment() {

    private lateinit var binding: FragmentTopBinding
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var appExecutors: AppExecutors
    private var adapter: TopRecyclerViewAdapter? = null

    val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(TopViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.rvArticle.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        if (adapter == null) {
            adapter = TopRecyclerViewAdapter(appExecutors) {
    

        binding.rvArticle.adapter = adapter
        val scrollListener = object : EndlessRecyclerOnScrollListener(
            binding.rvArticle.layoutManager as LinearLayoutManager
        ) {
            override fun onLoadMore(currentPage: Int) {
                viewModel.loadArticles(currentPage)
    

        binding.rvArticle.addOnScrollListener(scrollListener)
        viewModel.onRefresh.observe(this, Observer {
            it?.let { scrollListener.reset() }
)
        viewModel.loadArticles()
        viewModel.getReviewAndLikeInfo()

        viewModel.articles.observe(this, Observer { it ->
            it?.let {
                adapter?.submitList(it)
    
)
    }

    override fun onDestroy() {
        viewModel.destroy()
        super.onDestroy()
    }

    companion object {
        fun newInstance() = TopFragment()
    }
}
