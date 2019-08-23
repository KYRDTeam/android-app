package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.OrderFilter
import io.reactivex.Flowable

/**
 * Data Access Object for the order_filter table.
 */
@Dao
interface OrderFilterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrderFilter(filter: OrderFilter)

    @Transaction
    fun updateOrderFilter(orderFilter: OrderFilter) {
        deleteAllOrderFilters()
        insertOrderFilter(orderFilter)
    }

    @Query("SELECT * from order_filter where walletAddress = :address")
    fun findOrderFilterByAddressFlowable(address: String): Flowable<OrderFilter>

    @get:Query("SELECT * from order_filter LIMIT 1")
    val filter: OrderFilter?

    @get:Query("SELECT * from order_filter")
    val filterFlowable: Flowable<OrderFilter>

    @Query("DELETE FROM order_filter")
    fun deleteAllOrderFilters()

    @Delete
    fun delete(model: OrderFilter)

    @get:Query("SELECT * FROM order_filter")
    val all: Flowable<List<OrderFilter>>
}