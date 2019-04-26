package com.kyberswap.android.presentation.base

import androidx.recyclerview.widget.*

@Suppress("LeakingThis")
abstract class ListDifferAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH> {
    private val helper: AsyncListDiffer<T>

    protected constructor(diffCallback: DiffUtil.ItemCallback<T>) {
        helper = AsyncListDiffer<T>(
            AdapterListUpdateCallback(this),
            AsyncDifferConfig.Builder(diffCallback).build()
        )
    }

    protected constructor(config: AsyncDifferConfig<T>) {
        helper = AsyncListDiffer<T>(AdapterListUpdateCallback(this), config)
    }

    fun submitList(list: List<T>) {
        helper.submitList(list)
    }

    open fun getItem(position: Int): T {
        return helper.currentList[position]
    }

    override fun getItemCount() = helper.currentList.size

    fun getData(): List<T> = helper.currentList
}
