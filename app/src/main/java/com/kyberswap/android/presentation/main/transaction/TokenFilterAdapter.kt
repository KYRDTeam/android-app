package com.kyberswap.android.presentation.main.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemTokenFilterBinding
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class TokenFilterAdapter(
    appExecutors: AppExecutors,
    private val onTokenClick: ((Token) -> Unit)?
) : DataBoundListAdapter<Token, ItemTokenFilterBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Token>() {
        override fun areItemsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.tokenSymbol == newItem.tokenSymbol


        override fun areContentsTheSame(oldItem: Token, newItem: Token): Boolean {
            return oldItem.areContentsTheSame(newItem)

    }
) {
    private var fullMode: Boolean = false

    override fun bind(binding: ItemTokenFilterBinding, item: Token) {
        binding.tvToken.setOnClickListener {
            it.isSelected = !it.isSelected
            onTokenClick?.invoke(item)

        binding.setVariable(BR.token, item)
        binding.executePendingBindings()

    }

    fun setFullMode(isFullMode: Boolean) {
        if (fullMode != isFullMode) {
            fullMode = isFullMode
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