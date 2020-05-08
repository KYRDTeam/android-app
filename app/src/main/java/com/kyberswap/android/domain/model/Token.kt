package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.currencies.TokenCurrencyEntity
import com.kyberswap.android.data.api.token.TokenEntity
import com.kyberswap.android.data.db.DataTypeConverter
import com.kyberswap.android.data.db.WalletBalanceTypeConverter
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.rounding
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale

@Entity(tableName = "tokens")
@Parcelize
data class Token(
    val timestamp: Long = 0,
    val tokenSymbol: String = "",
    val tokenName: String = "",
    @NonNull
    @PrimaryKey
    val tokenAddress: String = "",
    val tokenDecimal: Int = 0,
    @TypeConverters(DataTypeConverter::class)
    val rateEthNow: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val changeEth24h: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val rateUsdNow: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val changeUsd24h: BigDecimal = BigDecimal.ZERO,
    val cgId: String = "",
    @TypeConverters(DataTypeConverter::class)
    val gasApprove: BigDecimal = BigDecimal.ZERO,
    val gasLimit: String = "",
    val listingTime: Long = 0,
    val priority: Boolean = false,
    val spLimitOrder: Boolean = false,
    @TypeConverters(WalletBalanceTypeConverter::class)
    val wallets: List<WalletBalance> = listOf(),
    var fav: Boolean = false,
    val isOther: Boolean = false,
    val limitOrderBalance: BigDecimal = BigDecimal.ZERO,
    val isQuote: Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    var isHide: Boolean = false

    @IgnoredOnParcel
    @Ignore
    var delistTime: Long = 0

    @IgnoredOnParcel
    @Ignore
    var isGasFixed: Boolean = false

    @IgnoredOnParcel
    @Ignore
    var isEven: Boolean = false

    @IgnoredOnParcel
    @Ignore
    var quotePriority: Int = 0

    val isDelist: Boolean
        get() {
            return delistTime > 0 && delistTime > listingTime
        }

    val rateEthNowOrDefaultValue: BigDecimal
        get() = if (isETH || isWETH || isETHWETH) BigDecimal.ONE else rateEthNow

    val currentBalance: BigDecimal
        get() {
            return wallets.find {
                it.isSelected
            }?.currentBalance
                ?: BigDecimal.ZERO
        }

    val allBalance: BigDecimal
        get() {
            var balance = BigDecimal.ZERO
            wallets.forEach {
                balance += it.currentBalance
            }
            return balance
        }

    val sourceAmountWithoutRounding: String
        get() = currentBalance.stripTrailingZeros().toPlainString()

    constructor(entity: TokenEntity) : this(
        entity.timestamp,
        entity.tokenSymbol,
        entity.tokenName,
        entity.tokenAddress,
        entity.tokenDecimal,
        entity.rateEthNow,
        entity.changeEth24h,
        entity.rateUsdNow,
        entity.changeUsd24h
    )

    constructor(tx: Transaction) : this(
        tokenName = tx.tokenName,
        tokenAddress = tx.contractAddress,
        tokenSymbol = tx.tokenSymbol,
        tokenDecimal = tx.tokenDecimal.toBigIntegerOrDefaultZero().toInt()
    )

    constructor(entity: TokenCurrencyEntity) : this(
        tokenSymbol = entity.symbol,
        tokenName = entity.name,
        tokenAddress = entity.address.toLowerCase(Locale.getDefault()),
        tokenDecimal = entity.decimals,
        cgId = entity.cgId,
        gasApprove = entity.gasApprove,
        gasLimit = entity.gasLimit,
        listingTime = entity.listingTime,
        priority = entity.priority,
        spLimitOrder = entity.spLimitOrder ?: false,
        isQuote = entity.isQuote ?: false
    ) {
        delistTime = entity.delistTime ?: 0L
        isGasFixed = entity.isGasFixed ?: false
        quotePriority = entity.quotePriority ?: 0
    }

    val symbol: String
        get() = if (tokenSymbol == ETH_SYMBOL_STAR) WETH_SYMBOL else tokenSymbol

    private val currentWalletBalance: WalletBalance?
        get() = wallets.find { it.isSelected }

    val selectedWalletAddress: String
        get() = currentWalletBalance?.walletAddress ?: ""

    val isListed: Boolean
        get() {
            return (System.currentTimeMillis() / 1000 - listingTime >= 0) && !isDelist
        }

    val shouldShowAsNew: Boolean
        get() = 7.0 * 24.0 * 60.0 * 60.0 >= System.currentTimeMillis() / 1000 - listingTime && System.currentTimeMillis() / 1000 - listingTime >= 0

    val submitOrderTokenSymbol: String
        get() = if (isETHWETH) WETH_SYMBOL else tokenSymbol

    fun with(entity: TokenCurrencyEntity): Token {
        return copy(
            tokenSymbol = entity.symbol,
            tokenName = entity.name,
            tokenAddress = entity.address,
            tokenDecimal = entity.decimals,
            cgId = entity.cgId,
            gasApprove = entity.gasApprove,
            gasLimit = entity.gasLimit,
            listingTime = entity.listingTime,
            priority = entity.priority,
            spLimitOrder = entity.spLimitOrder ?: false,
            isQuote = entity.isQuote ?: false
        ).apply {
            delistTime = entity.delistTime ?: 0L
            isGasFixed = entity.isGasFixed ?: false
            quotePriority = entity.quotePriority ?: 0
        }
    }

    fun updateSelectedWallet(wallet: Wallet?): Token {
        if (this.selectedWalletAddress == wallet?.address) return this
        if (wallet == null) return this
        val walletBalances =
            wallets.map {
                it.copy(isSelected = false)
            }.toMutableList()

        val walletBalance = wallets.find { it1 -> it1.walletAddress == wallet.address }
        if (walletBalance == null) {
            walletBalances.add(
                WalletBalance(
                    wallet.address,
                    BigDecimal.ZERO,
                    true
                )
            )
        } else {
            val idx = walletBalances.indexOf(walletBalance)
            if (idx >= 0) {
                walletBalances[idx] = walletBalance.copy(isSelected = true)
            }
        }

        return copy(wallets = walletBalances)
    }

    fun updateSelectedWallet(listWallets: List<Wallet>): Token {
        val walletBalances = listWallets.map { wallet ->
            val walletBalance = wallets.find { it.walletAddress == wallet.address }
            WalletBalance(
                wallet.address,
                walletBalance?.currentBalance ?: BigDecimal.ZERO,
                wallet.isSelected
            )
        }

        return this.copy(wallets = walletBalances)
    }

    fun deleteWallet(wallet: Wallet): Token {
        val walletBalance = wallets.find { it.walletAddress == wallet.address }
        walletBalance?.let {
            val updateBalances = wallets.toMutableList()
            updateBalances.remove(walletBalance)
            return copy(wallets = updateBalances)
        }
        return this
    }

    private fun updateBalance(walletBalance: WalletBalance?): Token {
        if (walletBalance == null) return this
        val updatedWalletBalance = wallets.map {
            if (it.walletAddress == walletBalance.walletAddress) {
                walletBalance
            } else {
                it
            }

        }
        return this.copy(wallets = updatedWalletBalance)
    }

    val owner: String?
        get() = currentWalletBalance?.walletAddress

    fun updateBalance(balance: BigDecimal): Token {
        return updateBalance(
            currentWalletBalance?.copy(
                currentBalance = balance
            )
        )
    }

    fun with(tokenAddress: String): Token {
        return this.copy(tokenAddress = tokenAddress)
    }

    val isMaintainance: Boolean
        get() = rateEthNow == BigDecimal.ZERO && rateUsdNow == BigDecimal.ZERO

    val displayRateEthNow: String
        get() = rateEthNowOrDefaultValue.toDisplayNumber()
    val displayChangeEth24h: String
        get() = changeEth24h.toDisplayNumber()
    val displayRateUsdNow: String
        get() = rateUsdNow.toDisplayNumber()
    val displayChangeUsd24h: String
        get() = changeUsd24h.toDisplayNumber()

    val displayCurrentBalance: String
        get() = if (isHide) "******" else currentBalance.rounding().toDisplayNumber().exactAmount()

    val roundingBalance: BigDecimal
        get() = if (currentBalance - currentBalance.toBigInteger()
                .toBigDecimal() > BigDecimal(1E-6)
        ) currentBalance else currentBalance.toBigInteger().toBigDecimal()

    val displayLimitOrderBalance: String
        get() = limitOrderBalance.rounding().max(BigDecimal.ZERO).toDisplayNumber().exactAmount()

    val displayCurrentBalanceInEth: String
        get() = StringBuilder()
            .append(if (isETH) "" else "≈ ")
            .append(
                if (currentBalance > BigDecimal.ZERO) currentBalance
                    .multiply(rateEthNowOrDefaultValue)
                    .toDisplayNumber() else "0"
            )
            .append(" ETH")
            .toString()

    val displayCurrentBalanceInUSD: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                if (currentBalance > BigDecimal.ZERO) currentBalance
                    .multiply(rateUsdNow)
                    .toDisplayNumber() else "0"
            )
            .append(" USD")
            .toString()

    val displayGasApprove: String
        get() = gasApprove.toDisplayNumber()

    val isETH: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == ETH_SYMBOL.toLowerCase(Locale.getDefault())

    val isWETH: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == WETH_SYMBOL.toLowerCase(Locale.getDefault())

    val isDGX: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == DGX.toLowerCase(Locale.getDefault())

    val isDAI: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == DAI.toLowerCase(Locale.getDefault())

    val isSAI: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == SAI.toLowerCase(Locale.getDefault())

    val isMKR: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == MKR.toLowerCase(Locale.getDefault())

    val isPRO: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == PRO.toLowerCase(Locale.getDefault())

    val isPT: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == PT.toLowerCase(Locale.getDefault())

    val isTUSD: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == TUSD.toLowerCase(Locale.getDefault())

    val isETHWETH: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == ETH_SYMBOL_STAR.toLowerCase(Locale.getDefault())

    val isENJ: Boolean
        get() = tokenSymbol.toLowerCase(Locale.getDefault()) == ENJ.toLowerCase(Locale.getDefault())

    fun areContentsTheSame(other: Token): Boolean {
        return this.tokenSymbol == other.tokenSymbol &&
            this.tokenAddress == other.tokenAddress &&
            this.currentBalance.toDisplayNumber() == other.currentBalance.toDisplayNumber() &&
            this.rateEthNow.toDisplayNumber() == other.rateEthNow.toDisplayNumber() &&
            this.rateUsdNow.toDisplayNumber() == other.rateUsdNow.toDisplayNumber() &&
            this.changeUsd24h.toDisplayNumber() == other.changeUsd24h.toDisplayNumber() &&
            this.changeEth24h.toDisplayNumber() == other.changeEth24h.toDisplayNumber() &&
            this.fav == other.fav &&
            this.shouldShowAsNew == other.shouldShowAsNew &&
            this.isEven == other.isEven &&
            this.isOther == other.isOther
    }

    fun change24hStatus(isEth: Boolean): Int {
        return getChange24hStatus(if (isEth) changeEth24h else changeUsd24h)
    }

    fun change24hValue(isEth: Boolean): BigDecimal {
        return if (isEth) changeEth24h else changeUsd24h
    }

    private fun getChange24hStatus(value: BigDecimal): Int {
        return when {
            value > BigDecimal.ZERO -> UP
            value < BigDecimal.ZERO -> DOWN
            else -> SAME
        }
    }

    fun updatePrecision(value: BigInteger): BigInteger {
        return value.div(BigInteger.TEN.pow(tokenDecimal))
    }

    fun withTokenDecimal(amount: BigDecimal): BigInteger {
        return amount.multiply(BigDecimal.TEN.pow(tokenDecimal)).toBigInteger()
    }

    companion object {
        const val UP = 1
        const val DOWN = -1
        const val SAME = 0
        const val ETH_SYMBOL = "ETH"
        const val ETH_SYMBOL_STAR = "ETH*"
        const val WETH_SYMBOL = "WETH"
        const val WBTC_SYMBOL = "WBTC"
        const val ETH_NAME = "Ethereum"
        const val ETH_DECIMAL = 18
        const val ETH = "ETH"
        const val KNC = "KNC"
        const val DAI = "DAI"
        const val SAI = "SAI"
        const val TUSD = "TUSD"
        const val DGX = "DGX"
        const val MKR = "MKR"
        const val PRO = "PRO"
        const val PT = "PT"
        const val ENJ = "ENJ"
        const val ENJIN = "ENJIN"
        const val FAV = "isFav"

        const val DIGIX_GAS_LIMIT_DEFAULT = 975_000
        const val EXCHANGE_ETH_TOKEN_GAS_LIMIT_DEFAULT = 500_000
        const val APPROVE_TOKEN_GAS_LIMIT_DEFAULT = 150_000
        const val TRANSFER_TOKEN_GAS_LIMIT_DEFAULT = 160_000
        const val TRANSFER_ETH_GAS_LIMIT_DEFAULT = 130_000
        const val DAI_GAS_LIMIT_DEFAULT = 650_000
        const val MAKER_GAS_LIMIT_DEFAULT = 520_000
        const val PROPY_GAS_LIMIT_DEFAULT = 650_000
        const val PROMOTION_TOKEN_GAS_LIMIT_DEFAULT = 500_000
        const val TRUE_USD_GAS_LIMIT_DEFAULT = 715_000
    }
}