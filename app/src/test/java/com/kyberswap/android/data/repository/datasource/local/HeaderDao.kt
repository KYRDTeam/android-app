package com.kyberswap.android.data.repository.datasource.local

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import com.kyberswap.android.data.api.home.entity.HeaderEntity
import io.reactivex.Flowable

@Dao
interface HeaderDao {
    @get:Query("SELECT * FROM HeaderEntity")
    val all: Flowable<HeaderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(headerEntity: HeaderEntity)

    @Query("DELETE FROM HeaderEntity")
    fun deleteAll()

    @Transaction
    fun updateData(headerEntity: HeaderEntity) {
        deleteAll()
        insert(headerEntity)
    }

    @Delete
    fun delete(headerEntity: HeaderEntity)
}
