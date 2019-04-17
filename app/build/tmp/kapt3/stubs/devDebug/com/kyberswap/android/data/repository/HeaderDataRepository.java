package com.kyberswap.android.data.repository;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B#\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u000e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u0016J\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\tH\u0016J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/kyberswap/android/data/repository/HeaderDataRepository;", "Lcom/kyberswap/android/domain/repository/HeaderRepository;", "headerLocalDataSource", "Lcom/kyberswap/android/data/repository/datasource/HeaderDataStore;", "headerRemoteDataSource", "mapper", "Lcom/kyberswap/android/data/mapper/HeaderMapper;", "(Lcom/kyberswap/android/data/repository/datasource/HeaderDataStore;Lcom/kyberswap/android/data/repository/datasource/HeaderDataStore;Lcom/kyberswap/android/data/mapper/HeaderMapper;)V", "getLikeAndReviewInfo", "Lio/reactivex/Flowable;", "Lcom/kyberswap/android/domain/model/HeaderInfo;", "headers", "", "Lcom/kyberswap/android/domain/model/Header;", "save", "", "headerEntity", "Lcom/kyberswap/android/data/api/home/entity/HeaderEntity;", "app_devDebug"})
public final class HeaderDataRepository implements com.kyberswap.android.domain.repository.HeaderRepository {
    private final com.kyberswap.android.data.repository.datasource.HeaderDataStore headerLocalDataSource = null;
    private final com.kyberswap.android.data.repository.datasource.HeaderDataStore headerRemoteDataSource = null;
    private final com.kyberswap.android.data.mapper.HeaderMapper mapper = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public io.reactivex.Flowable<com.kyberswap.android.domain.model.HeaderInfo> getLikeAndReviewInfo() {
        return null;
    }
    
    @java.lang.Override()
    public void save(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public io.reactivex.Flowable<java.util.List<com.kyberswap.android.domain.model.Header>> headers() {
        return null;
    }
    
    @javax.inject.Inject()
    public HeaderDataRepository(@org.jetbrains.annotations.NotNull()
    @com.kyberswap.android.util.di.qualifier.Local()
    com.kyberswap.android.data.repository.datasource.HeaderDataStore headerLocalDataSource, @org.jetbrains.annotations.NotNull()
    @com.kyberswap.android.util.di.qualifier.Remote()
    com.kyberswap.android.data.repository.datasource.HeaderDataStore headerRemoteDataSource, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.mapper.HeaderMapper mapper) {
        super();
    }
}