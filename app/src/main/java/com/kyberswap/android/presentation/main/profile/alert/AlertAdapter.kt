package com.kyberswap.android.presentation.main.profile.alert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemAlertBinding
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.presentation.base.DataBoundListAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class AlertAdapter(
    appExecutors: AppExecutors,
    private val onItemClick: ((Alert) -> Unit)?

) : DataBoundListAdapter<Alert, ItemAlertBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem



        override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem

    }
) {

    fun submitAlerts(tokens: List<Alert>) {
        submitList(listOf())
        submitList(tokens)
    }


    override fun bind(binding: ItemAlertBinding, item: Alert) {
        binding.setVariable(BR.alert, item)
        binding.root.setOnClickListener {
            onItemClick?.invoke(item)



        binding.executePendingBindings()

    }


    override fun onBindViewHolder(holder: DataBoundViewHolder<ItemAlertBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val root = holder.binding.root
        val background = ContextCompat.getColor(
            root.context,
            if (position % 2 == 0) R.color.token_item_even_bg else R.color.token_item_odd_bg
        )
        root.setBackgroundColor(background)
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemAlertBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_alert,
            parent,
            false
        )
}