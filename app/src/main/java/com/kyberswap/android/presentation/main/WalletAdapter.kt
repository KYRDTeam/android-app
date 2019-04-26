package com.kyberswap.android.presentation.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemDrawerMenuBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.DataBoundListAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class WalletAdapter(
    appExecutors: AppExecutors
) : DataBoundListAdapter<Wallet, ItemDrawerMenuBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Wallet>() {
        override fun areItemsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem == newItem


        override fun areContentsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem == newItem

    }
) {

    private var currentSelected: View? = null

    override fun bind(binding: ItemDrawerMenuBinding, item: Wallet) {
        binding.setVariable(BR.wallet, item)
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            currentSelected?.isSelected = false
            it.isSelected = true
            currentSelected = it

    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemDrawerMenuBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        if (currentSelected == null) {
            holder.binding.root.isSelected = true
            currentSelected = holder.binding.root

    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemDrawerMenuBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_drawer_menu,
            parent,
            false
        )
}