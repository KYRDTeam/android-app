package com.kyberswap.android.presentation.main.limitorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemFilterBinding
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class FilterItemAdapter(
    appExecutors: AppExecutors,
    private val onCancelClick: ((String) -> Unit)?

) : DataBoundListAdapter<String, ItemFilterBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun bind(binding: ItemFilterBinding, item: String) {
        binding.name = item
        binding.executePendingBindings()
    }


    override fun createBinding(parent: ViewGroup, viewType: Int): ItemFilterBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_filter,
            parent,
            false
        )
}