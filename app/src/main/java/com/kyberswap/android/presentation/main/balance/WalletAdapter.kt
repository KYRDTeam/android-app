package com.kyberswap.android.presentation.main.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemDrawerMenuBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class WalletAdapter(
    appExecutors: AppExecutors,
    private val onItemClick: ((Wallet) -> Unit)?
) : DataBoundListAdapter<Wallet, ItemDrawerMenuBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Wallet>() {
        override fun areItemsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem.walletId == newItem.walletId


        override fun areContentsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem.name == newItem.name && oldItem.address == newItem.address
                && oldItem.isSelected == newItem.isSelected

    }
) {


    override fun bind(binding: ItemDrawerMenuBinding, item: Wallet) {
        binding.setVariable(BR.wallet, item)
        binding.executePendingBindings()
        binding.root.setOnClickListener {
            onItemClick?.invoke(item.copy(isSelected = true))


    }


    override fun createBinding(parent: ViewGroup, viewType: Int): ItemDrawerMenuBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_drawer_menu,
            parent,
            false
        )
}