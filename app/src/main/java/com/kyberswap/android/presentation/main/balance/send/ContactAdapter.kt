package com.kyberswap.android.presentation.main.balance.send

import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.daimajia.swipe.SimpleSwipeListener
import com.daimajia.swipe.SwipeLayout
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemContactBinding
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.presentation.base.DataBoundListSwipeAdapter
import com.kyberswap.android.presentation.base.DataBoundViewHolder


class ContactAdapter(
    appExecutors: AppExecutors,
    private val handler: Handler,
    private val onItemClick: ((Contact) -> Unit)?,
    private val onSendClick: ((Contact) -> Unit)?,
    private val onEditClick: ((Contact) -> Unit)?,
    private val onDeleteClick: ((Contact) -> Unit)?

) : DataBoundListSwipeAdapter<Contact, ItemContactBinding>(
    appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.address == newItem.address


        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.areContentsTheSame(newItem)

    }
) {
    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.swipe
    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemContactBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        mItemManger.bindView(holder.itemView, position)
        super.onBindViewHolder(holder, position, payloads)

    }


    override fun bind(binding: ItemContactBinding, item: Contact) {
        binding.setVariable(BR.contact, item)
        binding.executePendingBindings()
        binding.lnItem.setOnClickListener {
            onItemClick?.invoke(item)


        binding.btnSend.setOnClickListener {

            binding.swipe.close(true)
            handler.postDelayed({
                onSendClick?.invoke(item)
    , 250)


        binding.btnEdit.setOnClickListener {
            binding.swipe.close(true)
            handler.postDelayed({
                onEditClick?.invoke(item)
    , 250)

        binding.btnDelete.setOnClickListener {
            binding.swipe.close(true)
            handler.postDelayed(
                {
                    onDeleteClick?.invoke(item)
        , 250
            )



        binding.swipe.addSwipeListener(object : SimpleSwipeListener() {
            override fun onStartOpen(layout: SwipeLayout?) {
                mItemManger.closeAllExcept(layout)
    
)
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ItemContactBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_contact,
            parent,
            false
        )
}