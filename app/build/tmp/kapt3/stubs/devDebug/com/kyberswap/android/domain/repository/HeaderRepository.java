package com.kyberswap.android.domain.repository;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H&J\u0014\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u0003H&J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH&\u00a8\u0006\f"}, d2 = {"Lcom/kyberswap/android/domain/repository/HeaderRepository;", "", "getLikeAndReviewInfo", "Lio/reactivex/Flowable;", "Lcom/kyberswap/android/domain/model/HeaderInfo;", "headers", "", "Lcom/kyberswap/android/domain/model/Header;", "save", "", "headerEntity", "Lcom/kyberswap/android/data/api/home/entity/HeaderEntity;", "app_devDebug"})
public abstract interface HeaderRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract io.reactivex.Flowable<java.util.List<com.kyberswap.android.domain.model.Header>> headers();
    
    public abstract void save(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity);
    
    @org.jetbrains.annotations.NotNull()
    public abstract io.reactivex.Flowable<com.kyberswap.android.domain.model.HeaderInfo> getLikeAndReviewInfo();
}