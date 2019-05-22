package com.kyberswap.android.presentation.main.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemTokenBinding
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.presentation.base.DataBoundListAdapter

class ContactAdapter(
    appExecutors: AppExecutors,
    private val onItemClick: ((Contact) -> Unit)?

) : DataBoundListAdapter<Contact, ItemTokenBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }
    }
) {


    override fun bind(binding: ItemTokenBinding, item: Contact) {
        binding.setVariable(BR.token, item)
        binding.lnItem.setOnClickListener {
            onItemClick?.invoke(item)
        }
        binding.executePendingBindings()
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemTokenBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_contact,
            parent,
            false
        )
}