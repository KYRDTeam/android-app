package com.kyberswap.android.presentation.main.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemNotificationBinding
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class NotificationAdapter(
    appExecutors: AppExecutors,
    private val onNotificationClick: ((Notification) -> Unit)?

) : DataBoundListAdapter<Notification, ItemNotificationBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Notification>() {

        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemNotificationBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_notification,
            parent,
            false
        )

    fun updateReadItem(notification: Notification?) {
        if (notification != null) {
            val currentList = getData().toMutableList()
            val index = currentList.indexOfFirst { it.id == notification.id }
            currentList.set(index, notification)
            submitList(currentList)
        } else {
            readAll()
        }
    }

    fun readAll() {
        submitList(getData().map { it.copy(read = true) })
    }

    override fun bind(binding: ItemNotificationBinding, item: Notification) {
        binding.notification = item
        binding.executePendingBindings()
        val indexOfItem = getData().indexOf(item)
        if (indexOfItem >= 0) {
            val color = if (!item.read) {
                R.color.notification_unread
            } else {
                R.color.notification_even
            }
            binding.root.setBackgroundColor(ContextCompat.getColor(binding.root.context, color))
        }
        binding.root.setOnClickListener {
            onNotificationClick?.invoke(item)
        }
    }
}