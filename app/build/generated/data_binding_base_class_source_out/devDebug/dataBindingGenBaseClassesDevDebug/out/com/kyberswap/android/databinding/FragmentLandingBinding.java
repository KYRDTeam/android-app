package com.kyberswap.android.databinding;

import android.databinding.Bindable;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.kyberswap.android.presentation.landing.LandingViewModel;

public abstract class FragmentLandingBinding extends ViewDataBinding {
  @NonNull
  public final ImageView imgLanding;

  @NonNull
  public final TextView textCreateWallet;

  @NonNull
  public final TextView textView2;

  @NonNull
  public final TextView tvImportWallet;

  @NonNull
  public final TextView tvLanding;

  @NonNull
  public final TextView tvPromo;

  @NonNull
  public final TextView tvTermAndCondition;

  @Bindable
  protected LandingViewModel mViewModel;

  protected FragmentLandingBinding(DataBindingComponent _bindingComponent, View _root,
      int _localFieldCount, ImageView imgLanding, TextView textCreateWallet, TextView textView2,
      TextView tvImportWallet, TextView tvLanding, TextView tvPromo, TextView tvTermAndCondition) {
    super(_bindingComponent, _root, _localFieldCount);
    this.imgLanding = imgLanding;
    this.textCreateWallet = textCreateWallet;
    this.textView2 = textView2;
    this.tvImportWallet = tvImportWallet;
    this.tvLanding = tvLanding;
    this.tvPromo = tvPromo;
    this.tvTermAndCondition = tvTermAndCondition;
  }

  public abstract void setViewModel(@Nullable LandingViewModel viewModel);

  @Nullable
  public LandingViewModel getViewModel() {
    return mViewModel;
  }

  @NonNull
  public static FragmentLandingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static FragmentLandingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable DataBindingComponent component) {
    return DataBindingUtil.<FragmentLandingBinding>inflate(inflater, com.kyberswap.android.R.layout.fragment_landing, root, attachToRoot, component);
  }

  @NonNull
  public static FragmentLandingBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static FragmentLandingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable DataBindingComponent component) {
    return DataBindingUtil.<FragmentLandingBinding>inflate(inflater, com.kyberswap.android.R.layout.fragment_landing, null, false, component);
  }

  public static FragmentLandingBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  public static FragmentLandingBinding bind(@NonNull View view,
      @Nullable DataBindingComponent component) {
    return (FragmentLandingBinding)bind(component, view, com.kyberswap.android.R.layout.fragment_landing);
  }
}
