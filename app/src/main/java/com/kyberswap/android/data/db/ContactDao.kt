package com.kyberswap.android.data.db

import androidx.room.*
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

    @Query("SELECT * from contacts where address = :address")
    fun findContactByAddress(address: String): Contact?

    @Query("DELETE FROM contacts")
    fun deleteAllContacts()

    @Query("SELECT * from contacts where walletAddress = :walletAddress")
    fun loadContactByWalletAddress(walletAddress: String): Flowable<List<Contact>>

    @get:Query("SELECT * FROM contacts")
    val all: Flowable<List<Contact>>
}

