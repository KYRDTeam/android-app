package com.kyberswap.android.util;

import java.lang.System;

/**
 * A lazy property that gets cleaned up when the fragment is destroyed.
 *
 * Accessing this variable in a destroyed fragment will throw NPE.
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u0002H\u00010\u0003B\r\u0012\u0006\u0010\u0005\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0006J\"\u0010\u000b\u001a\u00028\u00002\u0006\u0010\f\u001a\u00020\u00042\n\u0010\r\u001a\u0006\u0012\u0002\b\u00030\u000eH\u0096\u0002\u00a2\u0006\u0002\u0010\u000fJ*\u0010\u0010\u001a\u00020\u00112\u0006\u0010\f\u001a\u00020\u00042\n\u0010\r\u001a\u0006\u0012\u0002\b\u00030\u000e2\u0006\u0010\t\u001a\u00028\u0000H\u0096\u0002\u00a2\u0006\u0002\u0010\u0012R\u0011\u0010\u0005\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0012\u0010\t\u001a\u0004\u0018\u00018\u0000X\u0082\u000e\u00a2\u0006\u0004\n\u0002\u0010\n\u00a8\u0006\u0013"}, d2 = {"Lcom/kyberswap/android/util/AutoClearedValue;", "T", "", "Lkotlin/properties/ReadWriteProperty;", "Landroid/support/v4/app/Fragment;", "fragment", "(Landroid/support/v4/app/Fragment;)V", "getFragment", "()Landroid/support/v4/app/Fragment;", "value", "Ljava/lang/Object;", "getValue", "thisRef", "property", "Lkotlin/reflect/KProperty;", "(Landroid/support/v4/app/Fragment;Lkotlin/reflect/KProperty;)Ljava/lang/Object;", "setValue", "", "(Landroid/support/v4/app/Fragment;Lkotlin/reflect/KProperty;Ljava/lang/Object;)V", "app_devDebug"})
public final class AutoClearedValue<T extends java.lang.Object> implements kotlin.properties.ReadWriteProperty<android.support.v4.app.Fragment, T> {
    private T value;
    @org.jetbrains.annotations.NotNull()
    private final android.support.v4.app.Fragment fragment = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public T getValue(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment thisRef, @org.jetbrains.annotations.NotNull()
    kotlin.reflect.KProperty<?> property) {
        return null;
    }
    
    @java.lang.Override()
    public void setValue(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment thisRef, @org.jetbrains.annotations.NotNull()
    kotlin.reflect.KProperty<?> property, @org.jetbrains.annotations.NotNull()
    T value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.support.v4.app.Fragment getFragment() {
        return null;
    }
    
    public AutoClearedValue(@org.jetbrains.annotations.NotNull()
    android.support.v4.app.Fragment fragment) {
        super();
    }
}