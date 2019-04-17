package com.kyberswap.android.data.repository.datasource.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.kyberswap.android.data.api.home.entity.HeaderEntity

@Database(entities = [HeaderEntity::class], version = 5)
@TypeConverters(
    HeaderTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleFeatureDao(): HeaderDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: buildDatabase(context).also { INSTANCE = it }
        

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "iStyle.db"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
