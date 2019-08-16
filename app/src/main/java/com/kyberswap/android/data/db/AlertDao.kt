package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Alert
import io.reactivex.Flowable

/**
 * Data Access Object for the alerts table.
 */
@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlert(alert: Alert)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBatchAlerts(alerts: List<Alert>)

    @Transaction
    fun updateAlerts(alerts: List<Alert>) {
        deleteAllAlerts()
        insertBatchAlerts(alerts)
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAlert(alert: Alert)

    @Query("SELECT * from alerts where walletAddress = :address")
    fun findAlertByAddressFlowable(address: String): Flowable<Alert>

    @Query("SELECT * from alerts where walletAddress = :address LIMIT 1")
    fun findAlertByAddress(address: String): Alert?

    @Query("SELECT * from alerts where walletAddress = :address and state = :state LIMIT 1")
    fun findLocalAlert(address: String, state: String = Alert.STATE_LOCAL): Alert?


    @Query("SELECT * from alerts where id = :id")
    fun findAlertById(id: Long): Alert?

    @Query("SELECT * from alerts where id = :id")
    fun findAlertByIdFlowable(id: Long): Flowable<Alert>

    @Query("SELECT * from alerts where walletAddress = :address and state = :state LIMIT 1")
    fun findLocalAlertFlowable(address: String, state: String = Alert.STATE_LOCAL): Flowable<Alert>

    @Query("DELETE FROM alerts")
    fun deleteAllAlerts()

    @Query("DELETE FROM alerts WHERE id = :id")
    fun deleteById(id: Long)

    @Delete
    fun delete(model: Alert)

    @get:Query("SELECT * FROM alerts")
    val all: Flowable<List<Alert>>

    @Query("SELECT * FROM alerts")
    fun allAlerts(): List<Alert>
}

