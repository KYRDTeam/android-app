package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.UserInfo
import io.reactivex.Flowable

/**
 * Data Access Object for the users table.
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserInfo(userInfo: UserInfo)

    @Transaction
    fun updateUser(userInfo: UserInfo) {
        deleteAllUsers()
        insertUserInfo(userInfo)
    }

    @Update
    fun updateUserInfo(userInfo: UserInfo)

    @Query("DELETE FROM users")
    fun deleteAllUsers()

    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): UserInfo?

    @get:Query("SELECT * FROM users")
    val all: Flowable<UserInfo>

}

