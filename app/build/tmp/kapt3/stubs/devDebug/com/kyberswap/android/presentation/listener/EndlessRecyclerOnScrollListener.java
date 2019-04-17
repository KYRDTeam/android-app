package com.kyberswap.android.presentation.listener;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b&\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u0006H&J \u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u0006H\u0016J\u0006\u0010\u0015\u001a\u00020\u000fR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/kyberswap/android/presentation/listener/EndlessRecyclerOnScrollListener;", "Landroid/support/v7/widget/RecyclerView$OnScrollListener;", "linearLayoutManager", "Landroid/support/v7/widget/LinearLayoutManager;", "(Landroid/support/v7/widget/LinearLayoutManager;)V", "currentPage", "", "firstVisibleItem", "loading", "", "previousTotal", "totalItemCount", "visibleItemCount", "visibleThreshold", "onLoadMore", "", "onScrolled", "recyclerView", "Landroid/support/v7/widget/RecyclerView;", "dx", "dy", "reset", "app_devDebug"})
public abstract class EndlessRecyclerOnScrollListener extends android.support.v7.widget.RecyclerView.OnScrollListener {
    private int previousTotal;
    private boolean loading;
    private final int visibleThreshold = 5;
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;
    private int currentPage;
    private final android.support.v7.widget.LinearLayoutManager linearLayoutManager = null;
    
    @java.lang.Override()
    public void onScrolled(@org.jetbrains.annotations.NotNull()
    android.support.v7.widget.RecyclerView recyclerView, int dx, int dy) {
    }
    
    public final void reset() {
    }
    
    public abstract void onLoadMore(int currentPage);
    
    public EndlessRecyclerOnScrollListener(@org.jetbrains.annotations.NotNull()
    android.support.v7.widget.LinearLayoutManager linearLayoutManager) {
        super();
    }
}