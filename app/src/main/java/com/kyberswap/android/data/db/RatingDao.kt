package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kyberswap.android.domain.model.RatingInfo
import io.reactivex.Flowable

/**
 * Data Access Object for the ratings table.
 */
@Dao
interface RatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRatingInfo(ratingInfo: RatingInfo)

    @Update
    fun updateRatingInfo(RatingInfo: RatingInfo)

    @Query("DELETE FROM ratings")
    fun deleteAllRatings()

    @Query("SELECT * FROM ratings LIMIT 1")
    fun getRating(): RatingInfo?

    @get:Query("SELECT * FROM ratings")
    val all: Flowable<RatingInfo>
}

