package com.kyberswap.android.data.api.home.entity;

import java.lang.System;

@android.arch.persistence.room.Entity(tableName = "ArticleFeatureEntities")
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0019\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J;\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010!\u001a\u00020\"H\u00d6\u0001J\t\u0010#\u001a\u00020\u0003H\u00d6\u0001R\u001e\u0010\u0002\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001e\u0010\u0006\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u000b\"\u0004\b\u0013\u0010\rR\u001e\u0010\u0007\u001a\u00020\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u000f\"\u0004\b\u0015\u0010\u0011R\u001e\u0010\b\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u000b\"\u0004\b\u0017\u0010\r\u00a8\u0006$"}, d2 = {"Lcom/kyberswap/android/data/api/home/entity/ArticleFeatureEntity;", "", "endDatetime", "", "id", "", "label", "order", "startDatetime", "(Ljava/lang/String;JLjava/lang/String;JLjava/lang/String;)V", "getEndDatetime", "()Ljava/lang/String;", "setEndDatetime", "(Ljava/lang/String;)V", "getId", "()J", "setId", "(J)V", "getLabel", "setLabel", "getOrder", "setOrder", "getStartDatetime", "setStartDatetime", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toString", "app_devDebug"})
public final class ArticleFeatureEntity {
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "end_datetime")
    private java.lang.String endDatetime;
    @android.arch.persistence.room.PrimaryKey()
    @com.google.gson.annotations.SerializedName(value = "id")
    private long id;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "label")
    private java.lang.String label;
    @com.google.gson.annotations.SerializedName(value = "order")
    private long order;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "start_datetime")
    private java.lang.String startDatetime;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEndDatetime() {
        return null;
    }
    
    public final void setEndDatetime(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final long getId() {
        return 0L;
    }
    
    public final void setId(long p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLabel() {
        return null;
    }
    
    public final void setLabel(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final long getOrder() {
        return 0L;
    }
    
    public final void setOrder(long p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStartDatetime() {
        return null;
    }
    
    public final void setStartDatetime(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public ArticleFeatureEntity(@org.jetbrains.annotations.NotNull()
    java.lang.String endDatetime, long id, @org.jetbrains.annotations.NotNull()
    java.lang.String label, long order, @org.jetbrains.annotations.NotNull()
    java.lang.String startDatetime) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final long component4() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity copy(@org.jetbrains.annotations.NotNull()
    java.lang.String endDatetime, long id, @org.jetbrains.annotations.NotNull()
    java.lang.String label, long order, @org.jetbrains.annotations.NotNull()
    java.lang.String startDatetime) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
}