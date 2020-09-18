package com.kyberswap.android.presentation.main.walletconnect.service

import android.content.Intent
import android.os.IBinder
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectKillSessionUseCase
import dagger.android.DaggerService
import javax.inject.Inject

class WcSessionManagerService : DaggerService() {
    @Inject
    lateinit var killSessionUseCase: WalletConnectKillSessionUseCase
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        killSessionUseCase.execute(
            {

            },
            {
                it.printStackTrace()

            },
            WalletConnectKillSessionUseCase.Param()
        )
        this.stopSelf()
    }
}