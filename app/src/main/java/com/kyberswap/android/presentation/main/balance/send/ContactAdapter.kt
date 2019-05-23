package com.kyberswap.android.presentation.main.balance.send

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemContactBinding
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.presentation.base.DataBoundListAdapter


class ContactAdapter(
    appExecutors: AppExecutors,
    private val onItemClick: ((Contact) -> Unit)?

) : DataBoundListAdapter<Contact, ItemContactBinding>(
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


    override fun bind(binding: ItemContactBinding, item: Contact) {
        binding.setVariable(BR.contact, item)
        binding.lnItem.setOnClickListener {
            onItemClick?.invoke(item)
        }
        binding.executePendingBindings()


    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemContactBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_contact,
            parent,
            false
        )
}