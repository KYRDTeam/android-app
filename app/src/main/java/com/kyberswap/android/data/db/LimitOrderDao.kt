package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Order
import io.reactivex.Flowable

/**
 * Data Access Object for the orders table.
 */
@Dao
interface LimitOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrders(orders: List<Order>)

    @Update
    fun updateOrder(order: Order)

    @Transaction
    fun updateOrders(orders: List<Order>) {
        deleteAllOrders()
        insertOrders(orders)
    }

    @Delete
    fun deleteOrder(orders: List<Order>)

    @Query("SELECT * from orders where userAddr = :address")
    fun findOrderByAddressFlowable(address: String): Flowable<Order>

    @Query("SELECT * from orders where userAddr = :address")
    fun findOrdersByAddressFlowable(address: String): Flowable<List<Order>>

    @Query("SELECT * from orders where userAddr = :address AND src = :src AND dst = :dest")
    fun findRelatedOrdersByAddressFlowable(
        address: String,
        src: String,
        dest: String
    ): Flowable<List<Order>>

    @Query("SELECT * from orders where userAddr = :address LIMIT 1")
    fun findOrderByAddress(address: String): Order?

    @Query("SELECT * from orders where userAddr = :address")
    fun findAllOrdersByAddress(address: String): List<Order>

    @Query("DELETE FROM orders")
    fun deleteAllOrders()

    @Query("SELECT * from orders where id = :orderId")
    fun findOrderById(orderId: Long): Order?

    @Delete
    fun delete(model: Order)

    @get:Query("SELECT * FROM orders")
    val all: Flowable<List<Order>>
}

