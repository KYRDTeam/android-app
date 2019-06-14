package com.kyberswap.android.presentation.main.limitorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemOrderBinding
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.presentation.base.DataBoundListSwipeAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class OrderAdapter(
    appExecutors: AppExecutors,
    private val onCancelClick: ((Order) -> Unit)?

) : DataBoundListSwipeAdapter<Order, ItemOrderBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem

    }
) {

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemOrderBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        mItemManger.bindView(holder.itemView, position)
        super.onBindViewHolder(holder, position, payloads)

    }

    override fun bind(binding: ItemOrderBinding, item: Order) {
        binding.swipe.addSwipeListener(object : SimpleSwipeListener() {
            override fun onStartOpen(layout: SwipeLayout?) {
                mItemManger.closeAllExcept(layout)
    
)
        binding.tvCancel.setOnClickListener {
            onCancelClick?.invoke(item)
            mItemManger.closeAllItems()

        binding.order = item
        binding.executePendingBindings()

    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemOrderBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_order,
            parent,
            false
        )
}