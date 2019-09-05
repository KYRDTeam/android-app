package com.kyberswap.android.data.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.Contact
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.PassCode
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Rate
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.model.Unit
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.WalletToken

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
    version = 4
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

        @VisibleForTesting
        internal val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        @VisibleForTesting
        internal val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS new_transactions (`blockHash` TEXT NOT NULL, `blockNumber` TEXT NOT NULL, `confirmations` TEXT NOT NULL, `contractAddress` TEXT NOT NULL, `cumulativeGasUsed` TEXT NOT NULL, `from` TEXT NOT NULL, `gas` TEXT NOT NULL, `gasPrice` TEXT NOT NULL, `gasUsed` TEXT NOT NULL, `hash` TEXT NOT NULL, `input` TEXT NOT NULL, `isError` TEXT NOT NULL, `nonce` TEXT NOT NULL, `timeStamp` INTEGER NOT NULL, `to` TEXT NOT NULL, `transactionIndex` TEXT NOT NULL, `txreceiptStatus` TEXT NOT NULL, `value` TEXT NOT NULL, `tokenName` TEXT NOT NULL, `tokenSymbol` TEXT NOT NULL, `tokenDecimal` TEXT NOT NULL, `type` INTEGER NOT NULL, `txType` TEXT NOT NULL, `tokenSource` TEXT NOT NULL, `sourceAmount` TEXT NOT NULL, `tokenDest` TEXT NOT NULL, `destAmount` TEXT NOT NULL, `transactionStatus` TEXT NOT NULL, `walletAddress` TEXT NOT NULL, PRIMARY KEY(`hash`, `from`, `to`))
                """.trimIndent()
                )

                database.execSQL(
                    """
                        CREATE  INDEX `index_transactions_transactionStatus_walletAddress` ON new_transactions (`hash`, `transactionStatus`, `walletAddress`)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                INSERT INTO new_transactions (blockHash, blockNumber, confirmations, contractAddress, cumulativeGasUsed, `from`, gas, gasPrice, gasUsed, hash, input, isError, nonce, timeStamp, `to`, transactionIndex, txreceiptStatus, value, tokenName, tokenSymbol, tokenDecimal, type, txType, tokenSource, sourceAmount, tokenDest, destAmount, transactionStatus, walletAddress)
                SELECT blockHash, blockNumber, confirmations, contractAddress, cumulativeGasUsed, `from`, gas, gasPrice, gasUsed, hash, input, isError, nonce, timeStamp, `to`, transactionIndex, txreceiptStatus, value, tokenName, tokenSymbol, tokenDecimal, type, txType, tokenSource, sourceAmount, tokenDest, destAmount, transactionStatus, walletAddress FROM transactions
                """.trimIndent()
                )
                database.execSQL("DROP TABLE transactions")
                database.execSQL("ALTER TABLE new_transactions RENAME TO transactions")
            }
        }


        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "kyberswap.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
//                .fallbackToDestructiveMigration()
//                .allowMainThreadQueries()
                .build()
    }
}
