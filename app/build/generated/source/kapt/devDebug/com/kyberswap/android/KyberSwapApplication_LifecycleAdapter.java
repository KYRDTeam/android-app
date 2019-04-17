package com.kyberswap.android;

import android.arch.lifecycle.GeneratedAdapter;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MethodCallsLogger;
import java.lang.Override;

public class KyberSwapApplication_LifecycleAdapter implements GeneratedAdapter {
  final KyberSwapApplication mReceiver;

  KyberSwapApplication_LifecycleAdapter(KyberSwapApplication receiver) {
    this.mReceiver = receiver;
  }

  @Override
  public void callMethods(LifecycleOwner owner, Lifecycle.Event event, boolean onAny,
      MethodCallsLogger logger) {
    boolean hasLogger = logger != null;
    if (onAny) {
      return;
    }
    if (event == Lifecycle.Event.ON_STOP) {
      if (!hasLogger || logger.approveCall("onAppBackgrounded", 1)) {
        mReceiver.onAppBackgrounded();
      }
      return;
    }
    if (event == Lifecycle.Event.ON_START) {
      if (!hasLogger || logger.approveCall("onAppForegrounded", 1)) {
        mReceiver.onAppForegrounded();
      }
      return;
    }
  }
}
