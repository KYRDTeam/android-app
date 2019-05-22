package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Contact
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

    @Query("SELECT * from contacts where address = :address")
    fun loadContactByAddress(address: String): Flowable<Contact>

    @Query("SELECT * from contacts where address = :address")
    fun findContactByAddress(address: String): Contact

    @Query("DELETE FROM contacts")
    fun deleteAllContacts()

    @get:Query("SELECT * FROM contacts")
    val all: List<Contact>
}

