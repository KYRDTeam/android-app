package com.kyberswap.android.util.di.module;

import android.app.Activity;
import com.kyberswap.android.presentation.landing.LandingActivity;
import com.kyberswap.android.util.di.scope.ActivityScoped;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents = ActivityBindingModule_ContributeLandingActivity.LandingActivitySubcomponent.class
)
public abstract class ActivityBindingModule_ContributeLandingActivity {
  private ActivityBindingModule_ContributeLandingActivity() {}

  @Binds
  @IntoMap
  @ActivityKey(LandingActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindAndroidInjectorFactory(
      LandingActivitySubcomponent.Builder builder);

  @Subcomponent(modules = LandingActivityModule.class)
  @ActivityScoped
  public interface LandingActivitySubcomponent extends AndroidInjector<LandingActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<LandingActivity> {}
  }
}
