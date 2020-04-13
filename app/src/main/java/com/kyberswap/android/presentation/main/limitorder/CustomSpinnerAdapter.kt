package com.kyberswap.android.presentation.main.limitorder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemQuoteBinding

class CustomSpinnerAdapter(
    context: Context,
    resource: Int,
    quotes: List<String>
) : ArrayAdapter<String>(context, resource, quotes) {

    private val activeBackground by lazy {
        getContext().getDrawable(R.drawable.rounded_corner_spinner_background_active)
    }

    var hasSelected: Boolean = false

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view = customView(position, convertView, parent)

        if (hasSelected) {
            view.background = activeBackground
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return customView(position, convertView, parent)
    }

    fun setSelected(isSelected: Boolean) {
        if (isSelected != hasSelected) {
            hasSelected = isSelected
            notifyDataSetChanged()
        }
    }

    private fun customView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var binding: ItemQuoteBinding? = null
        val view = if (convertView == null) {
            binding = DataBindingUtil.inflate<ItemQuoteBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_quote,
                parent,
                false
            )
            binding.root
        } else {
            val tag = convertView.tag
            if (tag is ItemQuoteBinding) {
                binding = tag
            }
            convertView
        }

        if (binding != null && binding.symbol != getItem(position)) {
            binding.symbol = getItem(position)
            binding.executePendingBindings()

            view.tag = binding
        }

        return view
    }
}