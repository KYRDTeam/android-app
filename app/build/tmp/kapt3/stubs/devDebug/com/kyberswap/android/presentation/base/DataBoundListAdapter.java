package com.kyberswap.android.presentation.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\b&\u0018\u0000*\u0004\b\u0000\u0010\u0001*\b\b\u0001\u0010\u0002*\u00020\u00032\u0014\u0012\u0004\u0012\u0002H\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00050\u0004B\u001b\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00028\u00000\t\u00a2\u0006\u0002\u0010\nJ\u001d\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00028\u00012\u0006\u0010\u000e\u001a\u00028\u0000H$\u00a2\u0006\u0002\u0010\u000fJ\u001d\u0010\u0010\u001a\u00028\u00012\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014H$\u00a2\u0006\u0002\u0010\u0015J\u001e\u0010\u0016\u001a\u00020\f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00028\u00010\u00052\u0006\u0010\u0018\u001a\u00020\u0014H\u0016J\u001e\u0010\u0019\u001a\b\u0012\u0004\u0012\u00028\u00010\u00052\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014H\u0016\u00a8\u0006\u001a"}, d2 = {"Lcom/kyberswap/android/presentation/base/DataBoundListAdapter;", "T", "V", "Landroid/databinding/ViewDataBinding;", "Lcom/kyberswap/android/presentation/base/ListDifferAdapter;", "Lcom/kyberswap/android/presentation/base/DataBoundViewHolder;", "appExecutors", "Lcom/kyberswap/android/AppExecutors;", "diffCallback", "Landroid/support/v7/util/DiffUtil$ItemCallback;", "(Lcom/kyberswap/android/AppExecutors;Landroid/support/v7/util/DiffUtil$ItemCallback;)V", "bind", "", "binding", "item", "(Landroid/databinding/ViewDataBinding;Ljava/lang/Object;)V", "createBinding", "parent", "Landroid/view/ViewGroup;", "viewType", "", "(Landroid/view/ViewGroup;I)Landroid/databinding/ViewDataBinding;", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "app_devDebug"})
public abstract class DataBoundListAdapter<T extends java.lang.Object, V extends android.databinding.ViewDataBinding> extends com.kyberswap.android.presentation.base.ListDifferAdapter<T, com.kyberswap.android.presentation.base.DataBoundViewHolder<? extends V>> {
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public com.kyberswap.android.presentation.base.DataBoundViewHolder<V> onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected abstract V createBinding(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType);
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.presentation.base.DataBoundViewHolder<? extends V> holder, int position) {
    }
    
    protected abstract void bind(@org.jetbrains.annotations.NotNull()
    V binding, T item);
    
    public DataBoundListAdapter(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.AppExecutors appExecutors, @org.jetbrains.annotations.NotNull()
    android.support.v7.util.DiffUtil.ItemCallback<T> diffCallback) {
        super(null);
    }
}