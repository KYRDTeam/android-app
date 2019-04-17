package com.kyberswap.android.presentation.base;

import java.lang.System;

@kotlin.Suppress(names = {"LeakingThis"})
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b&\u0018\u0000*\u0004\b\u0000\u0010\u0001*\b\b\u0001\u0010\u0002*\u00020\u00032\b\u0012\u0004\u0012\u0002H\u00020\u0004B\u0015\b\u0014\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00028\u00000\u0006\u00a2\u0006\u0002\u0010\u0007B\u0015\b\u0014\u0012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00028\u00000\t\u00a2\u0006\u0002\u0010\nJ\f\u0010\r\u001a\b\u0012\u0004\u0012\u00028\u00000\u000eJ\u0015\u0010\u000f\u001a\u00028\u00002\u0006\u0010\u0010\u001a\u00020\u0011H\u0016\u00a2\u0006\u0002\u0010\u0012J\b\u0010\u0013\u001a\u00020\u0011H\u0016J\u0014\u0010\u0014\u001a\u00020\u00152\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00028\u00000\u000eR\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00028\u00000\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/kyberswap/android/presentation/base/ListDifferAdapter;", "T", "VH", "Landroid/support/v7/widget/RecyclerView$ViewHolder;", "Landroid/support/v7/widget/RecyclerView$Adapter;", "diffCallback", "Landroid/support/v7/util/DiffUtil$ItemCallback;", "(Landroid/support/v7/util/DiffUtil$ItemCallback;)V", "config", "Landroid/support/v7/recyclerview/extensions/AsyncDifferConfig;", "(Landroid/support/v7/recyclerview/extensions/AsyncDifferConfig;)V", "helper", "Landroid/support/v7/recyclerview/extensions/AsyncListDiffer;", "getData", "", "getItem", "position", "", "(I)Ljava/lang/Object;", "getItemCount", "submitList", "", "list", "app_devDebug"})
public abstract class ListDifferAdapter<T extends java.lang.Object, VH extends android.support.v7.widget.RecyclerView.ViewHolder> extends android.support.v7.widget.RecyclerView.Adapter<VH> {
    private final android.support.v7.recyclerview.extensions.AsyncListDiffer<T> helper = null;
    
    public final void submitList(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends T> list) {
    }
    
    public T getItem(int position) {
        return null;
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<T> getData() {
        return null;
    }
    
    protected ListDifferAdapter(@org.jetbrains.annotations.NotNull()
    android.support.v7.util.DiffUtil.ItemCallback<T> diffCallback) {
        super();
    }
    
    protected ListDifferAdapter(@org.jetbrains.annotations.NotNull()
    android.support.v7.recyclerview.extensions.AsyncDifferConfig<T> config) {
        super();
    }
}