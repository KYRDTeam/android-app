package com.kyberswap.android.presentation.main.limitorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemMarketBinding
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.presentation.base.DataBoundListAdapter
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero

class MarketAdapter(
    appExecutors: AppExecutors,
    private val isBuy: Boolean,
    private val onItemClick: ((MarketItem) -> Unit)?,
    private val onFavClick: ((MarketItem) -> Unit)?

) : DataBoundListAdapter<MarketItem, ItemMarketBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<MarketItem>() {
        override fun areItemsTheSame(oldItem: MarketItem, newItem: MarketItem): Boolean {
            return oldItem.displayPair == newItem.displayPair
        }

        override fun areContentsTheSame(oldItem: MarketItem, newItem: MarketItem): Boolean {
            return oldItem.displayPair == newItem.displayPair &&
                    if (isBuy) oldItem.buyPrice == newItem.buyPrice
                    else oldItem.sellPrice == newItem.sellPrice &&
                            oldItem.volume == newItem.volume &&
                            oldItem.change == newItem.change &&
                            oldItem.isFav == newItem.isFav
        }
    }
) {
    override fun createBinding(parent: ViewGroup, viewType: Int): ItemMarketBinding {
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_market,
            parent,
            false
        )
    }

    val isAsc: Boolean
        get() = orderType == OrderType.PAIR_ASC ||
                orderType == OrderType.PRICE_ASC ||
                orderType == OrderType.VOLUME_ASC ||
                orderType == OrderType.CHANGE_24H_ASC

    fun setOrderBy(type: OrderType) {
        this.orderType = type
        submitFilterList(getData(), true)
    }

    fun toggleChange24hOrder(): OrderType {
        return when (orderType) {
            OrderType.CHANGE24H_DESC -> OrderType.CHANGE_24H_ASC
            OrderType.CHANGE_24H_ASC -> OrderType.CHANGE24H_DESC
            else -> OrderType.CHANGE24H_DESC
        }
    }

    fun togglePairOrder(): OrderType {
        return when (orderType) {
            OrderType.PAIR_DESC -> OrderType.PAIR_ASC
            OrderType.PAIR_ASC -> OrderType.PAIR_DESC
            else -> OrderType.PAIR_DESC
        }
    }

    fun togglePriceOrder(): OrderType {
        return when (orderType) {
            OrderType.PRICE_DESC -> OrderType.PRICE_ASC
            OrderType.PRICE_ASC -> OrderType.PRICE_DESC
            else -> OrderType.PRICE_DESC
        }
    }

    fun toggleVolumeOrder(): OrderType {
        return when (orderType) {
            OrderType.VOLUME_DESC -> OrderType.VOLUME_ASC
            OrderType.VOLUME_ASC -> OrderType.VOLUME_DESC
            else -> OrderType.VOLUME_DESC
        }
    }

    fun submitFilterList(marketItems: List<MarketItem>, isForceUpdate: Boolean = false) {
        val orderList = when (orderType) {
            OrderType.PAIR_DESC -> marketItems.sortedByDescending {
                it.pair
            }
            OrderType.PAIR_ASC -> marketItems.sortedBy { it.pair }
            OrderType.PRICE_ASC -> marketItems.sortedBy {
                if (isBuy) it.buyPrice.toBigDecimalOrDefaultZero() else it.sellPrice.toBigDecimalOrDefaultZero()
            }
            OrderType.PRICE_DESC -> marketItems.sortedByDescending {
                if (isBuy) it.buyPrice.toBigDecimalOrDefaultZero() else it.sellPrice.toBigDecimalOrDefaultZero()
            }
            OrderType.VOLUME_ASC -> marketItems.sortedBy {
                it.volume.toBigDecimalOrDefaultZero()
            }
            OrderType.VOLUME_DESC -> marketItems.sortedByDescending {
                it.volume.toBigDecimalOrDefaultZero()
            }
            OrderType.CHANGE_24H_ASC -> marketItems.sortedBy {
                it.change.toBigDecimalOrDefaultZero()
            }
            OrderType.CHANGE24H_DESC -> marketItems.sortedByDescending {
                it.change.toBigDecimalOrDefaultZero()
            }
        }
        if (isForceUpdate) {
            submitList(null)
        } else {
            submitList(listOf())
        }
        submitList(orderList)
    }

    override fun bind(binding: ItemMarketBinding, item: MarketItem) {
        binding.clParent.setOnClickListener {
            onItemClick?.invoke(item)
        }

        binding.imgFav.setOnClickListener {
            it.isSelected = !item.isFav
            item.isFav = !item.isFav
            onFavClick?.invoke(item)
        }
        binding.isBuy = isBuy
        binding.setVariable(BR.item, item)
        binding.executePendingBindings()
    }

    private var orderType = OrderType.PAIR_DESC
}

enum class OrderType {
    PAIR_ASC, PAIR_DESC, PRICE_ASC, PRICE_DESC, VOLUME_ASC, VOLUME_DESC, CHANGE_24H_ASC, CHANGE24H_DESC
}