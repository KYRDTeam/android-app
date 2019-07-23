package com.kyberswap.android.presentation.main.setting.wallet

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemManageWalletBinding
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.DataBoundListSwipeAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class ManageWalletAdapter(
    appExecutors: AppExecutors,
    private val handler: Handler,
    private val onItemClick: ((Wallet) -> Unit)?,
    private val onSwitchClick: ((Wallet) -> Unit)?,
    private val onEditClick: ((Wallet) -> Unit)?,
    private val onDeleteClick: ((Wallet) -> Unit)?
) : DataBoundListSwipeAdapter<Wallet, ItemManageWalletBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Wallet>() {
        override fun areItemsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.sw_manage_wallet
    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemManageWalletBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        mItemManger.bindView(holder.itemView, position)
        super.onBindViewHolder(holder, position, payloads)

    }


    override fun bind(binding: ItemManageWalletBinding, item: Wallet) {
        binding.setVariable(BR.wallet, item)
        binding.executePendingBindings()

        binding.btnSwitch.visibility = if (getData().size > 1) View.VISIBLE else View.GONE

        binding.btnSwitch.visibility =
            if (getData().size > 1 && item.isSelected) View.GONE else View.VISIBLE

        binding.lnItem.setOnClickListener {
            onItemClick?.invoke(item)
        }
        binding.btnSwitch.setOnClickListener {

            binding.swManageWallet.close(true)
            handler.postDelayed({
                onSwitchClick?.invoke(item)
            }, 250)

        }
        binding.btnEdit.setOnClickListener {
            binding.swManageWallet.close(true)
            handler.postDelayed({
                onEditClick?.invoke(item)
            }, 250)
        }
        binding.btnDelete.setOnClickListener {
            binding.swManageWallet.close(true)
            handler.postDelayed(
                {
                    onDeleteClick?.invoke(item)
                }, 250
            )

        }

        binding.swManageWallet.addSwipeListener(object : SimpleSwipeListener() {
            override fun onStartOpen(layout: SwipeLayout?) {
                mItemManger.closeAllExcept(layout)
            }
        })
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemManageWalletBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_manage_wallet,
            parent,
            false
        )
}