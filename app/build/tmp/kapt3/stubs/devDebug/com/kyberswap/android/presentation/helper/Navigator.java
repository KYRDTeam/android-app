package com.kyberswap.android.presentation.helper;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\t\u001a\u00020\nJ$\u0010\u000b\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u0006H\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/kyberswap/android/presentation/helper/Navigator;", "", "activity", "Landroid/support/v7/app/AppCompatActivity;", "(Landroid/support/v7/app/AppCompatActivity;)V", "containerId", "", "fragmentManager", "Landroid/support/v4/app/FragmentManager;", "navigateToLandingPage", "", "replaceFragment", "fragment", "Landroid/support/v4/app/Fragment;", "addToBackStack", "", "customAnimations", "Companion", "app_devDebug"})
public final class Navigator {
    private final int containerId = com.kyberswap.android.R.id.container;
    private final android.support.v4.app.FragmentManager fragmentManager = null;
    private final android.support.v7.app.AppCompatActivity activity = null;
    public static final int IN_RIGHT_OUT_LEFT = 1;
    public static final int IN_LEFT_OUT_RIGHT = -1;
    public static final int WITHOUT_ANIMATION = 0;
    public static final com.kyberswap.android.presentation.helper.Navigator.Companion Companion = null;
    
    public final void navigateToLandingPage() {
    }
    
    public final void replaceFragment(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment fragment, boolean addToBackStack, int customAnimations) {
    }
    
    public final void replaceFragment(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment fragment, boolean addToBackStack) {
    }
    
    public final void replaceFragment(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment fragment) {
    }
    
    @javax.inject.Inject()
    public Navigator(@org.jetbrains.annotations.NotNull()
    android.support.v7.app.AppCompatActivity activity) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/kyberswap/android/presentation/helper/Navigator$Companion;", "", "()V", "IN_LEFT_OUT_RIGHT", "", "IN_RIGHT_OUT_LEFT", "WITHOUT_ANIMATION", "app_devDebug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}