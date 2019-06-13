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

    @Update
    fun updateOrderFilter(orderFilter: OrderFilter)

    @Query("SELECT * from order_filter where walletAddress = :address")
    fun findOrderFilterByAddressFlowable(address: String): Flowable<OrderFilter>

    @Query("SELECT * from order_filter where walletAddress = :address LIMIT 1")
    fun findOrderFilterByAddress(address: String): OrderFilter?

    @Query("DELETE FROM order_filter")
    fun deleteAllOrderFilters()

    @Delete
    fun delete(model: OrderFilter)

    @get:Query("SELECT * FROM order_filter")
    val all: Flowable<List<OrderFilter>>
}