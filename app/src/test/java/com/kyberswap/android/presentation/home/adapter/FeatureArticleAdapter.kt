package com.kyberswap.android.presentation.home.adapter

import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemHomeBinding
import com.kyberswap.android.domain.model.Article
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class FeatureArticleAdapter(
    appExecutors: AppExecutors,
    private val onItemClick: ((Article) -> Unit)?
) : DataBoundListAdapter<Article, ItemHomeBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem


        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem

    }
) {
    override fun createBinding(parent: ViewGroup, viewType: Int): ItemHomeBinding {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate(layoutInflater, R.layout.item_home, parent, false)
    }

    override fun bind(binding: ItemHomeBinding, item: Article) {
        binding.setVariable(BR.article, item)
        binding.root.setOnClickListener {
            onItemClick?.invoke(item)

    }
}