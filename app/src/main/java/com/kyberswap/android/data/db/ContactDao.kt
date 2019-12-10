package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kyberswap.android.domain.model.Contact
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Data Access Object for the contacts table.
 */
@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContact(contact: Contact)

    @Update
    fun updateContact(contact: Contact)

    @Delete
    fun deleteContactCompletable(contact: Contact): Completable

    @Query("SELECT * from contacts where address = :address")
    fun loadContactByAddress(address: String): Flowable<Contact>

    @Query("SELECT * from contacts where address COLLATE NOCASE = :address")
    fun findContactByAddress(address: String): Contact?

    @Query("SELECT * from contacts where address COLLATE NOCASE = :address")
    fun findAllContactByAddress(address: String): List<Contact>

    @Transaction
    fun updateContactAndRemoveDuplicate(contact: Contact) {
        val contactList = findAllContactByAddress(contact.address)
        if (contactList.isNotEmpty()) {
            deleteContactList(contactList)
        }
        insertContact(contact)
    }

    @Query("DELETE FROM contacts")
    fun deleteAllContacts()

    @Delete
    fun deleteContactList(contacts: List<Contact>)

    @Query("SELECT * from contacts where walletAddress = :walletAddress")
    fun loadContactByWalletAddress(walletAddress: String): Flowable<List<Contact>>

    @get:Query("SELECT * FROM contacts")
    val all: Flowable<List<Contact>>
}

