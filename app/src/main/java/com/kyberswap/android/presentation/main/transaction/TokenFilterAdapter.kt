package com.kyberswap.android.presentation.main.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemTokenFilterBinding
import com.kyberswap.android.domain.model.FilterItem
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class TokenFilterAdapter(
    appExecutors: AppExecutors
) : DataBoundListAdapter<FilterItem, ItemTokenFilterBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<FilterItem>() {
        override fun areItemsTheSame(oldItem: FilterItem, newItem: FilterItem): Boolean {
            return oldItem.name == newItem.name


        override fun areContentsTheSame(oldItem: FilterItem, newItem: FilterItem): Boolean {
            return oldItem.name == newItem.name && oldItem.isSelected == newItem.isSelected

    }
) {
    private var fullMode: Boolean = false

    override fun bind(binding: ItemTokenFilterBinding, item: FilterItem) {
        binding.tvToken.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyDataSetChanged()

        binding.setVariable(BR.item, item)
        binding.executePendingBindings()

    }

    fun setFullMode(isFullMode: Boolean) {
        if (fullMode != isFullMode) {
            fullMode = isFullMode
            notifyDataSetChanged()

    }

    fun submitFilterList(items: List<FilterItem>) {
        val sortedList = items.sortedByDescending {
            it.isSelected

        submitList(sortedList)
    }

    fun resetFilter(isSelecteAll: Boolean) {

        submitFilterList(getData().map {
            it.isSelected = isSelecteAll
            it
)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return when {
            fullMode -> super.getItemCount()
            else -> {
                if (super.getItemCount() > 12) {
                    12
         else {
                    super.getItemCount()
        
    

    }


    override fun createBinding(parent: ViewGroup, viewType: Int): ItemTokenFilterBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_token_filter,
            parent,
            false
        )
}