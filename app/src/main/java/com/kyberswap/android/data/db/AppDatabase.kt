package com.kyberswap.android.data.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        Contact::class,
        Transaction::class,
        Order::class,
        UserInfo::class,
        LocalLimitOrder::class,
        OrderFilter::class,
        Alert::class,
        PassCode::class,
        PendingBalances::class,
        TransactionFilter::class
    ],
    version = 3
)
@TypeConverters(
    DataTypeConverter::class,
    BigIntegerDataTypeConverter::class,
    TransactionTypeConverter::class,
    TokenPairTypeConverter::class,
    ListStringConverter::class,
    WalletBalanceTypeConverter::class,
    PendingBalancesConverter::class,
    TransactionTypesConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): TokenDao
    abstract fun walletDao(): WalletDao
    abstract fun unitDao(): UnitDao
    abstract fun swapDao(): SwapDao
    abstract fun walletTokenDao(): WalletTokenDao
    abstract fun rateDao(): RateDao
    abstract fun contactDao(): ContactDao
    abstract fun sendDao(): SendDao
    abstract fun transactionDao(): TransactionDao
    abstract fun limitOrderDao(): LimitOrderDao
    abstract fun userDao(): UserDao
    abstract fun localLimitOrderDao(): LocalLimitOrderDao
    abstract fun orderFilterDao(): OrderFilterDao
    abstract fun alertDao(): AlertDao
    abstract fun passCodeDao(): PassCodeDao
    abstract fun pendingBalancesDao(): PendingBalancesDao
    abstract fun transactionFilterDao(): TransactionFilterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: buildDatabase(context).also { INSTANCE = it }
                }

        @VisibleForTesting
        internal val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tokens " + " ADD COLUMN limitOrderBalance TEXT NOT NULL default '' ")
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "kyberswap.db"
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}
