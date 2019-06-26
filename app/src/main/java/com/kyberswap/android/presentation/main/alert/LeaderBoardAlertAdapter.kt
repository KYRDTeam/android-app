package com.kyberswap.android.presentation.main.alert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemLeaderBoardBinding
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class LeaderBoardAlertAdapter(
    appExecutors: AppExecutors

) : DataBoundListAdapter<Alert, ItemLeaderBoardBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem
        }


        override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem
        }
    }
) {


    fun submitAlerts(tokens: List<Alert>) {
        submitList(listOf())
        submitList(tokens)
    }


    override fun bind(binding: ItemLeaderBoardBinding, item: Alert) {
        binding.setVariable(BR.alert, item)
        binding.executePendingBindings()
    }


    override fun createBinding(parent: ViewGroup, viewType: Int): ItemLeaderBoardBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_leader_board,
            parent,
            false
        )
}