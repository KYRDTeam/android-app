package com.kyberswap.android.presentation.home.top

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentFeatureBinding
import com.kyberswap.android.domain.model.Header
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.home.adapter.FeatureArticleAdapter
import com.kyberswap.android.presentation.listener.EndlessRecyclerOnScrollListener
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject

class FeatureFragment : BaseFragment() {

    private lateinit var binding: FragmentFeatureBinding
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var appExecutors: AppExecutors
    private var adapter: FeatureArticleAdapter? = null
    private lateinit var header: Header
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var dialogHelper: DialogHelper

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(FeatureViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        header = arguments?.getParcelable(ARGS_HEADER) ?: return
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeatureBinding.inflate(inflater, container, false)
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
            adapter = FeatureArticleAdapter(
                appExecutors
            ) {
            }
        }
        binding.rvArticle.adapter = adapter

        val scrollListener = object : EndlessRecyclerOnScrollListener(
            binding.rvArticle.layoutManager as LinearLayoutManager
        ) {
            override fun onLoadMore(currentPage: Int) {
                viewModel.loadArticles(header.featureId, currentPage)
            }
        }
        binding.rvArticle.addOnScrollListener(scrollListener)

        viewModel.onRefresh.observe(this, Observer {
            it?.let { scrollListener.reset() }
        })
        viewModel.loadArticles(header.featureId)
        viewModel.articles.observe(this, Observer {
            it?.let {
                adapter?.submitList(it)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_QUOTE_ARTICLE && resultCode == Activity.RESULT_OK) {
        }
    }

    companion object {
        private const val ARGS_HEADER = "args_header"
        private const val RQ_QUOTE_ARTICLE = 0x1

        fun newInstance(header: Header): FeatureFragment {
            val fragment = FeatureFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARGS_HEADER, header)
            fragment.arguments = bundle
            return fragment
        }
    }
}
