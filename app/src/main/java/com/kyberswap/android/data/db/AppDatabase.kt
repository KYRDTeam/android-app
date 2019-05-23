package com.kyberswap.android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.model.Unit

@Database(
    entities = [
        Token::class,
        Wallet::class,
        Unit::class,
        Swap::class,
        Send::class,
        WalletToken::class,
        Rate::class,
        Contact::class
    ],
    version = 18
)
@TypeConverters(DataTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): TokenDao
    abstract fun walletDao(): WalletDao
    abstract fun unitDao(): UnitDao
    abstract fun swapDao(): SwapDao
    abstract fun walletTokenDao(): WalletTokenDao
    abstract fun rateDao(): RateDao
    abstract fun contactDao(): ContactDao
    abstract fun sendDao(): SendDao

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
                AppDatabase::class.java, "kyberswap.db"
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}
