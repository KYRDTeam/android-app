package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.data.db.ContactDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.repository.ContactRepository
import com.kyberswap.android.domain.usecase.contact.DeleteContactUseCase
import com.kyberswap.android.domain.usecase.contact.GetContactUseCase
import com.kyberswap.android.domain.usecase.contact.SaveContactUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.Locale
import javax.inject.Inject


class ContactDataRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val sendDao: SendDao,
    private val context: Context

) : ContactRepository {

    override fun saveContact(param: SaveContactUseCase.Param): Completable {
        return Completable.fromCallable {
            val name = if (param.name.isEmpty()) {
                context.getString(R.string.default_wallet_name)
            } else param.name

            val findContactByAddress = contactDao.findContactByAddress(param.address)
            val updatedAt = System.currentTimeMillis() / 1000
            val contact = findContactByAddress?.copy(
                address = param.address.toLowerCase(Locale.getDefault()),
                name = name,
                updatedAt = updatedAt
            ) ?: Contact(
                param.walletAddress,
                param.address.toLowerCase(Locale.getDefault()),
                name,
                updatedAt
            )
            contactDao.insertContact(contact)

            if (param.isSend) {
                val send = sendDao.findSendByAddress(param.walletAddress)
                send?.let {
                    sendDao.updateSend(it.copy(contact = contact))
                }
            }

        }
    }

    override fun getContacts(param: GetContactUseCase.Param): Flowable<List<Contact>> {
        return contactDao.all.map { contacts ->
            contacts.sortedByDescending { it.updatedAt }
        }
    }

    override fun deleteContact(param: DeleteContactUseCase.Param): Completable {
        return contactDao.deleteContactCompletable(param.contact)
    }
}