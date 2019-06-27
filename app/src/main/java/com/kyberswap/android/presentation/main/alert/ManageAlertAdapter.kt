package com.kyberswap.android.presentation.main.alert

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
import com.kyberswap.android.databinding.ItemManageAlertBinding
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.presentation.base.DataBoundListSwipeAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class ManageAlertAdapter(
    appExecutors: AppExecutors,
    private val handler: Handler,
    private val onItemClick: ((Alert) -> Unit)?,
    private val onEditClick: ((Alert) -> Unit)?,
    private val onDeleteClick: ((Alert) -> Unit)?

) : DataBoundListSwipeAdapter<Alert, ItemManageAlertBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem



        override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem

    }
) {
    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemManageAlertBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        mItemManger.bindView(holder.itemView, position)
        super.onBindViewHolder(holder, position, payloads)

    }

    fun submitAlerts(tokens: List<Alert>) {
        submitList(listOf())
        submitList(tokens)
    }


    override fun bind(binding: ItemManageAlertBinding, item: Alert) {
        binding.setVariable(BR.alert, item)
        binding.csLayout.setOnClickListener {
            onItemClick?.invoke(item)


        binding.executePendingBindings()

        binding.swipe.addSwipeListener(object : SimpleSwipeListener() {
            override fun onStartOpen(layout: SwipeLayout?) {
                mItemManger.closeAllExcept(layout)
    
)

        binding.btnEdit.setOnClickListener {

            binding.swipe.close(true)
            handler.postDelayed({
                onEditClick?.invoke(item)
    , 250)


        binding.btnDelete.setOnClickListener {
            binding.swipe.close(true)
            handler.postDelayed({
                onDeleteClick?.invoke(item)
    , 250)


    }


    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemManageAlertBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val background =
            if (position % 2 == 0) R.drawable.item_alert_even_background else R.drawable.item_alert_odd_background
        holder.binding.root.setBackgroundResource(background)

    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemManageAlertBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_manage_alert,
            parent,
            false
        )
}