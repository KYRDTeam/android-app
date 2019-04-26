package com.kyberswap.android.presentation.wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemWordBinding
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class WordAdapter(
    appExecutors: AppExecutors
) : DataBoundListAdapter<Word, ItemWordBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun bind(binding: ItemWordBinding, item: Word) {
        binding.setVariable(BR.item, item)
        binding.executePendingBindings()
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemWordBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_word,
            parent,
            false
        )
}