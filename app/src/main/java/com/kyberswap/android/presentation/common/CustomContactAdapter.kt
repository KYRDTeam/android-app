package com.kyberswap.android.presentation.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ItemContactAutocompleteBinding
import com.kyberswap.android.domain.model.Contact
import java.util.ArrayList

class CustomContactAdapter(
    context: Context,
    resource: Int,
    contacts: List<Contact>
) : ArrayAdapter<Contact>(context, resource, contacts) {
    private val contacts: MutableList<Contact>
    private val contactsAll: List<Contact>
    override fun getCount(): Int {
        return contacts.size
    }

    override fun getItem(position: Int): Contact {
        return contacts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        if (convertView == null) {
            val binding = DataBindingUtil.inflate<ItemContactAutocompleteBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_contact_autocomplete,
                parent,
                false
            )
            binding.contact = getItem(position)
            binding.executePendingBindings()
            return binding.root
        }

        return convertView
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any): String {
                return (resultValue as Contact).name
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                val contactsSuggestion: MutableList<Contact> = ArrayList()
                for (contact in contactsAll) {
                    if (contact.name.contains(constraint.toString(), true)) {
                        contactsSuggestion.add(contact)
                    }
                }
                filterResults.values = contactsSuggestion
                filterResults.count = contactsSuggestion.size
                return filterResults
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                contacts.clear()
                if (results.count > 0) { // avoids unchecked cast warning when using contacts.addAll((ArrayList<Contact>) results.values);
                    for (`object` in results.values as List<*>) {
                        if (`object` is Contact) {
                            contacts.add(`object`)
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    init {
        this.contacts = ArrayList(contacts)
        this.contactsAll = ArrayList(contacts)
    }
}