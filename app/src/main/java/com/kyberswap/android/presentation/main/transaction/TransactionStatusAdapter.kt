package com.kyberswap.android.presentation.main.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.HeaderTransactionBinding
import com.kyberswap.android.databinding.ItemTransactionBinding
import com.kyberswap.android.presentation.base.DataBoundListAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class TransactionStatusAdapter(
    appExecutors: AppExecutors,
    private val onTransactionClick: ((TransactionItem) -> Unit)?

) : DataBoundListAdapter<TransactionItem, ViewDataBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<TransactionItem>() {
        override fun areItemsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
            return false
        }

        override fun areContentsTheSame(
            oldItem: TransactionItem, newItem: TransactionItem
        ): Boolean {
            return false
        }
    }
) {


    override fun bind(binding: ViewDataBinding, item: TransactionItem) {
        when (item) {
            is TransactionItem.Header -> {
                binding as HeaderTransactionBinding
                binding.tvDate.text = item.date

            }

            is TransactionItem.ItemEven -> {
                binding as ItemTransactionBinding
                binding.tvTransactionDetail.text = item.transaction.displayTransaction
                binding.tvRate.text = item.transaction.displayRate
                binding.tvTransactionType.text = item.transaction.displayTransactionType
            }

            is TransactionItem.ItemOdd -> {
                binding as ItemTransactionBinding
                binding.tvTransactionDetail.text = item.transaction.displayTransaction
                binding.tvRate.text = item.transaction.displayRate
                binding.tvTransactionType.text = item.transaction.displayTransactionType
            }
        }

    }

    override fun getItemViewType(position: Int): Int {

        return when (getData()[position]) {
            is TransactionItem.Header -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }


    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ViewDataBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        val color = when (getData()[position]) {
            is TransactionItem.Header -> R.color.transaction_header_color
            is TransactionItem.ItemEven -> R.color.transaction_item_even_color
            is TransactionItem.ItemOdd -> R.color.transaction_item_odd_color
        }

        val root = holder.binding.root
        val background = ContextCompat.getColor(
            root.context,
            color
        )
        root.setBackgroundColor(background)
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
        val layout =
            if (viewType == TYPE_HEADER) R.layout.header_transaction else R.layout.item_transaction
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layout,
            parent,
            false
        )
    }


    companion object {
        private val TYPE_HEADER = 1
        private val TYPE_ITEM = 2
    }
}