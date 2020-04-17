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
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.PassCode
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Rate
import com.kyberswap.android.domain.model.RatingInfo
import com.kyberswap.android.domain.model.SelectedMarketItem
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.TokenExt
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.model.Unit
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet

@Database(
    entities = [
        Token::class,
        TokenExt::class,
        Wallet::class,
        Unit::class,
        Swap::class,
        Send::class,
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
        TransactionFilter::class,
        RatingInfo::class,
        MarketItem::class,
        SelectedMarketItem::class
    ],
    version = 11
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

    abstract fun tokenDao(): TokenDao
    abstract fun walletDao(): WalletDao
    abstract fun unitDao(): UnitDao
    abstract fun swapDao(): SwapDao
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
    abstract fun tokenExtDao(): TokenExtDao
    abstract fun ratingDao(): RatingDao
    abstract fun marketDao(): MarketDao
    abstract fun selectedMarketDao(): SelectedMarketDao

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

        @VisibleForTesting
        internal val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // for adding transfer fee
                database.execSQL("ALTER TABLE current_orders " + " ADD COLUMN transferFee TEXT NOT NULL default '' ")
            }
        }

        @VisibleForTesting
        internal val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // for adding transfer fee
                database.execSQL("CREATE TABLE IF NOT EXISTS new_tokens (`isHide` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `tokenSymbol` TEXT NOT NULL, `tokenName` TEXT NOT NULL, `tokenAddress` TEXT NOT NULL, `tokenDecimal` INTEGER NOT NULL, `rateEthNow` TEXT NOT NULL, `changeEth24h` TEXT NOT NULL, `rateUsdNow` TEXT NOT NULL, `changeUsd24h` TEXT NOT NULL, `cgId` TEXT NOT NULL, `gasApprove` TEXT NOT NULL, `gasLimit` TEXT NOT NULL, `listingTime` INTEGER NOT NULL, `priority` INTEGER NOT NULL, `spLimitOrder` INTEGER NOT NULL, `wallets` TEXT NOT NULL, `fav` INTEGER NOT NULL, `isOther` INTEGER NOT NULL, `limitOrderBalance` TEXT NOT NULL, `isQuote` INTEGER NOT NULL, PRIMARY KEY(`tokenAddress`))")


                database.execSQL(
                    """
                INSERT OR REPLACE INTO new_tokens (isHide, timestamp, tokenSymbol, tokenName, tokenAddress, tokenDecimal, rateEthNow, changeEth24h, rateUsdNow, changeUsd24h, cgId, gasApprove, gasLimit, listingTime, priority, spLimitOrder, wallets, fav, isOther, limitOrderBalance, isQuote)
                SELECT isHide, timestamp, tokenSymbol, tokenName, tokenAddress, tokenDecimal, rateEthNow, changeEth24h, rateUsdNow, changeUsd24h, cgId, gasApprove, gasLimit, listingTime, priority, spLimitOrder, wallets, fav, isOther, limitOrderBalance, isQuote FROM tokens
                """.trimIndent()
                )
                database.execSQL("DROP TABLE tokens")
                database.execSQL("ALTER TABLE new_tokens RENAME TO tokens")
            }
        }

        @VisibleForTesting
        internal val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new TokenExtTable
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS token_extras (`tokenSymbol` TEXT NOT NULL, `tokenName` TEXT NOT NULL, `tokenAddress` TEXT NOT NULL, `isGasFixed` INTEGER NOT NULL, `gasLimit` TEXT NOT NULL, `delistTime` INTEGER NOT NULL, PRIMARY KEY(`tokenAddress`))
                """.trimIndent()
                )
            }
        }

        @VisibleForTesting
        internal val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new TokenExtTable
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ratings (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `updatedAt` INTEGER NOT NULL, `count` INTEGER NOT NULL, `isNotNow` INTEGER NOT NULL, `isFinished` INTEGER NOT NULL)
                """.trimIndent()
                )
            }
        }

        @VisibleForTesting
        internal val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new TokenExtTable
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `new_transactions` (`blockHash` TEXT NOT NULL, `blockNumber` TEXT NOT NULL, `confirmations` TEXT NOT NULL, `contractAddress` TEXT NOT NULL, `cumulativeGasUsed` TEXT NOT NULL, `from` TEXT NOT NULL, `gas` TEXT NOT NULL, `gasPrice` TEXT NOT NULL, `gasUsed` TEXT NOT NULL, `hash` TEXT NOT NULL, `input` TEXT NOT NULL, `isError` TEXT NOT NULL, `nonce` TEXT NOT NULL, `timeStamp` INTEGER NOT NULL, `to` TEXT NOT NULL, `transactionIndex` TEXT NOT NULL, `txreceiptStatus` TEXT NOT NULL, `value` TEXT NOT NULL, `tokenName` TEXT NOT NULL, `tokenSymbol` TEXT NOT NULL, `tokenDecimal` TEXT NOT NULL, `type` INTEGER NOT NULL, `txType` TEXT NOT NULL, `tokenSource` TEXT NOT NULL, `sourceAmount` TEXT NOT NULL, `tokenDest` TEXT NOT NULL, `destAmount` TEXT NOT NULL, `transactionStatus` TEXT NOT NULL, `walletAddress` TEXT NOT NULL, `isCancel` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`hash`, `from`, `to`))
                """.trimIndent()
                )


                database.execSQL(
                    """
                        DROP INDEX IF EXISTS index_transactions_transactionStatus_walletAddress;

                    """.trimIndent()
                )

                database.execSQL(
                    """
                        CREATE  INDEX  `index_transactions_transactionStatus_walletAddress` ON new_transactions (`hash`, `transactionStatus`, `walletAddress`)
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

        @VisibleForTesting
        internal val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users " + " ADD COLUMN priceNoti INTEGER NOT NULL default 0 ")
            }
        }

        @VisibleForTesting
        internal val MIGRATION_10_11: Migration = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // add quotePriority to token_extras table
                database.execSQL("ALTER TABLE token_extras " + " ADD COLUMN quotePriority INTEGER NOT NULL default 0 ")

                // add sideTrade to orders table
                database.execSQL("ALTER TABLE orders " + " ADD COLUMN sideTrade TEXT NOT NULL default '' ")

                // Add new markets table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `markets` (`buyPrice` TEXT NOT NULL, `change` TEXT NOT NULL, `pair` TEXT NOT NULL, `sellPrice` TEXT NOT NULL, `volume` TEXT NOT NULL, `isFav` INTEGER NOT NULL, PRIMARY KEY(`pair`))
                """.trimIndent()
                )

                // Update primary key for current_orders (userAddr, type)
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `new_current_orders` (`userAddr` TEXT NOT NULL, `srcAmount` TEXT NOT NULL, `marketRate` TEXT NOT NULL, `expectedRate` TEXT NOT NULL, `minRate` TEXT NOT NULL, `destAddr` TEXT NOT NULL, `nonce` TEXT NOT NULL, `fee` TEXT NOT NULL, `status` TEXT NOT NULL, `txHash` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `gasLimit` TEXT NOT NULL, `gasPrice` TEXT NOT NULL, `transferFee` TEXT NOT NULL, `type` INTEGER NOT NULL, `source_isHide` INTEGER NOT NULL, `source_timestamp` INTEGER NOT NULL, `source_tokenSymbol` TEXT NOT NULL, `source_tokenName` TEXT NOT NULL, `source_tokenAddress` TEXT NOT NULL, `source_tokenDecimal` INTEGER NOT NULL, `source_rateEthNow` TEXT NOT NULL, `source_changeEth24h` TEXT NOT NULL, `source_rateUsdNow` TEXT NOT NULL, `source_changeUsd24h` TEXT NOT NULL, `source_cgId` TEXT NOT NULL, `source_gasApprove` TEXT NOT NULL, `source_gasLimit` TEXT NOT NULL, `source_listingTime` INTEGER NOT NULL, `source_priority` INTEGER NOT NULL, `source_spLimitOrder` INTEGER NOT NULL, `source_wallets` TEXT NOT NULL, `source_fav` INTEGER NOT NULL, `source_isOther` INTEGER NOT NULL, `source_limitOrderBalance` TEXT NOT NULL, `source_isQuote` INTEGER NOT NULL, `dest_isHide` INTEGER NOT NULL, `dest_timestamp` INTEGER NOT NULL, `dest_tokenSymbol` TEXT NOT NULL, `dest_tokenName` TEXT NOT NULL, `dest_tokenAddress` TEXT NOT NULL, `dest_tokenDecimal` INTEGER NOT NULL, `dest_rateEthNow` TEXT NOT NULL, `dest_changeEth24h` TEXT NOT NULL, `dest_rateUsdNow` TEXT NOT NULL, `dest_changeUsd24h` TEXT NOT NULL, `dest_cgId` TEXT NOT NULL, `dest_gasApprove` TEXT NOT NULL, `dest_gasLimit` TEXT NOT NULL, `dest_listingTime` INTEGER NOT NULL, `dest_priority` INTEGER NOT NULL, `dest_spLimitOrder` INTEGER NOT NULL, `dest_wallets` TEXT NOT NULL, `dest_fav` INTEGER NOT NULL, `dest_isOther` INTEGER NOT NULL, `dest_limitOrderBalance` TEXT NOT NULL, `dest_isQuote` INTEGER NOT NULL, `eth_isHide` INTEGER NOT NULL, `eth_timestamp` INTEGER NOT NULL, `eth_tokenSymbol` TEXT NOT NULL, `eth_tokenName` TEXT NOT NULL, `eth_tokenAddress` TEXT NOT NULL, `eth_tokenDecimal` INTEGER NOT NULL, `eth_rateEthNow` TEXT NOT NULL, `eth_changeEth24h` TEXT NOT NULL, `eth_rateUsdNow` TEXT NOT NULL, `eth_changeUsd24h` TEXT NOT NULL, `eth_cgId` TEXT NOT NULL, `eth_gasApprove` TEXT NOT NULL, `eth_gasLimit` TEXT NOT NULL, `eth_listingTime` INTEGER NOT NULL, `eth_priority` INTEGER NOT NULL, `eth_spLimitOrder` INTEGER NOT NULL, `eth_wallets` TEXT NOT NULL, `eth_fav` INTEGER NOT NULL, `eth_isOther` INTEGER NOT NULL, `eth_limitOrderBalance` TEXT NOT NULL, `eth_isQuote` INTEGER NOT NULL, `weth_isHide` INTEGER NOT NULL, `weth_timestamp` INTEGER NOT NULL, `weth_tokenSymbol` TEXT NOT NULL, `weth_tokenName` TEXT NOT NULL, `weth_tokenAddress` TEXT NOT NULL, `weth_tokenDecimal` INTEGER NOT NULL, `weth_rateEthNow` TEXT NOT NULL, `weth_changeEth24h` TEXT NOT NULL, `weth_rateUsdNow` TEXT NOT NULL, `weth_changeUsd24h` TEXT NOT NULL, `weth_cgId` TEXT NOT NULL, `weth_gasApprove` TEXT NOT NULL, `weth_gasLimit` TEXT NOT NULL, `weth_listingTime` INTEGER NOT NULL, `weth_priority` INTEGER NOT NULL, `weth_spLimitOrder` INTEGER NOT NULL, `weth_wallets` TEXT NOT NULL, `weth_fav` INTEGER NOT NULL, `weth_isOther` INTEGER NOT NULL, `weth_limitOrderBalance` TEXT NOT NULL, `weth_isQuote` INTEGER NOT NULL, PRIMARY KEY(`userAddr`, `type`))
                """.trimIndent()
                )

                // Add new selected_markets table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `selected_markets` (`walletAddress` TEXT NOT NULL, `pair` TEXT NOT NULL, PRIMARY KEY(`walletAddress`))
                """.trimIndent()
                )

                database.execSQL("DROP TABLE current_orders")
                database.execSQL("ALTER TABLE new_current_orders RENAME TO current_orders")
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "kyberswap.db"
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11
                )
//                .fallbackToDestructiveMigration()
//                .allowMainThreadQueries()
                .build()
    }
}
