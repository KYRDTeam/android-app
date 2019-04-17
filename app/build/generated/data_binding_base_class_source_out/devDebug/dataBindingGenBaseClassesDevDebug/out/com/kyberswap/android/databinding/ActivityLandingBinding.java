package com.kyberswap.android.databinding;

import android.databinding.Bindable;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.kyberswap.android.presentation.landing.LandingViewModel;
import me.relex.circleindicator.CircleIndicator;

public abstract class ActivityLandingBinding extends ViewDataBinding {
  @NonNull
  public final CircleIndicator indicator;

  @NonNull
  public final ViewPager vpLanding;

  @Bindable
  protected LandingViewModel mViewModel;

  protected ActivityLandingBinding(DataBindingComponent _bindingComponent, View _root,
      int _localFieldCount, CircleIndicator indicator, ViewPager vpLanding) {
    super(_bindingComponent, _root, _localFieldCount);
    this.indicator = indicator;
    this.vpLanding = vpLanding;
  }

  public abstract void setViewModel(@Nullable LandingViewModel viewModel);

  @Nullable
  public LandingViewModel getViewModel() {
    return mViewModel;
  }

  @NonNull
  public static ActivityLandingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivityLandingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivityLandingBinding>inflate(inflater, com.kyberswap.android.R.layout.activity_landing, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityLandingBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  @NonNull
  public static ActivityLandingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable DataBindingComponent component) {
    return DataBindingUtil.<ActivityLandingBinding>inflate(inflater, com.kyberswap.android.R.layout.activity_landing, null, false, component);
  }

  public static ActivityLandingBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  public static ActivityLandingBinding bind(@NonNull View view,
      @Nullable DataBindingComponent component) {
    return (ActivityLandingBinding)bind(component, view, com.kyberswap.android.R.layout.activity_landing);
  }
}
