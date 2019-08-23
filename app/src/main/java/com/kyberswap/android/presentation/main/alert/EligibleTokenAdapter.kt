package com.kyberswap.android.presentation.main.alert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemEligibleTokenBinding
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class EligibleTokenAdapter(
    appExecutors: AppExecutors,
    private val onTokenClick: ((String) -> Unit)?
) : DataBoundListAdapter<String, ItemEligibleTokenBinding>(
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
    override fun bind(binding: ItemEligibleTokenBinding, item: String) {
        binding.setVariable(BR.name, item)
        binding.executePendingBindings()

    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemEligibleTokenBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_eligible_token,
            parent,
            false
        )
}