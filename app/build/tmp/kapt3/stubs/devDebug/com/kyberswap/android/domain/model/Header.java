package com.kyberswap.android.domain.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\f\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0086\b\u0018\u0000 $2\u00020\u0001:\u0002$%B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u000f\b\u0016\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007B%\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u0015\u001a\u00020\tH\u00c6\u0003J\u000b\u0010\u0016\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\t\u0010\u0017\u001a\u00020\rH\u00c6\u0003J)\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\b\u001a\u00020\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\f\u001a\u00020\rH\u00c6\u0001J\b\u0010\u0019\u001a\u00020\u001aH\u0016J\u0013\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u00d6\u0003J\t\u0010\u001f\u001a\u00020\u001aH\u00d6\u0001J\t\u0010 \u001a\u00020\u000bH\u00d6\u0001J\u0018\u0010!\u001a\u00020\"2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010#\u001a\u00020\u001aH\u0016R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006&"}, d2 = {"Lcom/kyberswap/android/domain/model/Header;", "Landroid/os/Parcelable;", "parcel", "Landroid/os/Parcel;", "(Landroid/os/Parcel;)V", "articleFeatureEntity", "Lcom/kyberswap/android/data/api/home/entity/ArticleFeatureEntity;", "(Lcom/kyberswap/android/data/api/home/entity/ArticleFeatureEntity;)V", "type", "Lcom/kyberswap/android/domain/model/Header$Type;", "label", "", "featureId", "", "(Lcom/kyberswap/android/domain/model/Header$Type;Ljava/lang/String;J)V", "getFeatureId", "()J", "getLabel", "()Ljava/lang/String;", "getType", "()Lcom/kyberswap/android/domain/model/Header$Type;", "component1", "component2", "component3", "copy", "describeContents", "", "equals", "", "other", "", "hashCode", "toString", "writeToParcel", "", "flags", "CREATOR", "Type", "app_devDebug"})
public final class Header implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull()
    private final com.kyberswap.android.domain.model.Header.Type type = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String label = null;
    private final long featureId = 0L;
    public static final com.kyberswap.android.domain.model.Header.CREATOR CREATOR = null;
    
    @java.lang.Override()
    public void writeToParcel(@org.jetbrains.annotations.NotNull()
    android.os.Parcel parcel, int flags) {
    }
    
    @java.lang.Override()
    public int describeContents() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.domain.model.Header.Type getType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getLabel() {
        return null;
    }
    
    public final long getFeatureId() {
        return 0L;
    }
    
    public Header(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.model.Header.Type type, @org.jetbrains.annotations.Nullable()
    java.lang.String label, long featureId) {
        super();
    }
    
    public Header() {
        super();
    }
    
    public Header(@org.jetbrains.annotations.NotNull()
    android.os.Parcel parcel) {
        super();
    }
    
    public Header(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity articleFeatureEntity) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.domain.model.Header.Type component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    public final long component3() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.domain.model.Header copy(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.model.Header.Type type, @org.jetbrains.annotations.Nullable()
    java.lang.String label, long featureId) {
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
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0001\u0018\u0000 \r2\b\u0012\u0004\u0012\u00020\u00000\u00012\u00020\u0002:\u0001\rB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H\u0016J\u0018\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0016j\u0002\b\u000bj\u0002\b\f\u00a8\u0006\u000e"}, d2 = {"Lcom/kyberswap/android/domain/model/Header$Type;", "", "Landroid/os/Parcelable;", "(Ljava/lang/String;I)V", "describeContents", "", "writeToParcel", "", "parcel", "Landroid/os/Parcel;", "flags", "TOP", "FEATURE", "CREATOR", "app_devDebug"})
    public static enum Type implements android.os.Parcelable {
        /*public static final*/ TOP /* = new TOP() */,
        /*public static final*/ FEATURE /* = new FEATURE() */;
        public static final com.kyberswap.android.domain.model.Header.Type.CREATOR CREATOR = null;
        
        @java.lang.Override()
        public void writeToParcel(@org.jetbrains.annotations.NotNull()
        android.os.Parcel parcel, int flags) {
        }
        
        @java.lang.Override()
        public int describeContents() {
            return 0;
        }
        
        Type() {
        }
        
        @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00022\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u001d\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/kyberswap/android/domain/model/Header$Type$CREATOR;", "Landroid/os/Parcelable$Creator;", "Lcom/kyberswap/android/domain/model/Header$Type;", "()V", "createFromParcel", "parcel", "Landroid/os/Parcel;", "newArray", "", "size", "", "(I)[Lcom/kyberswap/android/domain/model/Header$Type;", "app_devDebug"})
        public static final class CREATOR implements android.os.Parcelable.Creator<com.kyberswap.android.domain.model.Header.Type> {
            
            @org.jetbrains.annotations.NotNull()
            @java.lang.Override()
            public com.kyberswap.android.domain.model.Header.Type createFromParcel(@org.jetbrains.annotations.NotNull()
            android.os.Parcel parcel) {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            @java.lang.Override()
            public com.kyberswap.android.domain.model.Header.Type[] newArray(int size) {
                return null;
            }
            
            private CREATOR() {
                super();
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00022\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u001d\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/kyberswap/android/domain/model/Header$CREATOR;", "Landroid/os/Parcelable$Creator;", "Lcom/kyberswap/android/domain/model/Header;", "()V", "createFromParcel", "parcel", "Landroid/os/Parcel;", "newArray", "", "size", "", "(I)[Lcom/kyberswap/android/domain/model/Header;", "app_devDebug"})
    public static final class CREATOR implements android.os.Parcelable.Creator<com.kyberswap.android.domain.model.Header> {
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public com.kyberswap.android.domain.model.Header createFromParcel(@org.jetbrains.annotations.NotNull()
        android.os.Parcel parcel) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public com.kyberswap.android.domain.model.Header[] newArray(int size) {
            return null;
        }
        
        private CREATOR() {
            super();
        }
    }
}