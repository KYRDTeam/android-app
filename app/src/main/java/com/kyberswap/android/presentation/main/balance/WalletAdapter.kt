package com.kyberswap.android.presentation.main.balance

import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemDrawerMenuBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.DataBoundListSwipeAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class WalletAdapter(
    appExecutors: AppExecutors,
    private val handler: Handler,
    private val onItemClick: ((Wallet) -> Unit)?,
    private val onItemCopyClick: ((Wallet) -> Unit)?,
    private val onStartOpen: (() -> Unit)?,
    private val onStartClose: (() -> Unit)?
) : DataBoundListSwipeAdapter<Wallet, ItemDrawerMenuBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Wallet>() {
        override fun areItemsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem.walletId == newItem.walletId
        }

        override fun areContentsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem.name == newItem.name && oldItem.address == newItem.address
                && oldItem.isSelected == newItem.isSelected
        }
    }
) {

    override fun bind(binding: ItemDrawerMenuBinding, item: Wallet) {
        binding.setVariable(BR.wallet, item)
        binding.executePendingBindings()
        binding.csItem.setOnClickListener {
            onItemClick?.invoke(item.copy(isSelected = true))
        }

        binding.swManageWallet.addSwipeListener(object : SimpleSwipeListener() {
            override fun onStartOpen(layout: SwipeLayout?) {
                onStartOpen?.invoke()
                mItemManger.closeAllExcept(layout)
            }

            override fun onStartClose(layout: SwipeLayout?) {
                onStartClose?.invoke()
                super.onStartClose(layout)
            }
        })

        binding.btnCopy.setOnClickListener {
            binding.swManageWallet.close(true)
            handler.postDelayed({
                onItemCopyClick?.invoke(item)
            }, 250)

        }
    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemDrawerMenuBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        mItemManger.bindView(holder.itemView, position)
        super.onBindViewHolder(holder, position, payloads)
    }


    override fun createBinding(parent: ViewGroup, viewType: Int): ItemDrawerMenuBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_drawer_menu,
            parent,
            false
        )

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.sw_manage_wallet
    }
}