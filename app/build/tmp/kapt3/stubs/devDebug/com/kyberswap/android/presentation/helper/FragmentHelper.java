package com.kyberswap.android.presentation.helper;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J,\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\nH\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/kyberswap/android/presentation/helper/FragmentHelper;", "", "fragmentManager", "Landroid/support/v4/app/FragmentManager;", "(Landroid/support/v4/app/FragmentManager;)V", "replaceFragment", "", "fragment", "Landroid/support/v4/app/Fragment;", "containerId", "", "addToBackStack", "", "customAnimations", "Companion", "app_devDebug"})
public final class FragmentHelper {
    private final android.support.v4.app.FragmentManager fragmentManager = null;
    public static final int IN_RIGHT_OUT_LEFT = 1;
    public static final int IN_LEFT_OUT_RIGHT = -1;
    public static final int WITHOUT_ANIMATION = 0;
    public static final com.kyberswap.android.presentation.helper.FragmentHelper.Companion Companion = null;
    
    public final void replaceFragment(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment fragment, int containerId, boolean addToBackStack, int customAnimations) {
    }
    
    public final void replaceFragment(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment fragment, int containerId, boolean addToBackStack) {
    }
    
    public final void replaceFragment(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment fragment, int containerId) {
    }
    
    @javax.inject.Inject()
    public FragmentHelper(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.FragmentManager fragmentManager) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/kyberswap/android/presentation/helper/FragmentHelper$Companion;", "", "()V", "IN_LEFT_OUT_RIGHT", "", "IN_RIGHT_OUT_LEFT", "WITHOUT_ANIMATION", "app_devDebug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}