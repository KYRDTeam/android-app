package com.kyberswap.android.presentation.main.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemTokenBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.presentation.base.DataBoundListSwipeAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder

class TokenAdapter(
    appExecutors: AppExecutors
) : DataBoundListSwipeAdapter<Token, ItemTokenBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Token>() {
        override fun areItemsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.tokenSymbol == newItem.tokenSymbol
        }

        override fun areContentsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }
    }
) {
    private var isEth = false


    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    fun showEth(boolean: Boolean) {
        isEth = boolean
        notifyDataSetChanged()
    }

    fun submitFilterList(tokens: List<Token>) {
        submitList(listOf())
        submitList(tokens)
    }


    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemTokenBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        mItemManger.bindView(holder.itemView, position)

    }

    override fun bind(binding: ItemTokenBinding, item: Token) {
        binding.setVariable(BR.token, item)
        binding.executePendingBindings()
        binding.btnBuy.setOnClickListener {
            Toast.makeText(binding.root.context, "Buy", Toast.LENGTH_SHORT).show()
            binding.swipe.close(true)

        }
        binding.btnSell.setOnClickListener {
            Toast.makeText(binding.root.context, "Sell", Toast.LENGTH_SHORT).show()
            binding.swipe.close(true)
        }
        binding.btnSend.setOnClickListener {
            Toast.makeText(binding.root.context, "Send", Toast.LENGTH_SHORT).show()
            binding.swipe.close(true)
        }

        binding.swipe.addSwipeListener(object : SimpleSwipeListener() {
            override fun onStartOpen(layout: SwipeLayout?) {
                mItemManger.closeAllExcept(layout)
            }
        })


        val drawable = when (item.change24hStatus()) {
            Token.UP -> R.drawable.ic_arrow_up
            Token.DOWN -> R.drawable.ic_arrow_down
            else -> null
        }

        val color = when (item.change24hStatus()) {
            Token.UP -> R.color.token_change24h_up
            Token.DOWN -> R.color.token_change24h_down
            else -> R.color.token_change24h_same
        }

        Glide.with(binding.imgState)
            .load(drawable)
            .into(binding.imgState)

        binding.tvChange24h.setTextColor(ContextCompat.getColor(binding.root.context, color))
        binding.setVariable(BR.showEth, isEth)
        binding.executePendingBindings()

    }


    override fun onBindViewHolder(holder: DataBoundViewHolder<ItemTokenBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val root = holder.binding.root
        val background = ContextCompat.getColor(
            root.context,
            if (position % 2 == 0) R.color.token_item_even_bg else R.color.token_item_odd_bg
        )
        root.setBackgroundColor(background)
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemTokenBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_token,
            parent,
            false
        )
}