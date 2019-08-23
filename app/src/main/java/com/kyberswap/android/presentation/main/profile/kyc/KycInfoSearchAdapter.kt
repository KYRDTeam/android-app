package com.kyberswap.android.presentation.main.profile.kyc

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemKycInfoBinding
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class KycInfoSearchAdapter(
    appExecutors: AppExecutors,
    private val onTokenClick: ((String) -> Unit)?
) : DataBoundListAdapter<String, ItemKycInfoBinding>(
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

    fun submitFilterList(data: List<String>) {
        submitList(listOf())
        submitList(data)

    }

    override fun bind(binding: ItemKycInfoBinding, item: String) {
        binding.root.setOnClickListener {
            onTokenClick?.invoke(item)
        }
        binding.setVariable(BR.item, item)
        binding.executePendingBindings()
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemKycInfoBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_kyc_info,
            parent,
            false
        )
}