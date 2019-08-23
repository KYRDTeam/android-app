package com.kyberswap.android.presentation.main.alert

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemPassportBinding
import com.kyberswap.android.presentation.base.DataBoundListAdapter
import kotlinx.android.parcel.Parcelize

class PassportAdapter(
    appExecutors: AppExecutors
) : DataBoundListAdapter<Passport, ItemPassportBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Passport>() {
        override fun areItemsTheSame(oldItem: Passport, newItem: Passport): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Passport, newItem: Passport): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun bind(binding: ItemPassportBinding, item: Passport) {
        binding.setVariable(BR.passport, item)
        binding.executePendingBindings()

    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemPassportBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_passport,
            parent,
            false
        )
}

@Parcelize
data class Passport(val resourceId: Int, val content: Int) : Parcelable