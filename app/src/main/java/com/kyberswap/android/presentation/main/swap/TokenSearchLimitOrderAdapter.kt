package com.kyberswap.android.presentation.main.swap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemTokenSearchLimitOrderBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class TokenSearchLimitOrderAdapter(
    appExecutors: AppExecutors,
    private val onTokenClick: ((Token) -> Unit)?
) : DataBoundListAdapter<Token, ItemTokenSearchLimitOrderBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Token>() {
        override fun areItemsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.tokenSymbol == newItem.tokenSymbol
        }

        override fun areContentsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }
    }
) {

    fun submitFilterList(tokens: List<Token>) {
        submitList(listOf())
        submitList(tokens)
    }

    override fun bind(binding: ItemTokenSearchLimitOrderBinding, item: Token) {
        binding.root.setOnClickListener {
            onTokenClick?.invoke(item)
        }
        binding.setVariable(BR.token, item)
        binding.executePendingBindings()
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemTokenSearchLimitOrderBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_token_search_limit_order,
            parent,
            false
        )
}