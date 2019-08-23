package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.LocalLimitOrder
import io.reactivex.Flowable

/**
 * Data Access Object for the current_order table.
 */
@Dao
interface LocalLimitOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(localLimitOrder: LocalLimitOrder)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateOrder(localLimitOrder: LocalLimitOrder)

    @Query("SELECT * from current_orders where userAddr = :address")
    fun findLocalLimitOrderByAddressFlowable(address: String): Flowable<LocalLimitOrder>

    @Query("SELECT * from current_orders where userAddr = :address LIMIT 1")
    fun findLocalLimitOrderByAddress(address: String): LocalLimitOrder?

    @Query("DELETE FROM current_orders")
    fun deleteAllLocalLimitOrders()

    @Delete
    fun delete(model: LocalLimitOrder)

    @get:Query("SELECT * FROM current_orders")
    val all: Flowable<List<LocalLimitOrder>>
}

