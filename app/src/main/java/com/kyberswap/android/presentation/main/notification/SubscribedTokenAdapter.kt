package com.kyberswap.android.presentation.main.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemSubscribedTokenBinding
import com.kyberswap.android.domain.model.SubscriptionNotificationData
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class SubscribedTokenAdapter(
    appExecutors: AppExecutors
) : DataBoundListAdapter<SubscriptionNotificationData, ItemSubscribedTokenBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<SubscriptionNotificationData>() {
        override fun areItemsTheSame(
            oldItem: SubscriptionNotificationData,
            newItem: SubscriptionNotificationData
        ): Boolean {
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(
            oldItem: SubscriptionNotificationData,
            newItem: SubscriptionNotificationData
        ): Boolean {
            return oldItem.symbol == newItem.symbol && oldItem.subscribed == newItem.subscribed
        }
    }
) {

    override fun bind(binding: ItemSubscribedTokenBinding, item: SubscriptionNotificationData) {
        binding.tvToken.setOnClickListener {
            item.subscribed = !item.subscribed
            notifyDataSetChanged()
        }
        binding.setVariable(BR.item, item)
        binding.executePendingBindings()
    }

    fun submitFilterList(items: List<SubscriptionNotificationData>) {
        val sortedList = items.sortedByDescending {
            it.subscribed
        }
        submitList(sortedList)
    }

    fun reset(isSelecteAll: Boolean) {

        submitFilterList(getData().map {
            it.subscribed = isSelecteAll
            it
        })
        notifyDataSetChanged()
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemSubscribedTokenBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_subscribed_token,
            parent,
            false
        )
}