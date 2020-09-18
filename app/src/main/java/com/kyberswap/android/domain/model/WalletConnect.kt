package com.kyberswap.android.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.db.WalletConnectDataTypeConverter
import org.jetbrains.annotations.NotNull

@Entity(tableName = "walletconnects")
data class WalletConnect(
    @PrimaryKey
    @NotNull
    var address: String = "",
    var sessionInfo: String = "",
    @TypeConverters(WalletConnectDataTypeConverter::class)
    var wcSessionRequest: WcSessionRequest? = null,
    @TypeConverters(WalletConnectDataTypeConverter::class)
    var wcEthSendTransaction: WcEthSendTransaction? = null,
    @TypeConverters(WalletConnectDataTypeConverter::class)
    var wcEthSign: WcEthSign? = null,
    @Ignore
    var hasSession: Boolean = false
)