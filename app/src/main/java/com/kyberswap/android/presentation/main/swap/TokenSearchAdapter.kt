package com.kyberswap.android.presentation.main.swap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemTokenSearchBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class TokenSearchAdapter(
    appExecutors: AppExecutors,
    private val onTokenClick: ((Token) -> Unit)?
) : DataBoundListAdapter<Token, ItemTokenSearchBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Token>() {
        override fun areItemsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.tokenSymbol == newItem.tokenSymbol


        override fun areContentsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.areContentsTheSame(newItem)

    }
) {

    fun submitFilterList(tokens: List<Token>) {
        if (itemCount > 0) {
            submitList(listOf())
            submitList(tokens)
 else {
            submitList(tokens)


    }

    override fun bind(binding: ItemTokenSearchBinding, item: Token) {
        binding.setVariable(BR.token, item)
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            onTokenClick?.invoke(item)

    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemTokenSearchBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_token_search,
            parent,
            false
        )
}