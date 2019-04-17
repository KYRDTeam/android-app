package com.kyberswap.android.presentation.base

import android.support.v7.recyclerview.extensions.AsyncDifferConfig
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.AdapterListUpdateCallback
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView

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
