package com.kyberswap.android.util.views;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0007J\"\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\"\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\f\u001a\u0004\u0018\u00010\r2\u0006\u0010\t\u001a\u00020\nH\u0007J\"\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000f\u001a\u00020\nH\u0007J\u0018\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0007\u00a8\u0006\u0013"}, d2 = {"Lcom/kyberswap/android/util/views/ImageViewBindingAdapter;", "", "()V", "loadImage", "", "view", "Landroid/widget/ImageView;", "url", "", "error", "Landroid/graphics/drawable/Drawable;", "loadImageUri", "uri", "Landroid/net/Uri;", "loadImageWithPlaceHolder", "placeHolder", "loadResource", "resourceId", "", "app_devDebug"})
public final class ImageViewBindingAdapter {
    public static final com.kyberswap.android.util.views.ImageViewBindingAdapter INSTANCE = null;
    
    @android.databinding.BindingAdapter(value = {"app:imageUrl"})
    public static final void loadImage(@org.jetbrains.annotations.NotNull()
    android.widget.ImageView view, @org.jetbrains.annotations.Nullable()
    java.lang.String url) {
    }
    
    @android.databinding.BindingAdapter(value = {"app:imageUrl", "app:error"})
    public static final void loadImage(@org.jetbrains.annotations.NotNull()
    android.widget.ImageView view, @org.jetbrains.annotations.Nullable()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    android.graphics.drawable.Drawable error) {
    }
    
    @android.databinding.BindingAdapter(value = {"imageUrl", "placeHolder"})
    public static final void loadImageWithPlaceHolder(@org.jetbrains.annotations.NotNull()
    android.widget.ImageView view, @org.jetbrains.annotations.Nullable()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    android.graphics.drawable.Drawable placeHolder) {
    }
    
    @android.databinding.BindingAdapter(value = {"app:imageUri", "app:error"})
    public static final void loadImageUri(@org.jetbrains.annotations.NotNull()
    android.widget.ImageView view, @org.jetbrains.annotations.Nullable()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    android.graphics.drawable.Drawable error) {
    }
    
    @android.databinding.BindingAdapter(value = {"app:resourceId"})
    public static final void loadResource(@org.jetbrains.annotations.NotNull()
    android.widget.ImageView view, int resourceId) {
    }
    
    private ImageViewBindingAdapter() {
        super();
    }
}