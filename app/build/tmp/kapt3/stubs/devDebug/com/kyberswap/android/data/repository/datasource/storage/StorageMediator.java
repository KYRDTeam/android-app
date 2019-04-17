package com.kyberswap.android.data.repository.datasource.storage;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\b\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0005\u001a\u00020\u0006J\b\u0010\u0007\u001a\u0004\u0018\u00010\bJ\b\u0010\t\u001a\u0004\u0018\u00010\nJ\u0006\u0010\u000b\u001a\u00020\fJ\u0006\u0010\r\u001a\u00020\fJ\u000e\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\nJ\u000e\u0010\u0010\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\fJ\u000e\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/kyberswap/android/data/repository/datasource/storage/StorageMediator;", "", "hawkWrapper", "Lcom/kyberswap/android/data/repository/datasource/storage/HawkWrapper;", "(Lcom/kyberswap/android/data/repository/datasource/storage/HawkWrapper;)V", "clearAuthenticationInformation", "", "getAuthentication", "Lcom/kyberswap/android/domain/model/LoginSession;", "getCredential", "Lcom/kyberswap/android/domain/model/Credential;", "isAuthenticated", "", "isShownPostGuide", "saveCredential", "credential", "savePostGuide", "setAuthentication", "loginSession", "Companion", "app_devDebug"})
public final class StorageMediator {
    private final com.kyberswap.android.data.repository.datasource.storage.HawkWrapper hawkWrapper = null;
    private static final java.lang.String KEY_AUTHENTICATION = "authentication";
    private static final java.lang.String KEY_CREDENTIAL = "credential";
    private static final java.lang.String KEY_SHOWN_POST_GUIDE = "post_guide";
    public static final com.kyberswap.android.data.repository.datasource.storage.StorageMediator.Companion Companion = null;
    
    public final boolean setAuthentication(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.model.LoginSession loginSession) {
        return false;
    }
    
    public final boolean isAuthenticated() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.kyberswap.android.domain.model.LoginSession getAuthentication() {
        return null;
    }
    
    public final boolean saveCredential(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.model.Credential credential) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.kyberswap.android.domain.model.Credential getCredential() {
        return null;
    }
    
    public final void clearAuthenticationInformation() {
    }
    
    public final boolean isShownPostGuide() {
        return false;
    }
    
    public final void savePostGuide(boolean isShownPostGuide) {
    }
    
    @javax.inject.Inject()
    public StorageMediator(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.repository.datasource.storage.HawkWrapper hawkWrapper) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/kyberswap/android/data/repository/datasource/storage/StorageMediator$Companion;", "", "()V", "KEY_AUTHENTICATION", "", "KEY_CREDENTIAL", "KEY_SHOWN_POST_GUIDE", "app_devDebug"})
    public static final class Companion {
        
        private Companion() {
            super();

    }
}