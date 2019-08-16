package com.kyberswap.android.presentation.main.limitorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemHeaderBinding
import com.kyberswap.android.databinding.ItemOrderBinding
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.presentation.base.DataBoundListSwipeAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class OrderAdapter(
    appExecutors: AppExecutors,
    private val onCancelClick: ((Order) -> Unit)?,
    private val onExtraClick: ((Order) -> Unit)? = {},
    private val onMinedOrderClick: ((Order) -> Unit)? = {},
    private val onInvalidatedClick: ((Order) -> Unit)? = {}

) : DataBoundListSwipeAdapter<OrderItem, ViewDataBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<OrderItem>() {
        override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return when {
                oldItem is OrderItem.Header && newItem is OrderItem.Header && oldItem.date == newItem.date -> true
                oldItem is OrderItem.ItemEven && newItem is OrderItem.ItemEven && oldItem.order.id == newItem.order.id -> true
                oldItem is OrderItem.ItemOdd && newItem is OrderItem.ItemOdd && oldItem.order.id == newItem.order.id -> true
                oldItem is OrderItem.ItemOdd && newItem is OrderItem.ItemEven && oldItem.order.id == newItem.order.id -> true
                oldItem is OrderItem.ItemEven && newItem is OrderItem.ItemOdd && oldItem.order.id == newItem.order.id -> true
                else -> false
            }

        }

        override fun areContentsTheSame(
            oldItem: OrderItem, newItem: OrderItem
        ): Boolean {
            return when {
                oldItem is OrderItem.Header && newItem is OrderItem.Header && oldItem.date == newItem.date -> true
                oldItem is OrderItem.ItemOdd && newItem is OrderItem.ItemOdd && oldItem.order.sameDisplay(
                    newItem.order
                ) -> true
                oldItem is OrderItem.ItemEven && newItem is OrderItem.ItemEven && oldItem.order.sameDisplay(
                    newItem.order
                ) -> true
                oldItem is OrderItem.ItemOdd && newItem is OrderItem.ItemEven && oldItem.order.sameDisplay(
                    newItem.order
                )
                -> true
                oldItem is OrderItem.ItemEven && newItem is OrderItem.ItemOdd && oldItem.order.sameDisplay(
                    newItem.order
                )
                -> true

                else -> false
            }
        }
    }
) {

    private var isWarning: Boolean = false

    val orderList: List<Order>
        get() = getData().map {
            when (it) {
                is OrderItem.ItemOdd -> it.order
                is OrderItem.ItemEven -> it.order
                is OrderItem.Header -> Order()
            }
        }.filter {
            it.id > 0
        }


    fun submitOrdersList(items: List<OrderItem>) {
        submitList(listOf())
        submitList(
            items
        )

    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ViewDataBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {

        super.onBindViewHolder(holder, position)
        when (getData()[position]) {
            is OrderItem.Header -> {
            }
            is OrderItem.ItemEven -> {
                mItemManger.bindView(holder.itemView, position)
            }
            is OrderItem.ItemOdd -> {
                mItemManger.bindView(holder.itemView, position)
            }
        }
    }

    private fun binding(binding: ItemOrderBinding, item: Order) {
        binding.swipe.isSwipeEnabled = item.isPending && !isWarning
        binding.swipe.addSwipeListener(object : SimpleSwipeListener() {
            override fun onStartOpen(layout: SwipeLayout?) {
                mItemManger.closeAllExcept(layout)
            }
        })
        binding.tvCancel.setOnClickListener {
            onCancelClick?.invoke(item)
            mItemManger.closeAllItems()
        }
        binding.order = item
        binding.executePendingBindings()
        binding.clHolder.setOnClickListener {
            if (item.isMined) {
                onMinedOrderClick?.invoke(item)
            }
        }

        binding.imgInvalidated.setOnClickListener {
            if (item.isInvalidated && item.msg.isNotEmpty()) {
                onInvalidatedClick?.invoke(item)
            }
        }


    }

    fun setWarning(isWarning: Boolean) {
        this.isWarning = isWarning
        notifyDataSetChanged()
    }

    override fun bind(binding: ViewDataBinding, item: OrderItem) {
        when (item) {
            is OrderItem.Header -> {
                binding as ItemHeaderBinding
                binding.tvDate.text = item.date

            }

            is OrderItem.ItemEven -> {
                binding as ItemOrderBinding
                binding(binding, item.order)
                val background = ContextCompat.getColor(
                    binding.root.context,
                    R.color.transaction_item_even_color
                )

                binding.clHolder.setBackgroundColor(background)

                binding.tvExtra.setOnClickListener {
                    onExtraClick?.invoke(item.order)
                }

            }

            is OrderItem.ItemOdd -> {
                binding as ItemOrderBinding
                binding(binding, item.order)
                val background = ContextCompat.getColor(
                    binding.root.context,
                    R.color.transaction_item_odd_color
                )

                binding.clHolder.setBackgroundColor(background)
                binding.tvExtra.setOnClickListener {
                    onExtraClick?.invoke(item.order)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getData()[position]) {
            is OrderItem.Header -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }


    override fun createBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
        val layout =
            if (viewType == TYPE_HEADER) R.layout.item_header else R.layout.item_order
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layout,
            parent,
            false
        )
    }


    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_ITEM = 2
    }
}

